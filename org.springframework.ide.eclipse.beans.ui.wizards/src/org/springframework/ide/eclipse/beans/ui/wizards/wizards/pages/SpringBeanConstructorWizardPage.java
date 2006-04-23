package org.springframework.ide.eclipse.beans.ui.wizards.wizards.pages;

import java.util.ArrayList;
import java.util.Collection;
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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.springframework.ide.eclipse.beans.core.internal.Introspector;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.ui.wizards.wizards.model.ConstructorArgModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.wizards.model.ConstructorModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.wizards.model.IdRefModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.wizards.model.ListModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.wizards.model.MapEntryModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.wizards.model.MapModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.wizards.model.PropModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.wizards.model.PropertyModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.wizards.model.PropsModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.wizards.model.RefModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.wizards.model.SetModelItem;
import org.springframework.ide.eclipse.beans.ui.wizards.wizards.model.ValueModelItem;
import org.springframework.ide.eclipse.core.StringUtils;
import org.springframework.ide.eclipse.core.ui.dialogs.input.MultipleInputDialog;
import org.springframework.ide.eclipse.core.ui.dialogs.input.SimpleComboDialog;
import org.springframework.ide.eclipse.core.ui.dialogs.input.SimpleInputDialog;
import org.springframework.ide.eclipse.core.ui.dialogs.message.WarningDialog;
import org.springframework.ide.eclipse.core.ui.fields.ComboDialogField;
import org.springframework.ide.eclipse.core.ui.fields.IDialogField;
import org.springframework.ide.eclipse.core.ui.fields.ListDialogField;
import org.springframework.ide.eclipse.core.ui.fields.TreeListDialogField;
import org.springframework.ide.eclipse.core.ui.treemodel.IModelItem;
import org.springframework.ide.eclipse.core.ui.treemodel.RootModelItem;
import org.springframework.ide.eclipse.core.ui.wizards.AbstractWizardCustomPage;
import org.springframework.ide.eclipse.core.ui.wizards.StatusInfo;

/**
 * @author pagregoire
 * 
 */
public class SpringBeanConstructorWizardPage extends AbstractWizardCustomPage {
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
				referenceableBeans[counter++] = ((IBean) it.next())
						.getElementName();
			}
			SimpleComboDialog inputDialog = new SimpleComboDialog(getShell(),
					"Input a bean id", "Referenced bean Id", null,
					"Select a beanId :", referenceableBeans);
			if (inputDialog.open() == IDialogConstants.OK_ID) {
				if (!(inputDialog.getResult().equals(""))) {
					IModelItem formerIdRef = (IModelItem) element;
					IModelItem parentModelItem = formerIdRef.getParent();
					parentModelItem.removeChild(formerIdRef.getUID());
					parentModelItem.addChild(new IdRefModelItem(inputDialog
							.getResult()));
				}
			}
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
				referenceableBeans[counter++] = ((IBean) it.next())
						.getElementName();
			}
			SimpleComboDialog inputDialog = new SimpleComboDialog(getShell(),
					"Input a bean id", "Referenced bean Id", null,
					"Select a beanId :", referenceableBeans);
			if (inputDialog.open() == IDialogConstants.OK_ID) {
				if (!(inputDialog.getResult().equals(""))) {
					IModelItem formerRef = (IModelItem) element;
					IModelItem parentModelItem = formerRef.getParent();
					parentModelItem.removeChild(formerRef.getUID());
					parentModelItem.addChild(new RefModelItem(inputDialog
							.getResult()));
				}
			}
		}
	}

	private final class EditValueAction extends Action {
		private final Object element;

		private EditValueAction(Object element) {
			super("Edit value");
			this.element = element;
		}

		public void run() {
			SimpleInputDialog inputDialog = new SimpleInputDialog(getShell(),
					"Input a value", "Input a value", null, "value :");
			if (inputDialog.open() == IDialogConstants.OK_ID) {
				if (!(inputDialog.getResult().equals(""))) {
					IModelItem formerValue = (IModelItem) element;
					IModelItem parentModelItem = formerValue.getParent();
					parentModelItem.removeChild(formerValue.getUID());
					parentModelItem.addChild(new ValueModelItem(inputDialog
							.getResult()));
				}
			}
		}
	}

	private final class InjectMapEntryAction extends Action {
		private final Object element;

		private InjectMapEntryAction(Object element) {
			super("Add an entry to the map");
			this.element = element;
		}

		public void run() {
			SimpleInputDialog inputDialog = new SimpleInputDialog(getShell(),
					"Input a map entry's key", "Input a map entry's key", null,
					"key :");
			if (inputDialog.open() == IDialogConstants.OK_ID) {
				if (!(inputDialog.getResult().equals(""))) {
					((IModelItem) element).addChild(new MapEntryModelItem(
							inputDialog.getResult()));
				}
			}
		}
	}

	private final class InjectPropAction extends Action {
		private final Object element;

		private InjectPropAction(Object element) {
			super("Add a property");
			this.element = element;
		}

		public void run() {
			MultipleInputDialog inputDialog = new MultipleInputDialog(
					getShell(), "Input a prop", "Input a key/value pair", null,
					null, new String[] { "key : ", "value : " });
			if (inputDialog.open() == IDialogConstants.OK_ID) {
				if (!(inputDialog.getResults().get("key : ").equals(""))) {
					PropsModelItem propsModelItem = (PropsModelItem) element;
					if (propsModelItem.hasChild((String) inputDialog
							.getResults().get("key : "))) {
						WarningDialog warningDialog = new WarningDialog(
								"Key is already used",
								"The key \""
										+ inputDialog.getResults()
												.get("key : ")
										+ "\" is already used in this instance of Properties.");
						warningDialog.open();

					} else {
						propsModelItem
								.addChild(new PropModelItem(
										(String) inputDialog.getResults().get(
												"key : "), (String) inputDialog
												.getResults().get("value : ")));
					}
				}
			}
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
				referenceableBeans[counter++] = ((IBean) it.next())
						.getElementName();
			}
			SimpleComboDialog inputDialog = new SimpleComboDialog(getShell(),
					"Input a bean id", "IdRef bean Id", null,
					"Select a beanId :", referenceableBeans);
			if (inputDialog.open() == IDialogConstants.OK_ID) {
				if (!(inputDialog.getResult().equals(""))) {
					((IModelItem) element).addChild(new IdRefModelItem(
							inputDialog.getResult()));
				}
			}
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
				referenceableBeans[counter++] = ((IBean) it.next())
						.getElementName();
			}
			SimpleComboDialog inputDialog = new SimpleComboDialog(getShell(),
					"Input a bean id", "Referenced bean Id", null,
					"Select a beanId :", referenceableBeans);
			if (inputDialog.open() == IDialogConstants.OK_ID) {
				if (!(inputDialog.getResult().equals(""))) {
					((IModelItem) element).addChild(new RefModelItem(
							inputDialog.getResult()));
				}
			}
		}
	}

	private final class InjectValueAction extends Action {
		private final Object element;

		private InjectValueAction(Object element) {
			super("Inject a value");
			this.element = element;
		}

		public void run() {
			SimpleInputDialog inputDialog = new SimpleInputDialog(getShell(),
					"Input a value", "Value for the property", null,
					"Input a value :");
			if (inputDialog.open() == IDialogConstants.OK_ID) {
				((IModelItem) element).addChild(new ValueModelItem(inputDialog
						.getResult()));
			}
		}
	}

	private final class ContextMenuListener implements IMenuListener {
		public void menuAboutToShow(IMenuManager mgr) {
			fillContextMenu(mgr);
		}
	}

	private static final String WIZARD_ID = SpringBeanConstructorWizardPage.class
			.getName();

	private TreeListDialogField constructorArgsDialogField;

	private Composite constructorSectionClient;

	private ComboDialogField constructorComboDialogField;

	/**
	 * @param selection
	 * @param wizardId
	 * @param title
	 * @param description
	 */
	public SpringBeanConstructorWizardPage() {
		super(WIZARD_ID, "Inject dependencies in a Spring Bean's properties",
				"This wizard page helps you to inject dependencies in this bean's properties.");
	}

	protected void describe() {
		Section propertiesSection = getWizardFormToolkit()
				.createSection(getWizardForm().getBody(),
						Section.TITLE_BAR | Section.EXPANDED);
		GridLayout propertiesSectionLayout = new GridLayout(1, false);
		propertiesSection.setLayout(propertiesSectionLayout);
		propertiesSection.setLayoutData(new TableWrapData(
				TableWrapData.FILL_GRAB));
		propertiesSection
				.setText("Should a specific constructor be used to init the bean :");
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 4;
		layout.makeColumnsEqualWidth = false;
		constructorSectionClient = getWizardFormToolkit().createComposite(
				propertiesSection);
		constructorSectionClient.setLayout(layout);
		constructorSectionClient.setLayoutData(new TableWrapData(
				TableWrapData.FILL_GRAB));
		constructorComboDialogField = new ComboDialogField(
				getWizardFormToolkit(), SWT.FLAT);
		constructorComboDialogField
				.setLabelText("Choose a constructor to use :");
		constructorComboDialogField.setDialogFieldListener(getWizardAdapter());
		constructorComboDialogField
				.doFillIntoTable(constructorSectionClient, 4);

		constructorArgsDialogField = new TreeListDialogField(
				getWizardFormToolkit(), getWizardAdapter(), new String[] {},
				new ConstructorArgsListLabelProvider());
		constructorArgsDialogField.setLabelText("Constructor arguments :");
		constructorArgsDialogField.setDialogFieldListener(getWizardAdapter());
		constructorArgsDialogField.doFillIntoTable(constructorSectionClient, 4);
		MenuManager menuManager = new MenuManager();
		constructorArgsDialogField.getTreeViewer().getTree().setMenu(
				menuManager.createContextMenu(constructorArgsDialogField
						.getTreeViewer().getTree()));
		menuManager.addMenuListener(new ContextMenuListener());

		propertiesSection.setClient(constructorSectionClient);
		getFirstPage().addPageCompleteListener(new IPageCompleteListener() {
			public void pageComplete(AbstractWizardCustomPage wizardPage) {
				constructorArgsDialogField.getTreeViewer().setContentProvider(
						new ConstructorArgsListContentProvider(
								constructorArgsDialogField,
								getFirstPage().selectedType
										.getFullyQualifiedName()
										+ "2"));
				List constructors = getBeanEligibleConstructors();
				String[] constructorIds = new String[constructors.size() + 1];
				constructorIds[0] = "Use the default no-parameter constructor (default).";
				int i = 1;
				for (Iterator it = constructors.iterator(); it.hasNext();) {
					ConstructorModelItem constructorModelItem = (ConstructorModelItem) it
							.next();
					String constructorId = createConstructorId(constructorModelItem);
					constructorIds[i++] = constructorId.toString();
				}
				if (constructorIds.length == 1) {
					constructorIds = new String[] { "No available specific constructor" };
				}
				constructorComboDialogField.setItems(constructorIds);
				constructorComboDialogField.selectItem(0);
			}

		});
	}

	protected static String createConstructorId(
			ConstructorModelItem constructorModelItem) {
		StringBuffer constructorId = new StringBuffer(constructorModelItem
				.getTypeName()
				+ "(");
		for (Iterator it2 = constructorModelItem.getChildren().iterator(); it2
				.hasNext();) {
			ConstructorArgModelItem constructorArgModelItem = (ConstructorArgModelItem) it2
					.next();
			if (constructorArgModelItem.isPrimitive()) {
				constructorId.append(constructorArgModelItem
						.getPrimitiveTypeName()
						+ " " + constructorArgModelItem.getName());
			} else {
				constructorId.append(constructorArgModelItem.getType()
						.getElementName()
						+ " " + constructorArgModelItem.getName());
			}
			if (it2.hasNext()) {
				constructorId.append(",");
			}
		}
		constructorId.append(")");
		return constructorId.toString();
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
		// boolean addMapKey = false;
		boolean addProps = false;
		boolean addProp = false;
		final List accessibleBeans = getAccessibleBeans();
		mgr.removeAll();
		final List allSelectedElements = this.constructorArgsDialogField
				.getSelectedElements();
		final Object firstElement = allSelectedElements.get(0);
		remove = true;
		for (Iterator it = allSelectedElements.iterator(); it.hasNext();) {
			Object next = it.next();
			if (next instanceof ConstructorModelItem
					|| next instanceof ConstructorArgModelItem) {
				remove = false;
				break;
			}
		}
		if (allSelectedElements.size() == 1) {
			if (firstElement instanceof PropertyModelItem
					|| firstElement instanceof ListModelItem
					|| firstElement instanceof SetModelItem
					|| firstElement instanceof MapEntryModelItem
					|| firstElement instanceof ConstructorArgModelItem) {
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
								ITypeHierarchy superTypeHierarchy = propertyModelItem
										.getType().newSupertypeHierarchy(
												new NullProgressMonitor());
								if (superTypeHierarchy.contains(BeansModelUtils
										.getJavaType(
												getFirstPage().selectedProject
														.getProject(),
												Set.class.getName()))) {
									addSet = true;
									addValue = false;
									addRef = false;
									addIdRef = false;
								}
								if (superTypeHierarchy.contains(BeansModelUtils
										.getJavaType(
												getFirstPage().selectedProject
														.getProject(),
												List.class.getName()))) {
									addList = true;
									addValue = false;
									addRef = false;
									addIdRef = false;
								}
								if (superTypeHierarchy.contains(BeansModelUtils
										.getJavaType(
												getFirstPage().selectedProject
														.getProject(),
												Map.class.getName()))) {
									addMap = true;
									addValue = false;
									addRef = false;
									addIdRef = false;
								}
								if (superTypeHierarchy.contains(BeansModelUtils
										.getJavaType(
												getFirstPage().selectedProject
														.getProject(),
												Properties.class.getName()))) {
									addProps = true;
									addMap = false;// because Properties is a
									// dirty extension of Map
									addValue = false;
									addRef = false;
									addIdRef = false;
								}

							} catch (JavaModelException e) {
								// do nothing
							}
						}
					} else if (modelItem instanceof ConstructorArgModelItem) {
						ConstructorArgModelItem constructorArgModelItem = (ConstructorArgModelItem) firstElement;
						if (!(constructorArgModelItem.isPrimitive())) {
							try {
								ITypeHierarchy superTypeHierarchy = constructorArgModelItem
										.getType().newSupertypeHierarchy(
												new NullProgressMonitor());
								if (superTypeHierarchy.contains(BeansModelUtils
										.getJavaType(
												getFirstPage().selectedProject
														.getProject(),
												Set.class.getName()))) {
									addSet = true;
									addValue = false;
									addRef = false;
									addIdRef = false;
								}
								if (superTypeHierarchy.contains(BeansModelUtils
										.getJavaType(
												getFirstPage().selectedProject
														.getProject(),
												List.class.getName()))) {
									addList = true;
									addValue = false;
									addRef = false;
									addIdRef = false;
								}
								if (superTypeHierarchy.contains(BeansModelUtils
										.getJavaType(
												getFirstPage().selectedProject
														.getProject(),
												Map.class.getName()))) {
									addMap = true;
									addValue = false;
									addRef = false;
									addIdRef = false;
								}
								if (superTypeHierarchy.contains(BeansModelUtils
										.getJavaType(
												getFirstPage().selectedProject
														.getProject(),
												Properties.class.getName()))) {
									addProps = true;
									addMap = false;// because Properties is a
									// dirty extension of Map
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
			mgr
					.add(new InjectBeanReferenceAction(accessibleBeans,
							firstElement));
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
		return ((SpringBeanBasicWizardPage) getWizard().getPage(
				SpringBeanBasicWizardPage.WIZARD_ID));
	}

	private void setListEnabled(boolean enabled) {
		if (constructorArgsDialogField != null) {
			constructorArgsDialogField.setEnabled(enabled);
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
		List configSets = BeansModelUtils
				.getConfigSets(getFirstPage().selectedConfigFile);
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
			Collection propertiesMethods = Introspector.findWritableProperties(
					type, "");
			for (Iterator it = propertiesMethods.iterator(); it.hasNext();) {
				IMethod method = (IMethod) it.next();
				String propertyName = StringUtils.uncapitalize(method
						.getElementName().substring(3));
				PropertyModelItem propertyModelItem = new PropertyModelItem(
						propertyName);
				IField field = type.getField(propertyName);
				IType propertyType = null;
				try {
					String propertyTypeString = Signature.toString(
							field.getTypeSignature()).replace('$', '.');
					String resolvedPropertyType = resolveClassName(
							propertyTypeString, type);
					propertyType = BeansModelUtils.getJavaType(type
							.getJavaProject().getProject(),
							resolvedPropertyType);
					if (propertyType == null) {
						propertyModelItem.setPrimitive(true);
						propertyModelItem
								.setPrimitiveTypeName(resolvedPropertyType);
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
	}

	private List getBeanEligibleConstructors() {
		List result = new ArrayList();
		try {
			IType type = getFirstPage().selectedType;
			Collection constructors = Introspector.findAllConstructors(type);
			int i = 0;
			for (Iterator it = constructors.iterator(); it.hasNext();) {
				IMethod constructor = (IMethod) it.next();
				ConstructorModelItem constructorModelItem = new ConstructorModelItem(
						i++, constructor.getElementName());
				String[] parameterNames = constructor.getRawParameterNames();
				String[] parameterTypes = constructor.getParameterTypes();
				for (int j = 0; j < parameterNames.length; j++) {
					ConstructorArgModelItem constructorArgModelItem = new ConstructorArgModelItem(
							j);
					IType constructorArgType = null;
					try {
						String constructorArgTypeString = Signature.toString(
								parameterTypes[j]).replace('$', '.');
						String resolvedconstructorArgType = resolveClassName(
								constructorArgTypeString, type);
						constructorArgType = BeansModelUtils.getJavaType(type
								.getJavaProject().getProject(),
								resolvedconstructorArgType);
						if (constructorArgType == null) {
							constructorArgModelItem.setPrimitive(true);
							constructorArgModelItem
									.setPrimitiveTypeName(resolvedconstructorArgType);
						}
					} catch (IllegalArgumentException e) {
						// do Nothing
					}
					constructorArgModelItem.setType(constructorArgType);
					constructorArgModelItem.setName(parameterNames[j]);
					constructorModelItem.addChild(constructorArgModelItem);
				}
				if (parameterNames.length > 0) {
					result.add(constructorModelItem);
				}
			}
		} catch (Throwable e) {
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ec.ep.dit.isp.foundry.eclipse.plugins.webservices.wizards.AbstractWizardPage#initialize()
	 */
	protected void initialize() {
		setListEnabled(true);
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
	 * @see ec.ep.dit.isp.foundry.eclipse.platform.utils.wizards.AbstractWizardPage#handleSelectionChanged(org.eclipse.ui.IWorkbenchPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	protected void handleSelectionChanged(IWorkbenchPart part,
			ISelection selection) {
		touch();
	}

	/**
	 * @see ec.ep.dit.isp.foundry.eclipse.plugins.webservices.wizards.AbstractWizardPage#handleDialogFieldChanged()
	 */
	protected void handleDialogFieldChanged(IDialogField field) {
		if (field.equals(constructorComboDialogField)) {
			boolean constructorArgsRefreshed = false;
			for (Iterator it = getBeanEligibleConstructors().iterator(); it
					.hasNext();) {
				ConstructorModelItem constructorModelItem = (ConstructorModelItem) it
						.next();

				if (constructorComboDialogField.getItems().length > 1
						&& constructorComboDialogField.getSelectionIndex() != -1
						&& constructorComboDialogField.getItems()[constructorComboDialogField
								.getSelectionIndex()]
								.equals(createConstructorId(constructorModelItem))) {
					RootModelItem.getInstance(
							getFirstPage().selectedType.getFullyQualifiedName()
									+ "2").addChild(constructorModelItem);
					constructorArgsRefreshed = true;
				}
			}
			if (!constructorArgsRefreshed) {

				RootModelItem.getInstance(
						getFirstPage().selectedType.getFullyQualifiedName()
								+ "2").clearChildren();

			}
		}
		touch();
	}

	/**
	 * @see ec.ep.dit.isp.foundry.eclipse.plugins.webservices.wizards.AbstractWizardPage#handleCustomButtonPressed(ListDialogField,
	 *      int)
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

	public List getChosenConstructorArgs() {
		List constructors = RootModelItem.getInstance(
				getFirstPage().selectedType.getFullyQualifiedName() + "2")
				.getChildren();
		return constructors.size() != 0 ? ((ConstructorModelItem) constructors
				.get(0)).getChildren() : new ArrayList();
	}

}