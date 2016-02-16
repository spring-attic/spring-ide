/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.beans.core.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.config.core.schemas.BeansSchemaConstants;
import org.springframework.ide.eclipse.core.java.Introspector;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.wizard.Messages;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.core.WizardContentAssistConverter;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Wizard page for specifying bean properties
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class BeanPropertiesWizardPage extends AbstractBeanWizardPage {

	private Table propertiesTable, constructorArgsTable;

	private Image[] errorImages, warningImages;

	private int propertyProblemCounter, constArgProblemCounter;

	private static final int PROPERTY = 0, CONSTRUCTOR_ARG = 1;

	private final Set<String> definedProperties;

	private ComboViewer constructorCombo;

	private IStructuredContentProvider comboContentProvider;

	private static final String DEFAULT_MESSAGE = Messages.getString("BeanPropertiesWizardPage.TITLE_DESCRIPTION"); //$NON-NLS-1$

	private Button ignoreErrorButton;

	private static final String ICON_PATH_PREFIX = "icons/full/etool16/", ERROR_ICON = "error.gif",
			WARNING_ICON = "warning.gif", ERROR_ICON2 = "error2.gif", WARNING_ICON2 = "warning2.gif";

	protected BeanPropertiesWizardPage(String pageName, BeanWizard wizard) {
		super(pageName, wizard);

		setTitle(Messages.getString("BeanPropertiesWizardPage.TITLE")); //$NON-NLS-1$
		setDescription(Messages.getString("BeanPropertiesWizardPage.TITLE_DESCRIPTION")); //$NON-NLS-1$

		this.definedProperties = new HashSet<String>();

		this.propertyProblemCounter = 0;
		this.constArgProblemCounter = 0;
	}

	protected boolean checkCanProceed(int kind) {
		if (kind == PROPERTY) {
			return true;
		}

		if (!constructorCombo.getSelection().isEmpty()) {
			boolean confirmed = MessageDialog
					.openConfirm(
							getShell(),
							Messages.getString("BeanPropertiesWizardPage.CONFIRM_CONSTRUCTOR_CHANGE_DIALOG_TITLE"), Messages.getString("BeanPropertiesWizardPage.CONFIRM_CONSTRUCTOR_CHANGE_DIALOG_MESSAGE")); //$NON-NLS-1$ //$NON-NLS-2$
			if (confirmed) {
				constructorCombo.setSelection(StructuredSelection.EMPTY);
			}
			return confirmed;
		}
		return true;
	}

	private void createButtons(final int kind, Composite parent, final String nodeName, final Table table) {
		Composite rightContainer = new Composite(parent, SWT.NONE);
		rightContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
		GridLayout rightLayout = new GridLayout();
		rightLayout.marginHeight = 0;
		rightLayout.marginWidth = 0;
		rightContainer.setLayout(rightLayout);

		GridData buttonData = new GridData();

		Button addButton = new Button(rightContainer, SWT.NONE);
		addButton.setText(Messages.getString("BeanPropertiesWizardPage.ADD_BUTTON_LABEL")); //$NON-NLS-1$
		addButton.setLayoutData(buttonData);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (checkCanProceed(kind)) {
					IDOMElement newElement = createNewChild(kind, nodeName);
					String title = null;

					if (kind == PROPERTY) {
						title = Messages.getString("BeanPropertiesWizardPage.NEW_PROPERTY_DIALOG_TITLE"); //$NON-NLS-1$
					}
					else if (kind == CONSTRUCTOR_ARG) {
						title = Messages.getString("BeanPropertiesWizardPage.NEW_CONSTRUCTOR_ARG_DIALOG_TITLE"); //$NON-NLS-1$
					}

					BeanChildDialog dialog = createDialog(kind, newElement, title, true);
					int status = dialog.open();

					if (status == Dialog.OK) {
						createElementItem(kind, newElement);
					}
					else {
						wizard.getNewBean().removeChild(newElement);
					}
				}
			}
		});

		Button editButton = new Button(rightContainer, SWT.NONE);
		editButton.setText(Messages.getString("BeanPropertiesWizardPage.EDIT_LABEL")); //$NON-NLS-1$
		editButton.setLayoutData(buttonData);
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editSelectedElementItem(kind, table);
			}
		});

		Button removeButton = new Button(rightContainer, SWT.NONE);
		removeButton.setText(Messages.getString("BeanPropertiesWizardPage.REMOVE_LABEL")); //$NON-NLS-1$
		removeButton.setLayoutData(buttonData);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (checkCanProceed(kind)) {
					removeSelected(kind, table);
				}
			}
		});

		table.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
				editSelectedElementItem(kind, table);
			}
		});
	}

	private void createCombo(Composite sectionComposite) {
		constructorCombo = new ComboViewer(sectionComposite);
		comboContentProvider = new IStructuredContentProvider() {

			public void dispose() {
			}

			public Object[] getElements(Object inputElement) {
				IFile beanFile = wizard.getBeanFile();
				if (beanFile == null) {
					return new Object[0];
				}

				String className = BeansEditorUtils.getClassNameForBean(beanFile, wizard.getOriginalDocument(), wizard
						.getNewBean());
				IType type = JdtUtils.getJavaType(beanFile.getProject(), className);
				Set<IMethod> constructors = null;
				try {
					constructors = Introspector.findAllConstructors(type);
				}
				catch (JavaModelException e) {
					StatusHandler.log(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID,
							"Failed to populate constructor combo.", e));
				}
				if (constructors == null) {
					return new Object[0];
				}
				return constructors.toArray();
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		};
		constructorCombo.setContentProvider(comboContentProvider);
		constructorCombo.setInput(this);
		constructorCombo.setLabelProvider(new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT));

		constructorCombo.addFilter(new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				if (element instanceof IMethod) {
					IMethod method = (IMethod) element;
					try {
						return method.getParameterNames().length > 0;
					}
					catch (JavaModelException e) {
						StatusHandler.log(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID,
								"Failed to populate constructor combo.", e));
					}
				}
				return false;
			}
		});

		GridData comboData = new GridData(SWT.FILL, SWT.FILL, true, false);
		comboData.horizontalSpan = 2;
		comboData.widthHint = 400;
		constructorCombo.getControl().setLayoutData(comboData);
		constructorCombo.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = constructorCombo.getSelection();
				if (selection instanceof StructuredSelection) {
					StructuredSelection structuredSelection = (StructuredSelection) selection;
					Object[] items = structuredSelection.toArray();
					if (items.length == 1) {
						IMethod constructor = (IMethod) items[0];
						int numParam = constructor.getNumberOfParameters();
						int itemCount = constructorArgsTable.getItemCount();
						if (itemCount != numParam) {
							if (itemCount > 0) {
								boolean confirmed = MessageDialog
										.openConfirm(
												getShell(),
												Messages
														.getString("BeanPropertiesWizardPage.CHANG_CONSTRUCTOR_ARGS_CONFIRM_DIALOG_TITLE"), Messages.getString("BeanPropertiesWizardPage.CHANG_CONSTRUCTOR_ARGS_CONFIRM_DIALOG_MESSAGE")); //$NON-NLS-1$ //$NON-NLS-2$
								if (!confirmed) {
									return;
								}
								constructorArgsTable.removeAll();
							}

							for (int i = 0; i < numParam; i++) {
								IDOMElement child = createNewChild(CONSTRUCTOR_ARG,
										BeansSchemaConstants.ELEM_CONSTRUCTOR_ARG);
								createElementItem(CONSTRUCTOR_ARG, child);
							}
							constructorArgsTable.redraw();
						}
					}
				}
			}
		});
	}

	private void createConstructorArgsSection(Composite container) {
		Composite sectionComposite = createSection(container, Messages
				.getString("BeanPropertiesWizardPage.CONSTRUCTOR_ARGS_SECTION_TITLE"), false); //$NON-NLS-1$

		createCombo(sectionComposite);
		constructorArgsTable = createTable(sectionComposite);
		TableColumn valueColumn = new TableColumn(constructorArgsTable, SWT.NONE);
		valueColumn.setText(Messages.getString("BeanPropertiesWizardPage.VALUE/REF_COLUMN_TITLE")); //$NON-NLS-1$
		valueColumn.setWidth(250);

		createButtons(CONSTRUCTOR_ARG, sectionComposite, BeansSchemaConstants.ELEM_CONSTRUCTOR_ARG,
				constructorArgsTable);
	}

	public void createControl(Composite parent) {
		try {
			String prefix = WizardPlugin.getDefault().getBundle().getEntry("/") + ICON_PATH_PREFIX;
			Display display = getShell().getDisplay();
			errorImages = new Image[] {
					new Image(display, ImageDescriptor.createFromURL(new URL(prefix + ERROR_ICON)).getImageData()),
					new Image(display, ImageDescriptor.createFromURL(new URL(prefix + ERROR_ICON2)).getImageData()) };
			warningImages = new Image[] {
					new Image(display, ImageDescriptor.createFromURL(new URL(prefix + WARNING_ICON)).getImageData()),
					new Image(display, ImageDescriptor.createFromURL(new URL(prefix + WARNING_ICON2)).getImageData()) };
		}
		catch (MalformedURLException e) {
			StatusHandler.log(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID,
					"Failed to create bean properties wizard page.", e));
		}

		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());

		Composite topContainer = new Composite(container, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginRight = 0;
		topContainer.setLayout(layout);
		topContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite bottomContainer = new Composite(container, SWT.NONE);
		GridLayout layout2 = new GridLayout();
		layout2.marginWidth = 0;
		bottomContainer.setLayout(layout2);
		bottomContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		createPropertySection(topContainer);
		createConstructorArgsSection(bottomContainer);

		ignoreErrorButton = wizard.createIgnoreErrorButton(container, this);
		ignoreErrorButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				validateElements();
				updateMessage();
			}
		});

		populateValues();

		setControl(container);
	}

	private BeanChildDialog createDialog(int kind, IDOMElement element, String title, boolean isNew) {
		if (kind == PROPERTY) {
			return new PropertyDialog(getShell(), wizard, element, title, definedProperties, isNew);
		}
		else if (kind == CONSTRUCTOR_ARG) {
			return new ConstructorArgDialog(getShell(), wizard, element, title, isNew);
		}
		return null;
	}

	private void createElementItem(int kind, IDOMElement newElement) {
		Table table = null;

		if (kind == PROPERTY) {
			table = propertiesTable;
			definedProperties.add(newElement.getAttribute(BeansSchemaConstants.ATTR_NAME));
		}
		else if (kind == CONSTRUCTOR_ARG) {
			table = constructorArgsTable;
		}

		TableItem item = new TableItem(table, SWT.NONE);
		item.setData(newElement);
		item.setText(getElementItemText(kind, newElement));

		if (kind == PROPERTY) {
			propertyProblemCounter += validateElement(kind, newElement, item);
		}
		else {
			constArgProblemCounter += validateElement(kind, newElement, item);
		}

		updateMessage();
	}

	private IDOMElement createNewChild(int kind, String nodeName) {
		IDOMElement newBean = wizard.getNewBean();
		IDOMElement newChild = (IDOMElement) newBean.getOwnerDocument().createElementNS(
				NamespaceUtils.DEFAULT_NAMESPACE_URI, nodeName);

		if (kind == PROPERTY) {
			newBean.appendChild(newChild);
		}
		else {
			// find the first property child and insert constructor arg before
			// it
			NodeList childNodes = newBean.getChildNodes();
			Node firstPropertyNode = null;

			for (int i = 0; i < childNodes.getLength(); i++) {
				Node childNode = childNodes.item(i);
				String childNodeName = childNode.getNodeName();
				if (childNodeName.equals(BeansSchemaConstants.ELEM_PROPERTY)) {
					firstPropertyNode = childNode;
					break;
				}
			}

			if (firstPropertyNode != null) {
				newBean.insertBefore(newChild, firstPropertyNode);
			}
			else {
				newBean.appendChild(newChild);
			}
		}
		return newChild;
	}

	private void createPropertySection(Composite container) {
		Composite sectionComposite = createSection(container, Messages
				.getString("BeanPropertiesWizardPage.PROPERTIES_SECTION_TITLE"), true); //$NON-NLS-1$

		propertiesTable = createTable(sectionComposite);

		TableColumn nameColumn = new TableColumn(propertiesTable, SWT.NONE);
		nameColumn.setText(Messages.getString("BeanPropertiesWizardPage.NAME_COLUMN_TITLE")); //$NON-NLS-1$
		nameColumn.setWidth(150);

		TableColumn valueColumn = new TableColumn(propertiesTable, SWT.NONE);
		valueColumn.setText(Messages.getString("BeanPropertiesWizardPage.VALUE/REF_COLUMN_TITLE")); //$NON-NLS-1$
		valueColumn.setWidth(250);

		createButtons(PROPERTY, sectionComposite, BeansSchemaConstants.ELEM_PROPERTY, propertiesTable);
	}

	private Composite createSection(final Composite container, String title, boolean expanded) {
		final ExpandableComposite section = new FormToolkit(getShell().getDisplay()).createExpandableComposite(
				container, ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR | ExpandableComposite.COMPACT);
		section.setText(title);
		GridData sectionData = new GridData(SWT.FILL, SWT.TOP, true, false);
		section.clientVerticalSpacing = 0;
		section.setLayoutData(sectionData);
		section.setBackground(container.getBackground());

		Composite sectionComposite = new Composite(section, SWT.NONE);
		section.setClient(sectionComposite);

		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		sectionComposite.setLayout(layout);

		section.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				getShell().pack();
			}
		});

		section.setExpanded(expanded);

		return sectionComposite;
	}

	private Table createTable(Composite container) {
		GridData tableData = new GridData(SWT.FILL, SWT.FILL, true, true);
		tableData.heightHint = 100;

		Table table = new Table(container, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		table.setLayoutData(tableData);

		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		return table;
	}

	@Override
	public void dispose() {
		if (errorImages != null) {
			errorImages[0].dispose();
			errorImages[1].dispose();
			warningImages[0].dispose();
			warningImages[1].dispose();
		}
	}

	private void editSelectedElementItem(int kind, Table table) {
		TableItem[] selectedItems = table.getSelection();
		if (selectedItems.length == 1) {
			TableItem item = selectedItems[0];
			IDOMElement element = (IDOMElement) item.getData();
			int numProblemPreEdit = validateElement(kind, element, item);

			String title = null;
			if (kind == PROPERTY) {
				title = Messages.getString("BeanPropertiesWizardPage.EDIT_PROPERTY_DIALOG_TITLE"); //$NON-NLS-1$
				definedProperties.remove(element.getAttribute(BeansSchemaConstants.ATTR_NAME));
			}
			else if (kind == CONSTRUCTOR_ARG) {
				title = Messages.getString("BeanPropertiesWizardPage.EDIT_CONSTRUCTOR_ARG_DIALOG_TITLE"); //$NON-NLS-1$
			}
			BeanChildDialog dialog = createDialog(kind, element, title, false);

			int status = dialog.open();
			if (status == Dialog.OK) {
				item.setText(getElementItemText(kind, element));
				int numProblemPostEdit = validateElement(kind, element, item);

				if (kind == PROPERTY) {
					definedProperties.add(element.getAttribute(BeansSchemaConstants.ATTR_NAME));
					propertyProblemCounter += (numProblemPostEdit - numProblemPreEdit);
				}
				else {
					constArgProblemCounter += (numProblemPostEdit - numProblemPreEdit);
				}
			}
		}

		updateMessage();
	}

	private void fillConstructorDropdownBox() {
		constructorCombo.refresh(true);
	}

	private String getElementAttributeText(IDOMElement element, String attributeName) {
		if (element.hasAttribute(attributeName)) {
			return element.getAttribute(attributeName);
		}
		else {
			return Messages.getString("BeanPropertiesWizardPage.EMPTY_CELL_LABEL"); //$NON-NLS-1$
		}
	}

	private String[] getElementItemText(int kind, IDOMElement element) {
		String name = getElementAttributeText(element, BeansSchemaConstants.ATTR_NAME);
		String valueRef = getElementAttributeText(element, BeansSchemaConstants.ATTR_REF);
		if (valueRef.equals(Messages.getString("BeanPropertiesWizardPage.EMPTY_CELL_LABEL"))) { //$NON-NLS-1$
			valueRef = getElementAttributeText(element, BeansSchemaConstants.ATTR_VALUE);
		}

		if (kind == PROPERTY) {
			return new String[] { name, valueRef };
		}
		if (kind == CONSTRUCTOR_ARG) {
			return new String[] { valueRef };
		}
		return null;
	}

	@Override
	public boolean isPageComplete() {
		if (propertyProblemCounter < 0 || constArgProblemCounter < 0) {
			validateElements();
		}
		return BeanWizard.getIgnoreError() || (propertyProblemCounter == 0 && constArgProblemCounter == 0);
	}

	private void populateValues() {
		IDOMElement newBean = wizard.getNewBean();
		if (newBean == null) {
			return;
		}

		NodeList childNodes = newBean.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node childNode = childNodes.item(i);

			if (childNode instanceof IDOMElement) {
				IDOMElement domNode = (IDOMElement) childNode;

				String nodeName = domNode.getNodeName();
				if (nodeName != null) {
					if (nodeName.equals(BeansSchemaConstants.ELEM_PROPERTY)) {
						createElementItem(PROPERTY, domNode);
					}
					else if (nodeName.equals(BeansSchemaConstants.ELEM_CONSTRUCTOR_ARG)) {
						createElementItem(CONSTRUCTOR_ARG, domNode);
					}
				}
			}
		}

		Object[] elements = comboContentProvider.getElements(this);
		int itemCount = constructorArgsTable.getItemCount();
		for (Object element : elements) {
			if (element instanceof IMethod) {
				IMethod constructor = (IMethod) element;
				try {
					String[] parameterNames = constructor.getParameterNames();
					if (parameterNames != null && parameterNames.length == itemCount) {
						constructorCombo.setSelection(new StructuredSelection(constructor));
					}
				}
				catch (JavaModelException e) {
					StatusHandler.log(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID,
							"Failed to populate constructor combo.", e));
				}
			}
		}
	}

	private void removeSelected(int kind, Table table) {
		TableItem[] items = table.getItems();
		int[] indices = table.getSelectionIndices();
		IDOMElement newBean = wizard.getNewBean();

		for (int index : indices) {
			TableItem item = items[index];
			Object data = item.getData();
			if (data != null && data instanceof IDOMElement) {
				IDOMElement element = (IDOMElement) data;
				if (kind == PROPERTY) {
					propertyProblemCounter -= validateElement(kind, element, item);
				}
				else {
					constArgProblemCounter -= validateElement(kind, element, item);
				}
				newBean.removeChild(element);
				if (kind == PROPERTY) {
					definedProperties.remove(element.getAttribute(BeansSchemaConstants.ATTR_NAME));
				}
			}
		}

		table.remove(indices);
		updateMessage();
	}

	public void resetProblemCounter() {
		propertyProblemCounter = -1;
		constArgProblemCounter = -1;
	}

	@Override
	public void updateMessage() {
		String text = "error";
		if (BeanWizard.getIgnoreError()) {
			text = "warning";
		}

		if (propertyProblemCounter > 0) {
			if (propertyProblemCounter == 1) {
				setDialogMessage("There is 1 " + text + " in the Properties section.");
			}
			else {
				setDialogMessage("There are " + propertyProblemCounter + " " + text + "s in the Properties section.");
			}
		}
		else if (constArgProblemCounter > 0) {
			if (constArgProblemCounter == 1) {
				setDialogMessage("There is 1 " + text + " in the Constructor Args section.");
			}
			else {
				setDialogMessage("There are " + constArgProblemCounter + " " + text
						+ "s in the Constructor Args section.");
			}
		}
		else {
			setMessage(DEFAULT_MESSAGE);
		}

		if (isCurrentPage()) {
			getWizard().getContainer().updateButtons();
		}
	}

	private boolean validateAttribute(String attributeValue, String attributeName, IDOMElement property,
			TableItem item, int kind) {
		boolean errorFound = false;

		Image[] images;
		if (BeanWizard.getIgnoreError()) {
			images = warningImages;
		}
		else {
			images = errorImages;
		}

		int index = 0;

		if (attributeValue != null) {
			WizardContentAssistConverter contentAssistConverter = new WizardContentAssistConverter(property, property
					.getAttributeNode(attributeName), wizard.getBeanFile(), wizard.getOriginalDocument());

			if (attributeName.equals(BeansSchemaConstants.ATTR_NAME)) {
				errorFound = contentAssistConverter.getPropertyProposals(attributeValue, true).isEmpty();
				if (errorFound) {
					item.setImage(index, images[index]);
				}
				else {
					item.setImage(index, BeansUIImages.getImage(BeansUIImages.IMG_OBJS_PROPERTY));
				}
			}
			else if (attributeName.equals(BeansSchemaConstants.ATTR_REF)) {
				if (kind == PROPERTY) {
					index = 1;
				}
				if (attributeValue.length() == 0) {
					item.setImage(index, null);
				}
				else {
					errorFound = contentAssistConverter.getReferenceableBeanDescriptions(attributeValue, true)
							.isEmpty();
					if (errorFound) {
						item.setImage(index, images[index]);
					}
					else {
						item.setImage(index, null);
					}
				}
			}
		}

		if (kind == PROPERTY) {
			propertiesTable.update();
		}
		return errorFound;
	}

	private int validateElement(int kind, IDOMElement property, TableItem item) {
		int counter = 0;

		if (kind == PROPERTY) {
			if (validateAttribute(property.getAttribute(BeansSchemaConstants.ATTR_NAME),
					BeansSchemaConstants.ATTR_NAME, property, item, kind)) {
				counter++;
			}
		}

		if (validateAttribute(property.getAttribute(BeansSchemaConstants.ATTR_REF), BeansSchemaConstants.ATTR_REF,
				property, item, kind)) {
			counter++;
		}
		return counter;
	}

	public void validateElements() {
		fillConstructorDropdownBox();

		propertyProblemCounter = 0;
		TableItem[] items = propertiesTable.getItems();
		for (TableItem item : items) {
			Object data = item.getData();
			if (data instanceof IDOMElement) {
				IDOMElement property = (IDOMElement) data;
				if (property.getNodeName().equals(BeansSchemaConstants.ELEM_PROPERTY)) {
					propertyProblemCounter += validateElement(PROPERTY, property, item);
				}
			}
		}

		constArgProblemCounter = 0;
		items = constructorArgsTable.getItems();
		for (TableItem item : items) {
			Object data = item.getData();
			if (data instanceof IDOMElement) {
				IDOMElement constructorArg = (IDOMElement) data;
				if (constructorArg.getNodeName().equals(BeansSchemaConstants.ELEM_CONSTRUCTOR_ARG)) {
					constArgProblemCounter += validateElement(CONSTRUCTOR_ARG, constructorArg, item);
				}
			}
		}

		ignoreErrorButton.setSelection(BeanWizard.getIgnoreError());

		updateMessage();
	}
}
