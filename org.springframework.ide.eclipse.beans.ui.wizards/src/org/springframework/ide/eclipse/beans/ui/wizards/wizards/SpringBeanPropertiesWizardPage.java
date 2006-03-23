package org.springframework.ide.eclipse.beans.ui.wizards.wizards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.springframework.ide.eclipse.beans.core.internal.Introspector;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.ui.wizards.model.IdRefModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.model.ListModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.model.MapEntryModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.model.MapModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.model.PropModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.model.PropertyModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.model.PropsModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.model.RefModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.model.SetModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.model.ValueModelItem;
import org.springframework.ide.eclipse.core.StringUtils;
import org.springframework.ide.eclipse.core.ui.dialogs.input.MultipleInputDialog;
import org.springframework.ide.eclipse.core.ui.dialogs.input.SimpleComboDialog;
import org.springframework.ide.eclipse.core.ui.dialogs.input.SimpleInputDialog;
import org.springframework.ide.eclipse.core.ui.fields.IDialogField;
import org.springframework.ide.eclipse.core.ui.fields.ListDialogField;
import org.springframework.ide.eclipse.core.ui.fields.SelectionButtonDialogField;
import org.springframework.ide.eclipse.core.ui.fields.TreeListDialogField;
import org.springframework.ide.eclipse.core.ui.treemodel.IModelItem;
import org.springframework.ide.eclipse.core.ui.treemodel.RootModelItem;
import org.springframework.ide.eclipse.core.ui.wizards.AbstractWizardCustomPage;
import org.springframework.ide.eclipse.core.ui.wizards.StatusInfo;

/**
 * @author pagregoire
 * 
 */
public class SpringBeanPropertiesWizardPage extends AbstractWizardCustomPage {
	private final class RemoveItemAction extends Action {
		private final List elements;

		private RemoveItemAction(List elements) {
			super("Remove");
			this.elements = elements;
		}

		public void run() {
			for (Iterator it = elements.iterator(); it.hasNext();) {
				IModelItem modelItem = (IModelItem) it.next();
				if (modelItem.getParent() != null) {
					modelItem.getParent().removeChild(modelItem.getUID());
				}
			}
		}
	}

	private final class EditIdRefAction extends Action {
		private final List beans;

		private final Object element;

		private EditIdRefAction(List beans, Object element) {
			super("Edit Idref");
			this.beans = beans;
			this.element = element;
		}

		public void run() {
			String[] referenceableBeans = new String[beans.size()];
			int counter = 0;
			for (Iterator it = beans.iterator(); it.hasNext();) {
				referenceableBeans[counter++] = ((IBean) it.next()).getElementName();
			}
			SimpleComboDialog inputDialog = new SimpleComboDialog(getShell(), "Input a bean id", "Referenced bean Id", null, "Select a beanId :", referenceableBeans);
			inputDialog.open();
			IModelItem formerIdRef = (IModelItem) element;
			IModelItem parentModelItem = formerIdRef.getParent();
			parentModelItem.removeChild(formerIdRef.getUID());
			parentModelItem.addChild(new IdRefModelItem(inputDialog.getResult()));
		}
	}

	private final class EditBeanRefAction extends Action {
		private final List beans;

		private final Object element;

		private EditBeanRefAction(List beans, Object element) {
			super("Edit Bean reference");
			this.beans = beans;
			this.element = element;
		}

		public void run() {
			String[] referenceableBeans = new String[beans.size()];
			int counter = 0;
			for (Iterator it = beans.iterator(); it.hasNext();) {
				referenceableBeans[counter++] = ((IBean) it.next()).getElementName();
			}
			SimpleComboDialog inputDialog = new SimpleComboDialog(getShell(), "Input a bean id", "Referenced bean Id", null, "Select a beanId :", referenceableBeans);
			inputDialog.open();
			IModelItem formerRef = (IModelItem) element;
			IModelItem parentModelItem = formerRef.getParent();
			parentModelItem.removeChild(formerRef.getUID());
			parentModelItem.addChild(new RefModelItem(inputDialog.getResult()));
		}
	}

	private final class EditValueAction extends Action {
		private final Object element;

		private EditValueAction(Object element) {
			super("Edit value");
			this.element = element;
		}

		public void run() {
			SimpleInputDialog inputDialog = new SimpleInputDialog(getShell(), "Input a value", "Value for the property", null, "Input a value :");
			inputDialog.open();
			IModelItem formerValue = (IModelItem) element;
			IModelItem parentModelItem = formerValue.getParent();
			parentModelItem.removeChild(formerValue.getUID());
			parentModelItem.addChild(new ValueModelItem(inputDialog.getResult()));
		}
	}

	private final class InjectMapEntryAction extends Action {
		private final Object element;

		private InjectMapEntryAction(Object element) {
			super("Add an entry to the map");
			this.element = element;
		}

		public void run() {
			SimpleInputDialog inputDialog = new SimpleInputDialog(getShell(), "Input a map entry's key", "Value of the key", null, "Input a map entry's key :");
			inputDialog.open();
			((IModelItem) element).addChild(new MapEntryModelItem(inputDialog.getResult()));
		}
	}

	private final class InjectPropAction extends Action {
		private final Object element;

		private InjectPropAction(Object element) {
			super("Add a property");
			this.element = element;
		}

		public void run() {
			MultipleInputDialog inputDialog = new MultipleInputDialog(getShell(), "Input a prop", "Input the prop", null, "Input the prop :", new String[] { "key : ", "value : " });
			inputDialog.open();

			((IModelItem) element).addChild(new PropModelItem((String) inputDialog.getResults().get("key : "), (String) inputDialog.getResults().get("value : ")));
		}
	}

	private final class InjectPropsAction extends Action {
		private final List elements;

		private InjectPropsAction(List elements) {
			super("Inject properties");
			this.elements = elements;
		}

		public void run() {
			for (Iterator it = elements.iterator(); it.hasNext();) {
				((IModelItem) it.next()).addChild(new PropsModelItem());
			}
		}
	}

	private final class InjectMapAction extends Action {
		private final Object element;

		private InjectMapAction(Object element) {
			super("Inject a Map");
			this.element = element;
		}

		public void run() {
			((IModelItem) element).addChild(new MapModelItem());
		}
	}

	private final class InjectSetAction extends Action {
		private final Object element;

		private InjectSetAction(Object element) {
			super("Inject a Set");
			this.element = element;
		}

		public void run() {
			((IModelItem) element).addChild(new SetModelItem());
		}
	}

	private final class InjectListAction extends Action {
		private final Object element;

		private InjectListAction(Object element) {
			super("Inject a List");
			this.element = element;
		}

		public void run() {
			((IModelItem) element).addChild(new ListModelItem());
		}
	}

	private final class InjectIdRefAction extends Action {
		private final List beans;

		private final Object element;

		private InjectIdRefAction(List beans, Object element) {
			super("Inject an Idref");
			this.beans = beans;
			this.element = element;
		}

		public void run() {
			String[] referenceableBeans = new String[beans.size()];
			int counter = 0;
			for (Iterator it = beans.iterator(); it.hasNext();) {
				referenceableBeans[counter++] = ((IBean) it.next()).getElementName();
			}
			SimpleComboDialog inputDialog = new SimpleComboDialog(getShell(), "Input a bean id", "IdRef bean Id", null, "Select a beanId :", referenceableBeans);
			inputDialog.open();
			((IModelItem) element).addChild(new IdRefModelItem(inputDialog.getResult()));
		}
	}

	private final class InjectBeanReferenceAction extends Action {
		private final List beans;

		private final Object element;

		private InjectBeanReferenceAction(List beans, Object element) {
			super("Inject a bean reference");
			this.beans = beans;
			this.element = element;
		}

		public void run() {
			String[] referenceableBeans = new String[beans.size()];
			int counter = 0;
			for (Iterator it = beans.iterator(); it.hasNext();) {
				referenceableBeans[counter++] = ((IBean) it.next()).getElementName();
			}
			SimpleComboDialog inputDialog = new SimpleComboDialog(getShell(), "Input a bean id", "Referenced bean Id", null, "Select a beanId :", referenceableBeans);
			inputDialog.open();
			((IModelItem) element).addChild(new RefModelItem(inputDialog.getResult()));
		}
	}

	private final class InjectValueAction extends Action {
		private final Object element;

		private InjectValueAction(Object element) {
			super("Inject a value");
			this.element = element;
		}

		public void run() {
			SimpleInputDialog inputDialog = new SimpleInputDialog(getShell(), "Input a value", "Value for the property", null, "Input a value :");
			inputDialog.open();
			((IModelItem) element).addChild(new ValueModelItem(inputDialog.getResult()));
		}
	}

	private final class AddPropertyListener implements MouseListener {
		public void mouseUp(MouseEvent e) {
		}

		public void mouseDown(MouseEvent e) {
			List properties = getBeanEligibleProperties();
			Map index = new HashMap();
			int counter = 0;
			String[] chooseableProperties = new String[properties.size()];
			for (Iterator it = properties.iterator(); it.hasNext();) {
				PropertyModelItem next = (PropertyModelItem) it.next();
				String shownLabel = next.getName() + " <" + (next.isPrimitive() ? next.getPrimitiveTypeName() : next.getType().getElementName()) + ">";
				chooseableProperties[counter++] = shownLabel;
				index.put(shownLabel, next);
			}
			SimpleComboDialog comboDialog = new SimpleComboDialog(getShell(), "Select a property", "Select a property", null, "Select a property :", chooseableProperties);
			comboDialog.open();
			String result = comboDialog.getResult();
			RootModelItem.getInstance(getFirstPage().selectedType.getFullyQualifiedName()).addChild((IModelItem) index.get(result));
		}

		public void mouseDoubleClick(MouseEvent e) {
		}
	}

	private final class ContextMenuListener implements IMenuListener {
		public void menuAboutToShow(IMenuManager mgr) {
			fillContextMenu(mgr);
		}
	}

	private final class DiscoverPropertiesListener implements MouseListener {
		public void mouseUp(MouseEvent e) {
		}

		public void mouseDown(MouseEvent e) {
			for (Iterator it = getBeanEligibleProperties().iterator(); it.hasNext();) {
				RootModelItem.getInstance(getFirstPage().selectedType.getFullyQualifiedName()).addChild((IModelItem) it.next());
			}
		}

		public void mouseDoubleClick(MouseEvent e) {
		}
	}

	private static final String WIZARD_ID = SpringBeanPropertiesWizardPage.class.getName();

	/**
	 * Logger for this class
	 */

	private SelectionButtonDialogField injectCheckBoxField;

	private TreeListDialogField propertiesDialogField;

	private Composite propertiesSectionClient;

	/**
	 * @param selection
	 * @param wizardId
	 * @param title
	 * @param description
	 */
	public SpringBeanPropertiesWizardPage() {
		super(WIZARD_ID, "Inject dependencies in a Spring Bean's properties", "This wizard page helps you to inject dependencies in this bean's properties.");
	}

	protected void describe() {
		Section propertiesSection = getWizardFormToolkit().createSection(getWizardForm().getBody(), Section.TITLE_BAR | Section.EXPANDED);
		GridLayout propertiesSectionLayout = new GridLayout(1, false);
		propertiesSection.setLayout(propertiesSectionLayout);
		propertiesSection.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		propertiesSection.setText("What properties should receive injected values :");
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 4;
		layout.makeColumnsEqualWidth = false;
		propertiesSectionClient = getWizardFormToolkit().createComposite(propertiesSection);
		propertiesSectionClient.setLayout(layout);
		propertiesSectionClient.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		injectCheckBoxField = new SelectionButtonDialogField(getWizardFormToolkit(), SWT.CHECK);
		injectCheckBoxField.setLabelText("I want to inject something into my bean.");
		injectCheckBoxField.setDialogFieldListener(getWizardAdapter());
		injectCheckBoxField.setEventDispatchingEnabled(true);
		injectCheckBoxField.doFillIntoTable(propertiesSectionClient, 4);

		Button addPropertyButton = getWizardFormToolkit().createButton(propertiesSectionClient, "Add an injectable property", SWT.FLAT);
		TableWrapData td = new TableWrapData();
		td.colspan = 2;
		addPropertyButton.setLayoutData(td);
		addPropertyButton.addMouseListener(new AddPropertyListener());

		Button discoverPropertiesButton = getWizardFormToolkit().createButton(propertiesSectionClient, "Discover all properties", SWT.FLAT);
		TableWrapData td2 = new TableWrapData();
		td2.colspan = 2;
		discoverPropertiesButton.setLayoutData(td2);
		discoverPropertiesButton.addMouseListener(new DiscoverPropertiesListener());

		propertiesDialogField = new TreeListDialogField(getWizardFormToolkit(), getWizardAdapter(), new String[] {}, new PropertiesListLabelProvider());
		propertiesDialogField.setLabelText("Properties :");
		propertiesDialogField.setDialogFieldListener(getWizardAdapter());
		propertiesDialogField.doFillIntoTable(propertiesSectionClient, 4);
		MenuManager menuManager = new MenuManager();
		propertiesDialogField.getTreeViewer().getTree().setMenu(menuManager.createContextMenu(propertiesDialogField.getTreeViewer().getTree()));
		menuManager.addMenuListener(new ContextMenuListener());

		propertiesSection.setClient(propertiesSectionClient);
		getFirstPage().addPageCompleteListener(new IPageCompleteListener() {
			public void pageComplete(AbstractWizardCustomPage wizardPage) {
				propertiesDialogField.getTreeViewer().setContentProvider(new PropertiesListContentProvider(propertiesDialogField, getFirstPage().selectedType.getFullyQualifiedName()));
			}
		});
	}

	private void fillContextMenu(IMenuManager mgr) {
		boolean remove = false;
		boolean addValue = false;
		boolean editValue = false;
		boolean addRef = false;
		boolean editRef = false;
		boolean addIdRef = false;
		boolean editIdRef = false;
		boolean addList = false;
		boolean addSet = false;
		boolean addMap = false;
		boolean addMapEntry = false;
		boolean addMapKey = false;
		boolean addProps = false;
		boolean addProp = false;
		final List accessibleBeans = getAccessibleBeans();
		mgr.removeAll();
		final List allSelectedElements = this.propertiesDialogField.getSelectedElements();
		final Object firstElement = allSelectedElements.get(0);
		remove = true;
		if (allSelectedElements.size() == 1) {
			if (firstElement instanceof PropertyModelItem || firstElement instanceof ListModelItem || firstElement instanceof SetModelItem || firstElement instanceof MapEntryModelItem) {
				IModelItem modelItem = (IModelItem) firstElement;
				if (!modelItem.hasChildren()) {
					addValue = true;
					if (accessibleBeans.size() >= 1) {
						addRef = true;
						addIdRef = true;
					}
					if (modelItem instanceof PropertyModelItem) {
						PropertyModelItem propertyModelItem = (PropertyModelItem) firstElement;
						if (!(propertyModelItem.isPrimitive())) {
							try {
								ITypeHierarchy superTypeHierarchy = propertyModelItem.getType().newSupertypeHierarchy(new NullProgressMonitor());
								if (superTypeHierarchy.contains(BeansModelUtils.getJavaType(getFirstPage().selectedProject.getProject(), Set.class.getName()))) {
									addSet = true;
									addValue = false;
									addRef = false;
									addIdRef = false;
								}
								if (superTypeHierarchy.contains(BeansModelUtils.getJavaType(getFirstPage().selectedProject.getProject(), List.class.getName()))) {
									addList = true;
									addValue = false;
									addRef = false;
									addIdRef = false;
								}
								if (superTypeHierarchy.contains(BeansModelUtils.getJavaType(getFirstPage().selectedProject.getProject(), Map.class.getName()))) {
									addMap = true;
									addValue = false;
									addRef = false;
									addIdRef = false;
								}
								if (superTypeHierarchy.contains(BeansModelUtils.getJavaType(getFirstPage().selectedProject.getProject(), Properties.class.getName()))) {
									addProps = true;
									addMap = false;// because Properties is a dirty extension of Map
									addValue = false;
									addRef = false;
									addIdRef = false;
								}

							} catch (JavaModelException e) {
								// do nothing
							}
						}
					} else {
						addList = true;
						addMap = true;
						addSet = true;
						addProps = true;
					}
				}
			} else if (firstElement instanceof ValueModelItem) {
				editValue = true;
			} else if (firstElement instanceof RefModelItem) {
				editRef = true;
			} else if (firstElement instanceof IdRefModelItem) {
				editIdRef = true;
			} else if (firstElement instanceof MapModelItem) {
				addMapEntry = true;
			} else if (firstElement instanceof PropsModelItem) {
				addProp = true;
			}
		}

		if (addValue) {
			mgr.add(new InjectValueAction(firstElement));
		}
		if (addRef) {
			mgr.add(new InjectBeanReferenceAction(accessibleBeans, firstElement));
		}
		if (addIdRef) {
			mgr.add(new InjectIdRefAction(accessibleBeans, firstElement));
		}
		if (addList) {
			mgr.add(new InjectListAction(firstElement));
		}
		if (addSet) {
			mgr.add(new InjectSetAction(firstElement));
		}
		if (addMap) {
			mgr.add(new InjectMapAction(firstElement));
		}
		if (addProps) {
			mgr.add(new InjectPropsAction(allSelectedElements));
		}
		if (addProp) {
			mgr.add(new InjectPropAction(firstElement));
		}
		if (addMapEntry) {
			mgr.add(new InjectMapEntryAction(firstElement));
		}
		if (editValue) {
			mgr.add(new EditValueAction(firstElement));
		}
		if (editRef) {
			mgr.add(new EditBeanRefAction(accessibleBeans, firstElement));
		}
		if (editIdRef) {
			mgr.add(new EditIdRefAction(accessibleBeans, firstElement));
		}
		if (remove) {
			mgr.add(new RemoveItemAction(allSelectedElements));
		}
	}

	private SpringBeanBasicWizardPage getFirstPage() {
		return ((SpringBeanBasicWizardPage) getWizard().getPage(SpringBeanBasicWizardPage.WIZARD_ID));
	}

	private void setListEnabled(boolean enabled) {
		if (propertiesDialogField != null) {
			propertiesDialogField.setEnabled(enabled);
		}
	}

	private static String resolveClassName(String className, IType type) {
		try {
			String[][] fullInter = type.resolveType(className);
			if (fullInter != null && fullInter.length > 0) {
				return fullInter[0][0] + "." + fullInter[0][1];
			}
		} catch (JavaModelException e) {
		}

		return className;
	}

	private List getAccessibleBeans() {
		List result = new ArrayList();
		List configSets = BeansModelUtils.getConfigSets(getFirstPage().selectedConfigFile);
		if (configSets.size() > 0) {
			for (Iterator it = configSets.iterator(); it.hasNext();) {
				result.addAll(((IBeansConfigSet) it.next()).getBeans());
			}
		} else {
			result.addAll(getFirstPage().selectedConfigFile.getBeans());
		}
		return result;
	}

	private List getBeanEligibleProperties() {
		List result = new ArrayList();
		try {
			IType type = getFirstPage().selectedType;
			Collection propertiesMethods = Introspector.findWritableProperties(type, "");
			for (Iterator it = propertiesMethods.iterator(); it.hasNext();) {
				IMethod method = (IMethod) it.next();
				String propertyName = StringUtils.uncapitalize(method.getElementName().substring(3));
				PropertyModelItem propertyModelItem = new PropertyModelItem(propertyName);
				IField field = type.getField(propertyName);
				IType propertyType = null;
				try {
					String propertyTypeString = Signature.toString(field.getTypeSignature()).replace('$', '.');
					String resolvedPropertyType = resolveClassName(propertyTypeString, type);
					propertyType = BeansModelUtils.getJavaType(type.getJavaProject().getProject(), resolvedPropertyType);
					if (propertyType == null) {
						propertyModelItem.setPrimitive(true);
						propertyModelItem.setPrimitiveTypeName(resolvedPropertyType);
					}
				} catch (IllegalArgumentException e) {
					// do Nothing
				} catch (JavaModelException e) {
					// do Nothing
				}
				propertyModelItem.setType(propertyType);
				result.add(propertyModelItem);
			}
		} catch (Throwable e) {
		}
		return result;
	} /*
		 * (non-Javadoc)
		 * 
		 * @see ec.ep.dit.isp.foundry.eclipse.plugins.webservices.wizards.AbstractWizardPage#initialize()
		 */

	protected void initialize() {
		injectCheckBoxField.setSelection(false);
		setListEnabled(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ec.ep.dit.isp.foundry.eclipse.plugins.webservices.wizards.AbstractWizardPage#touch()
	 */
	protected void touch() {
		updateStatus(validate());
	}

	protected IStatus validate() {
		StatusInfo status = new StatusInfo();
		return status;
	}

	/**
	 * @see ec.ep.dit.isp.foundry.eclipse.platform.utils.wizards.AbstractWizardPage#handleSelectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	protected void handleSelectionChanged(IWorkbenchPart part, ISelection selection) {
		touch();
	}

	/**
	 * @see ec.ep.dit.isp.foundry.eclipse.plugins.webservices.wizards.AbstractWizardPage#handleDialogFieldChanged()
	 */
	protected void handleDialogFieldChanged(IDialogField field) {
		if (injectCheckBoxField != null && field.equals(injectCheckBoxField)) {
			setListEnabled(injectCheckBoxField.isSelected());
		}
		touch();
	}

	/**
	 * @see ec.ep.dit.isp.foundry.eclipse.plugins.webservices.wizards.AbstractWizardPage#handleCustomButtonPressed(ListDialogField, int)
	 */
	protected void handleCustomButtonPressed(IDialogField field, int buttonIndex) {
		touch();
	}

	/**
	 * @see ec.ep.dit.isp.foundry.eclipse.plugins.webservices.wizards.AbstractWizardPage#handleDoubleClicked(ListDialogField)
	 */
	protected void handleDoubleClicked(IDialogField field) {
		touch();
	}

	protected void handleChangeControlPressed(IDialogField field) {
		touch();
	}

	/**
	 * @see ec.ep.dit.isp.foundry.eclipse.plugins.webservices.wizards.AbstractWizardPage#handleSelectionChanged(ListDialogField)
	 */
	protected void handleSelectionChanged(IDialogField field) {
		touch();
	}

	protected void handleKeyPressed(TreeListDialogField field, KeyEvent event) {
		touch();
	}

	public boolean getInjectionState() {
		return injectCheckBoxField.isSelected();
	}

	public List getInjectableProperties() {
		return RootModelItem.getInstance(getFirstPage().selectedType.getFullyQualifiedName()).getChildren();
	}

}