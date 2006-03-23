/*
 * Created on 13-Jan-2005
 */
package org.springframework.ide.eclipse.beans.ui.wizards.wizards;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.springframework.ide.eclipse.core.ui.fields.ComboDialogField;
import org.springframework.ide.eclipse.core.ui.fields.IDialogField;
import org.springframework.ide.eclipse.core.ui.fields.ListDialogField;
import org.springframework.ide.eclipse.core.ui.fields.TreeListDialogField;
import org.springframework.ide.eclipse.core.ui.wizards.AbstractWizardCustomPage;
import org.springframework.ide.eclipse.core.ui.wizards.StatusInfo;

/**
 * @author pagregoire
 * 
 */
public class SpringBeanLifecycleWizardPage extends AbstractWizardCustomPage {
	private static final String WIZARD_ID = SpringBeanLifecycleWizardPage.class.getName();

	public static final int DEP_CHCK_DEFAULT = 0;

	public static final int DEP_CHCK_NONE = 1;

	public static final int DEP_CHCK_SIMPLE = 2;

	public static final int DEP_CHCK_OBJECTS = 3;

	public static final int DEP_CHCK_ALL = 4;

	public static final String[] DEP_CHECK_LABELS = new String[] { "default", "none", "simple", "objects", "all" };

	public static final int AUTOWIRE_DEFAULT = 0;

	public static final int AUTOWIRE_NO = 1;

	public static final int AUTOWIRE_BYNAME = 2;

	public static final int AUTOWIRE_BYTYPE = 3;

	public static final int AUTOWIRE_CONSTRUCTOR = 4;

	public static final int AUTOWIRE_AUTODETECT = 5;

	public static final String[] AUTOWIRE_LABELS = new String[] { "default", "no", "byName", "byType", "constructor", "autodetect" };

	public static final int LAZYINIT_DEFAULT = 0;

	public static final int LAZYINIT_TRUE = 1;

	public static final int LAZYINIT_FALSE = 2;

	public static final String[] LAZYINIT_LABELS = new String[] { "default", "true", "false" };

	public static final int SINGLETON_DEFAULT = 0;

	public static final int SINGLETON_TRUE = 1;

	public static final int SINGLETON_FALSE = 2;

	public static final String[] SINGLETON_LABELS = new String[] { "default", "true", "false" };

	protected ComboDialogField lazyInitCombo;

	protected ComboDialogField autowireCombo;

	protected ComboDialogField dependencyCheckCombo;

	protected ComboDialogField initMethodCombo;

	protected ComboDialogField destroyMethodCombo;

	protected ComboDialogField singletonBeanCombo;

	/**
	 * @param selection
	 * @param wizardId
	 * @param title
	 * @param description
	 */
	public SpringBeanLifecycleWizardPage() {
		super(WIZARD_ID, "Declaring a Spring Bean", "This wizard page helps the user with the basic part of bean declaration.");
	}

	protected void describe() {
		Section howSection = getWizardFormToolkit().createSection(getWizardForm().getBody(), Section.TWISTIE | Section.TITLE_BAR | Section.EXPANDED);
		GridLayout howSectionLayout = new GridLayout(1, false);
		howSection.setLayout(howSectionLayout);
		howSection.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		howSection.setText("How this Spring Bean's lifecycle will be handled by the Bean factory :");
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 4;
		layout.makeColumnsEqualWidth = false;
		Composite howSectionClient = getWizardFormToolkit().createComposite(howSection);
		howSectionClient.setLayout(layout);
		singletonBeanCombo = new ComboDialogField(getWizardFormToolkit(), SWT.READ_ONLY);
		singletonBeanCombo.setLabelText("Singleton Bean");
		singletonBeanCombo.setItems(getSingletonBeanModes());
		singletonBeanCombo.selectItem(0);
		singletonBeanCombo.doFillIntoTable(howSectionClient, 4);
		lazyInitCombo = new ComboDialogField(getWizardFormToolkit(), SWT.READ_ONLY);
		lazyInitCombo.setLabelText("Lazy-init :");
		lazyInitCombo.setItems(getLazyInitModes());
		lazyInitCombo.selectItem(0);
		lazyInitCombo.doFillIntoTable(howSectionClient, 4);
		lazyInitCombo.getLabelControl(null).setToolTipText("This option defines the way the bean will be inited. See options for details.");
		autowireCombo = new ComboDialogField(getWizardFormToolkit(), SWT.READ_ONLY);
		autowireCombo.setLabelText("Autowire :");
		autowireCombo.setItems(getAutowireModes());
		autowireCombo.selectItem(0);
		autowireCombo.doFillIntoTable(howSectionClient, 4);
		autowireCombo.getLabelControl(null).setToolTipText("This option defines if a bean is autowired and why.  See options for details.");
		dependencyCheckCombo = new ComboDialogField(getWizardFormToolkit(), SWT.READ_ONLY);
		dependencyCheckCombo.setLabelText("Dependency-check :");
		dependencyCheckCombo.setItems(getDependencyCheckModes());
		dependencyCheckCombo.selectItem(0);
		dependencyCheckCombo.doFillIntoTable(howSectionClient, 4);
		dependencyCheckCombo.getLabelControl(null).setToolTipText("This option defines how the dependency checks done before a bean is inited.\n See options for details.");
		initMethodCombo = new ComboDialogField(getWizardFormToolkit(), SWT.READ_ONLY);
		initMethodCombo.setLabelText("Init-method :");
		String[] beanEligibleInitMethods = getBeanEligibleInitMethods();
		initMethodCombo.setItems(beanEligibleInitMethods);
		initMethodCombo.selectItem(0);
		initMethodCombo.doFillIntoTable(howSectionClient, 4);
		initMethodCombo.getLabelControl(null).setToolTipText("This option defines a method used to init this bean.\n if none is chosen, it can be delegated to a default init method \n(as of Spring 1.2.7).");
		if (beanEligibleInitMethods.length == 1) {
			initMethodCombo.getLabelControl(null).setEnabled(false);
			initMethodCombo.getComboControl(null).setEnabled(false);
		}
		destroyMethodCombo = new ComboDialogField(getWizardFormToolkit(), SWT.READ_ONLY);
		destroyMethodCombo.setLabelText("Destroy-method :");
		String[] beanEligibleDestroyMethods = getBeanEligibleDestroyMethods();
		destroyMethodCombo.setItems(beanEligibleDestroyMethods);
		destroyMethodCombo.selectItem(0);
		destroyMethodCombo.doFillIntoTable(howSectionClient, 4);
		destroyMethodCombo.getLabelControl(null).setToolTipText("This option defines a method used to destroy this bean.\n if none is chosen, it can be delegated to a default destroy method \n(as of Spring 1.2.7).");
		if (beanEligibleDestroyMethods.length == 1) {
			destroyMethodCombo.getLabelControl(null).setEnabled(false);
			destroyMethodCombo.getComboControl(null).setEnabled(false);
		}
		howSection.setClient(howSectionClient);
		getFirstPage().addPageCompleteListener(new IPageCompleteListener() {
			public void pageComplete(AbstractWizardCustomPage wizardPage) {
				String[] beanEligibleInitMethods = getBeanEligibleInitMethods();
				initMethodCombo.setItems(beanEligibleInitMethods);
				if (beanEligibleInitMethods.length == 1) {
					initMethodCombo.getLabelControl(null).setEnabled(false);
					initMethodCombo.getComboControl(null).setEnabled(false);
					initMethodCombo.selectItem(0);
				} else {
					initMethodCombo.getLabelControl(null).setEnabled(true);
					initMethodCombo.getComboControl(null).setEnabled(true);
					initMethodCombo.selectItem(0);
				}
			}
		});
		getFirstPage().addPageCompleteListener(new IPageCompleteListener() {
			public void pageComplete(AbstractWizardCustomPage wizardPage) {
				String[] beanEligibleDestroyMethods = getBeanEligibleDestroyMethods();
				destroyMethodCombo.setItems(beanEligibleDestroyMethods);
				if (beanEligibleDestroyMethods.length == 1) {
					destroyMethodCombo.getLabelControl(null).setEnabled(false);
					destroyMethodCombo.getComboControl(null).setEnabled(false);
					destroyMethodCombo.selectItem(0);
				} else {
					destroyMethodCombo.getLabelControl(null).setEnabled(true);
					destroyMethodCombo.getComboControl(null).setEnabled(true);
					destroyMethodCombo.selectItem(0);
				}
			}
		});
	}

	private String[] getSingletonBeanModes() {
		String[] singletonBeanModes = new String[3];
		singletonBeanModes[SINGLETON_DEFAULT] = "don't specify (default)";
		singletonBeanModes[SINGLETON_TRUE] = "yes (true)";
		singletonBeanModes[SINGLETON_FALSE] = "no (false)";
		return singletonBeanModes;
	}

	private SpringBeanBasicWizardPage getFirstPage() {
		return ((SpringBeanBasicWizardPage) getWizard().getPage(SpringBeanBasicWizardPage.WIZARD_ID));
	}

	private String[] getBeanEligibleDestroyMethods() {
		String[] result = null;
		try {
			IType type = getFirstPage().selectedType;
			IMethod[] methods = type.getMethods();
			List retainedMethods = new ArrayList();
			for (int i = 0; i < methods.length;) {
				if (methods[i].getNumberOfParameters() == 0 && (!methods[i].isConstructor())) {
					retainedMethods.add(methods[i].getElementName());
				}
				i++;
			}

			result = new String[retainedMethods.size() + 1];
			if (result.length > 1) {
				result[0] = "No chosen init method";
				int i = 1;
				for (Iterator it = retainedMethods.iterator(); it.hasNext();) {
					result[i++] = (String) it.next();
				}
			} else {
				result[0] = "No eligible init method";
			}
		} catch (Exception jme) {
			result = new String[] { "No eligible init method" };
		}
		return result;
	}

	private String[] getBeanEligibleInitMethods() {
		String[] result = null;
		try {
			IType type = getFirstPage().selectedType;
			IMethod[] methods = type.getMethods();
			List retainedMethods = new ArrayList();
			for (int i = 0; i < methods.length;) {
				if (methods[i].getNumberOfParameters() == 0 && (!methods[i].isConstructor())) {
					retainedMethods.add(methods[i].getElementName());
				}
				i++;
			}
			result = new String[retainedMethods.size() + 1];
			if (result.length > 1) {
				result[0] = "No chosen destroy method";
				int i = 1;
				for (Iterator it = retainedMethods.iterator(); it.hasNext();) {
					result[i++] = (String) it.next();
				}
			} else {
				result[0] = "No eligible destroy method";
			}
		} catch (Exception e) {
			result = new String[] { "No eligible destroy method" };
		}
		return result;
	}

	private String[] getDependencyCheckModes() {
		String[] dependencyCheckModes = new String[5];
		dependencyCheckModes[DEP_CHCK_DEFAULT] = "default behaviour (default)";
		dependencyCheckModes[DEP_CHCK_NONE] = "No check (none)";
		dependencyCheckModes[DEP_CHCK_SIMPLE] = "Primitives and String (simple)";
		dependencyCheckModes[DEP_CHCK_OBJECTS] = "Collaborators (objects)";
		dependencyCheckModes[DEP_CHCK_ALL] = "Both previous(all)";
		return dependencyCheckModes;
	}

	private String[] getAutowireModes() {
		String[] autowireModes = new String[6];
		autowireModes[AUTOWIRE_DEFAULT] = "default behaviour (default)";
		autowireModes[AUTOWIRE_NO] = "static (no)";
		autowireModes[AUTOWIRE_BYNAME] = "by Bean name (byName)";
		autowireModes[AUTOWIRE_BYTYPE] = "by Bean type (byType)";
		autowireModes[AUTOWIRE_CONSTRUCTOR] = "by Constructor argument's type (constructor)";
		autowireModes[AUTOWIRE_AUTODETECT] = "Auto-detect (autodetect)";
		return autowireModes;
	}

	private String[] getLazyInitModes() {
		String[] lazyInitModes = new String[3];
		lazyInitModes[LAZYINIT_DEFAULT] = "default behaviour (default)";
		lazyInitModes[LAZYINIT_TRUE] = "yes (true)";
		lazyInitModes[LAZYINIT_FALSE] = "no (false)";
		return lazyInitModes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ec.ep.dit.isp.foundry.eclipse.plugins.webservices.wizards.AbstractWizardPage#initialize()
	 */
	protected void initialize() {
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

	public int getBeanSingletonState() {
		return singletonBeanCombo.getSelectionIndex();
	}

	public int getBeanAutoWireState() {
		return autowireCombo.getSelectionIndex();
	}

	public int getBeanLazyInitState() {
		return lazyInitCombo.getSelectionIndex();
	}

	public int getBeanDependencyCheckState() {
		return dependencyCheckCombo.getSelectionIndex();
	}

	public String getBeanInitMethod() {
		int selectionIndex = initMethodCombo.getSelectionIndex();
		return selectionIndex > 0 ? initMethodCombo.getText() : null;
	}

	public String getBeanDestroyMethod() {
		int selectionIndex = destroyMethodCombo.getSelectionIndex();
		return selectionIndex > 0 ? destroyMethodCombo.getText() : null;
	}

}