/*
 * Created on 13-Jan-2005
 */
package org.springframework.ide.eclipse.beans.ui.wizards.wizards;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.contentassist.ISubjectControlContentAssistProcessor;
import org.eclipse.jface.contentassist.SubjectControlContentAssistant;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contentassist.ContentAssistHandler;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.wizards.BeansWizardsPlugin;
import org.springframework.ide.eclipse.beans.ui.wizards.wizards.contentassist.SpringBeansCandidateContentAssistProcessor;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.ui.dialogs.message.ErrorDialog;
import org.springframework.ide.eclipse.core.ui.fields.ComboDialogField;
import org.springframework.ide.eclipse.core.ui.fields.IDialogField;
import org.springframework.ide.eclipse.core.ui.fields.ListDialogField;
import org.springframework.ide.eclipse.core.ui.fields.StringButtonDialogField;
import org.springframework.ide.eclipse.core.ui.fields.StringDialogField;
import org.springframework.ide.eclipse.core.ui.fields.TreeListDialogField;
import org.springframework.ide.eclipse.core.ui.wizards.AbstractWizardCustomPage;
import org.springframework.ide.eclipse.core.ui.wizards.StatusInfo;

/**
 * @author pagregoire
 * 
 */
public class SpringBeanBasicWizardPage extends AbstractWizardCustomPage {
	public static final String WIZARD_ID = SpringBeanBasicWizardPage.class.getName();

	protected ComboDialogField configFilesCombo;

	protected StringButtonDialogField javaBeanToDeclareDialogField;

	protected StringDialogField beanIdDialogField;

	protected StringDialogField beanNameDialogField;

	protected ComboDialogField projectCombo;

	protected IBeansProject selectedProject;

	protected IBeansConfig selectedConfigFile;

	protected IType selectedType;

	private Section whereSection;

	private Section whichSection;

	private Section howSection;

	protected ComboDialogField abstractBeanComboField;

	private SubjectControlContentAssistant beanNamecontentAssistant;

	private SubjectControlContentAssistant beanIdcontentAssistant;

	public static final int ABSTRACT_DEFAULT = 0;

	public static final int ABSTRACT_TRUE = 1;

	public static final int ABSTRACT_FALSE = 2;

	public static final String[] ABSTRACT_LABELS = new String[] { "default", "true", "false" };

	/**
	 * @param selection
	 * @param wizardId
	 * @param title
	 * @param description
	 */
	public SpringBeanBasicWizardPage(IBeansProject project, IBeansConfig configFile, IType type) {
		super(WIZARD_ID, "Declare a Spring bean", "Basic bean configuration");
		this.selectedProject = project;
		this.selectedConfigFile = configFile;
		this.selectedType = type;
		setColumnsNumber(1);
	}

	protected void describe() {
		whereSection = getWizardFormToolkit().createSection(getWizardForm().getBody(), Section.TWISTIE | Section.TITLE_BAR | Section.EXPANDED);
		GridLayout whereSectionLayout = new GridLayout(1, false);
		whereSection.setLayout(whereSectionLayout);
		whereSection.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		whereSection.setText("Where the Spring Bean should be declared :");
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;

		Composite whereSectionClient = getWizardFormToolkit().createComposite(whereSection);
		whereSectionClient.setLayout(layout);
		projectCombo = new ComboDialogField(getWizardFormToolkit(), SWT.READ_ONLY);
		projectCombo.setLabelText("Select a project :");
		projectCombo.setDialogFieldListener(getWizardAdapter());
		projectCombo.doFillIntoTable(whereSectionClient, 2);
		configFilesCombo = new ComboDialogField(getWizardFormToolkit(), SWT.READ_ONLY);
		configFilesCombo.setLabelText("Select a configuration file :");
		configFilesCombo.setDialogFieldListener(getWizardAdapter());
		configFilesCombo.doFillIntoTable(whereSectionClient, 2);
		configFilesCombo.getLabelControl(null).setToolTipText("These are the config files that can be found in this project's classpath.");
		whereSection.setClient(whereSectionClient);

		whichSection = getWizardFormToolkit().createSection(getWizardForm().getBody(), Section.TWISTIE | Section.TITLE_BAR);
		GridLayout whichSectionLayout = new GridLayout(1, false);
		whichSection.setLayout(whichSectionLayout);
		whichSection.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		whichSection.setText("Which java bean should be declared as a Spring Bean :");
		layout = new TableWrapLayout();
		layout.numColumns = 3;
		layout.makeColumnsEqualWidth = false;
		Composite whichSectionClient = getWizardFormToolkit().createComposite(whichSection);
		whichSectionClient.setLayout(layout);
		javaBeanToDeclareDialogField = new StringButtonDialogField(getWizardFormToolkit(), getWizardAdapter());
		javaBeanToDeclareDialogField.setLabelText("Select the Java Class :");
		javaBeanToDeclareDialogField.setButtonLabel("Browse...");
		javaBeanToDeclareDialogField.setDialogFieldListener(getWizardAdapter());
		javaBeanToDeclareDialogField.doFillIntoTable(whichSectionClient, 3);
		whichSection.setClient(whichSectionClient);

		howSection = getWizardFormToolkit().createSection(getWizardForm().getBody(), Section.TITLE_BAR);
		GridLayout howSectionLayout = new GridLayout(1, false);
		howSection.setLayout(howSectionLayout);
		howSection.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		howSection.setText("How this java class should be declared as a Spring Bean :");
		layout = new TableWrapLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		Composite howSectionClient = getWizardFormToolkit().createComposite(howSection);
		howSectionClient.setLayout(layout);
		abstractBeanComboField = new ComboDialogField(getWizardFormToolkit(), SWT.READ_ONLY);
		abstractBeanComboField.setLabelText("Abstract Bean");
		abstractBeanComboField.setItems(getAbstractBeanModes());
		abstractBeanComboField.selectItem(0);
		abstractBeanComboField.setDialogFieldListener(getWizardAdapter());
		abstractBeanComboField.doFillIntoTable(howSectionClient, 2);

		beanIdDialogField = new StringDialogField(getWizardFormToolkit());
		beanIdDialogField.setLabelText("Bean Id :");
		beanIdDialogField.setDialogFieldListener(getWizardAdapter());
		beanIdDialogField.doFillIntoTable(howSectionClient, 2);

		beanNameDialogField = new StringDialogField(getWizardFormToolkit());
		beanNameDialogField.setLabelText("Bean Name :");
		beanNameDialogField.setDialogFieldListener(getWizardAdapter());
		beanNameDialogField.doFillIntoTable(howSectionClient, 2);
		howSection.setClient(howSectionClient);
	}

	private void createBeansContentAssistProcessors() {
		beanIdcontentAssistant = new SubjectControlContentAssistant();

		ISubjectControlContentAssistProcessor processor = new SpringBeansCandidateContentAssistProcessor();
		beanIdcontentAssistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
		beanIdcontentAssistant.setProposalSelectorBackground(JFaceColors.getBannerBackground(getShell().getDisplay()));
		beanIdcontentAssistant.setProposalSelectorForeground(JFaceColors.getBannerForeground(getShell().getDisplay()));
		beanIdcontentAssistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		beanIdcontentAssistant.setInformationControlCreator(new IInformationControlCreator() {
			/*
			 * @see org.eclipse.jface.text.IInformationControlCreator#createInformationControl(org.eclipse.swt.widgets.Shell)
			 */
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent);
			}
		});

		beanNamecontentAssistant = new SubjectControlContentAssistant();

		processor = new SpringBeansCandidateContentAssistProcessor();
		beanNamecontentAssistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
		beanNamecontentAssistant.setProposalSelectorBackground(JFaceColors.getBannerBackground(getShell().getDisplay()));
		beanNamecontentAssistant.setProposalSelectorForeground(JFaceColors.getBannerForeground(getShell().getDisplay()));
		beanNamecontentAssistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		beanNamecontentAssistant.setInformationControlCreator(new IInformationControlCreator() {
			/*
			 * @see org.eclipse.jface.text.IInformationControlCreator#createInformationControl(org.eclipse.swt.widgets.Shell)
			 */
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent);
			}
		});
	}

	private String[] getAbstractBeanModes() {
		String[] abstractBeanModes = new String[3];
		abstractBeanModes[ABSTRACT_DEFAULT] = "don't specify (default)";
		abstractBeanModes[ABSTRACT_FALSE] = "yes (true)";
		abstractBeanModes[ABSTRACT_TRUE] = "no (false)";
		return abstractBeanModes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ec.ep.dit.isp.foundry.eclipse.plugins.webservices.wizards.AbstractWizardPage#initialize()
	 */
	protected void initialize() {
		if (selectedProject != null) {
			String[] beansProjects = getBeansProjects();
			projectCombo.setItems(getBeansProjects());
			int selectedProjectIndex = getBeanProjectIndex(selectedProject, beansProjects);
			if (selectedProjectIndex != -1) {
				projectCombo.selectItem(selectedProjectIndex);
			}
		} else {
			projectCombo.setItems(getBeansProjects());
			if (projectCombo.getItems().length == 1) {
				projectCombo.selectItem(0);
			}
		}
		if (selectedConfigFile != null) {
			configFilesCombo.setItems(new String[] { selectedConfigFile.getConfigFile().getProjectRelativePath().toString() });
			configFilesCombo.selectItem(0);
		} else if (configFilesCombo.getItems().length == 1) {
			configFilesCombo.selectItem(0);
		}
		if (selectedType != null) {
			javaBeanToDeclareDialogField.setText(selectedType.getFullyQualifiedName());
			whichSection.setExpanded(false);
		}
		if (selectedProject != null && selectedConfigFile != null) {
			whereSection.setExpanded(false);
		}
		createBeansContentAssistProcessors();
		ContentAssistHandler.createHandlerForText(beanIdDialogField.getTextControl(null), beanIdcontentAssistant);
		ContentAssistHandler.createHandlerForText(beanNameDialogField.getTextControl(null), beanNamecontentAssistant);
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
		boolean projectCombo_OK=true;
		projectCombo.getLabelControl(null).setForeground(JFaceColors.getBannerForeground(getShell().getDisplay()));
		boolean configFilesCombo_OK=true;
		configFilesCombo.getLabelControl(null).setForeground(JFaceColors.getBannerForeground(getShell().getDisplay()));
		boolean javaBeanToDeclareDialogField_OK=true;
		javaBeanToDeclareDialogField.getLabelControl(null).setForeground(JFaceColors.getBannerForeground(getShell().getDisplay()));
		boolean beanIdDialogField_OK=true;
		beanIdDialogField.getLabelControl(null).setForeground(JFaceColors.getBannerForeground(getShell().getDisplay()));
		boolean beanNameDialogField_OK=true;
		beanNameDialogField.getLabelControl(null).setForeground(JFaceColors.getBannerForeground(getShell().getDisplay()));
		String statusMessage="You can now create the bean based on these basic infos or configure it more in the next pages";
		if (projectCombo.getSelectionIndex() == -1) {
			statusMessage="Choose a project first";
			projectCombo_OK=false;
		} else {
			if (configFilesCombo.getSelectionIndex() == -1) {
				statusMessage="A config file should be chosen";
				configFilesCombo_OK=false;
			} else {
				if (javaBeanToDeclareDialogField.getText().length() == 0) {
					statusMessage="A class to declare should be chosen";
					javaBeanToDeclareDialogField_OK=false;
				} else {
					if (!checkValidType(selectedProject, javaBeanToDeclareDialogField.getText())) {
						statusMessage="The class to declare should be valid";
						javaBeanToDeclareDialogField_OK=false;
					} else {
						if (beanIdDialogField.getText().trim().length() == 0 && beanNameDialogField.getText().trim().length() == 0) {
							beanIdDialogField_OK=false;
							beanNameDialogField_OK=false;
							statusMessage="At least a bean Id or a bean Name should be chosen. Otherwise the class name with an incremented id will be used.";
						} 
						else {
							status.setInfo(statusMessage);
						}
					}
				}
			}
		}
		whereSection.setExpanded(false);
		whichSection.setExpanded(false);
		if((!projectCombo_OK)||(!configFilesCombo_OK)){
			whereSection.setExpanded(true);
			if(!projectCombo_OK){
				setErrorOn(projectCombo);
				status.setError(statusMessage);
			}else if(!configFilesCombo_OK){
				setErrorOn(configFilesCombo);
				status.setError(statusMessage);
			}
		}
		if(!javaBeanToDeclareDialogField_OK){
			whichSection.setExpanded(true);
			setErrorOn(javaBeanToDeclareDialogField);
			status.setError(statusMessage);
		}
		if((!beanIdDialogField_OK)||(!beanNameDialogField_OK)){
			howSection.setExpanded(true);
			setWarningOn(beanNameDialogField);
			setWarningOn(beanIdDialogField);
			status.setWarning(statusMessage);
		}
		return status;
	}

	private void setErrorOn(IDialogField dialogField) {
		dialogField.getLabelControl(null).setForeground(JFaceColors.getErrorText(getShell().getDisplay()));
		dialogField.setFocus();
	}
	private void setWarningOn(IDialogField dialogField) {
		dialogField.getLabelControl(null).setForeground(JFaceColors.getHyperlinkText(getShell().getDisplay()));
		dialogField.setFocus();
	}
	/**
	 * @see ec.ep.dit.isp.foundry.eclipse.plugins.webservices.wizards.AbstractWizardPage#handleDialogFieldChanged()
	 */
	protected void handleDialogFieldChanged(IDialogField field) {
		if (field.equals(projectCombo)) {
			if (projectCombo.getSelectionIndex() != -1) {
				selectedProject = getBeansProject(projectCombo.getText());
				configFilesCombo.setItems(getConfigFiles(selectedProject));
				configFilesCombo.setEnabled(true);
			}
		}
		if (field.equals(configFilesCombo)) {
			if (configFilesCombo.getSelectionIndex() != -1) {
				selectedConfigFile = getConfigFile(configFilesCombo.getText());
				whichSection.setExpanded(true);
			}
		}
		updateContentAssist();
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
		IType javaType = doBrowseTypes();
		if (javaType != null) {
			javaBeanToDeclareDialogField.setTextWithoutUpdate(javaType.getFullyQualifiedName());
			selectedType = javaType;
		}
		touch();
	}

	private void updateContentAssist() {
		Set proposals = new TreeSet();
		if (selectedType != null) {
			proposals.add(Introspector.decapitalize(selectedType.getElementName()));
			proposals.add(Introspector.decapitalize(selectedType.getFullyQualifiedName()));
			try {
				if (selectedType.getSuperclassName() != null && !selectedType.getSuperclassName().equals("java.lang.Object")) {
					proposals.add(Introspector.decapitalize(selectedType.getSuperclassName()));
				}
			} catch (JavaModelException e) {
				// silencing the exception
			}
		}
		if (beanNamecontentAssistant != null) {
			((SpringBeansCandidateContentAssistProcessor) beanNamecontentAssistant.getContentAssistProcessor(IDocument.DEFAULT_CONTENT_TYPE)).setProposals(proposals);
		}
		if (beanIdcontentAssistant != null) {
			((SpringBeansCandidateContentAssistProcessor) beanIdcontentAssistant.getContentAssistProcessor(IDocument.DEFAULT_CONTENT_TYPE)).setProposals(proposals);
		}
	}

	private IType doBrowseTypes() {
		IType type = null;
		try {
			IJavaElement[] javaElements = JavaCore.create(selectedProject.getProject()).getAllPackageFragmentRoots();
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(javaElements);
			// TODO should there be only classes?
			SelectionDialog dialog = JavaUI.createTypeDialog(getShell(), PlatformUI.getWorkbench().getProgressService(), scope, IJavaElementSearchConstants.CONSIDER_CLASSES, false);
			dialog.setTitle("Choose a Java Type");
			if (dialog.open() == ContainerSelectionDialog.OK) {
				Object[] results = dialog.getResult();
				if (results.length == 1) {
					if (results[0] instanceof IType) {
						type = (IType) results[0];
					}
				}
			}
		} catch (JavaModelException e) {
			ErrorDialog dialog = new ErrorDialog(SpringBeanBasicWizardPage.class.getName(), "Cannot create the interface selection dialog", e);
			dialog.open();
		}
		return type;
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

	// FIXME put this method in an Utils class or replace its calls with an
	// equivalent method.
	private static boolean checkValidType(IBeansProject beansProject, String fullyQualifiedName) {
		boolean valid = true;
		IType type = null;
		try {
			type = JavaCore.create(beansProject.getProject()).findType(fullyQualifiedName);
		} catch (JavaModelException jme) {
		}
		if (type == null) {
			valid = false;
		} else {
			// FIXME add validation for the types here
			valid = true;
		}
		return valid;
	}

	public int getBeanAbstractState() {
		return abstractBeanComboField.getSelectionIndex();
	}

	public String getBeanId() {
		String result = null;
		if (!(beanIdDialogField.getText().equals(""))) {
			result = beanIdDialogField.getText();
		}
		return result;
	}

	public String getBeanName() {
		String result = null;
		if (!(beanNameDialogField.getText().equals(""))) {
			result = beanNameDialogField.getText();
		}
		return result;
	}

	public String getBeanClass() {
		return selectedType.getFullyQualifiedName();
	}

	// POTENTIAL UTILITY METHODS
	private String[] getBeansProjects() {
		IProject[] projects = BeansWizardsPlugin.getWorkspace().getRoot().getProjects();
		List projectNames = new ArrayList();
		for (int i = 0; i < projects.length; i++) {
			try {
				IProject project = projects[i];
				if (project.isOpen() && project.hasNature(SpringCore.NATURE_ID)) {
					projectNames.add(project.getName());
				}
			} catch (CoreException e) {
				// silence this exception...
			}
		}
		int i = 0;
		String[] result = new String[projectNames.size()];
		for (Iterator it = projectNames.iterator(); it.hasNext();) {
			result[i++] = (String) it.next();
		}
		return result;
	}

	private IBeansProject getBeansProject(String name) {
		IProject project = BeansWizardsPlugin.getWorkspace().getRoot().getProject(name);
		return new BeansProject(project);
	}

	private String[] getConfigFiles(IBeansProject project) {
		// FIXME retrieve the config files from the local context.
		List configFiles = new ArrayList();
		for (Iterator it = project.getConfigs().iterator(); it.hasNext();) {
			configFiles.add(((IBeansConfig) it.next()).getElementName());
		}
		String[] result = new String[configFiles.size()];
		int i = 0;
		for (Iterator it = configFiles.iterator(); it.hasNext();) {
			result[i++] = (String) it.next();
		}
		return result;
	}

	private IBeansConfig getConfigFile(String name) {
		// FIXME retrieve the config files from the local context.
		return selectedProject.getConfig(name);
	}

	private int getBeanProjectIndex(IBeansProject selectedProject, String[] beansProjects) {
		int result = -1;
		for (int i = 0; i < beansProjects.length; i++) {
			if (beansProjects[i].equals(selectedProject.getElementName())) {
				result = i;
				break;
			}
		}
		return result;
	}
}