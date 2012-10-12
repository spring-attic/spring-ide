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
package org.springframework.ide.eclipse.roo.ui.internal.wizard;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ComboDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.dialogs.WorkingSetConfigurationBlock;
import org.springframework.ide.eclipse.roo.core.RooCoreActivator;
import org.springframework.ide.eclipse.roo.core.model.IRooInstall;
import org.springframework.ide.eclipse.roo.core.model.IRooInstallListener;
import org.springframework.ide.eclipse.roo.ui.internal.RooUiUtil;
import org.springframework.ide.eclipse.roo.ui.internal.wizard.NewRooProjectWizard.ProjectType;


/**
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @author Terry Denney
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class NewRooProjectWizardPageOne extends WizardPage {

	/**
	 * Request a project name. Fires an event whenever the text field is
	 * changed, regardless of its content.
	 */
	private final class NameGroup extends Observable implements IDialogFieldListener {

		protected final StringDialogField fNameField;

		protected final StringDialogField fPackageField;

		protected final StringDialogField fdescriptionField;

		protected final ComboDialogField fTemplateField;

		public NameGroup() {
			// text field for project name
			fNameField = new StringDialogField();
			fNameField.setLabelText(NewWizardMessages.NewJavaProjectWizardPageOne_NameGroup_label_text);
			fNameField.setDialogFieldListener(this);
			fPackageField = new StringDialogField();
			fPackageField.setLabelText("Top level package name:");
			fPackageField.setDialogFieldListener(this);

			fTemplateField = new ComboDialogField(SWT.READ_ONLY);
			fTemplateField.setLabelText("Project type:");
			List<String> types = new ArrayList<String>();
			for (ProjectType type : ProjectType.values()) {
				types.add(type.getDisplayString());
			}
			fTemplateField.setItems(types.toArray(new String[types.size()]));
			fTemplateField.selectItem(0);

			fdescriptionField = new StringDialogField();
			fdescriptionField.setLabelText("Description");
			fdescriptionField.setDialogFieldListener(this);
			fdescriptionField.setEnabled(false);
		}

		public Control createControl(Composite composite) {
			Composite nameComposite = new Composite(composite, SWT.NONE);
			nameComposite.setFont(composite.getFont());
			nameComposite.setLayout(initGridLayout(new GridLayout(2, false), false));

			fNameField.doFillIntoGrid(nameComposite, 2);
			LayoutUtil.setHorizontalGrabbing(fNameField.getTextControl(null));

			fPackageField.doFillIntoGrid(nameComposite, 2);
			LayoutUtil.setHorizontalGrabbing(fPackageField.getTextControl(null));

			fTemplateField.doFillIntoGrid(nameComposite, 2);
			LayoutUtil.setHorizontalGrabbing(fTemplateField.getComboControl(null));
			fTemplateField.getComboControl(null).addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					ProjectType type = getProjectType();
					fdescriptionField.setEnabled(type != ProjectType.PROJECT);
					if (packagingProviderGroup != null) {
						packagingProviderGroup.updateEnablement();
					}
				}
			});

			fdescriptionField.doFillIntoGrid(nameComposite, 2);
			LayoutUtil.setHorizontalGrabbing(fdescriptionField.getTextControl(null));

			return nameComposite;
		}

		protected void fireEvent() {
			setChanged();
			notifyObservers();
		}

		public String getName() {
			return fNameField.getText().trim();
		}

		public String getPackageName() {
			return fPackageField.getText().trim();
		}

		public String getDescription() {
			return fdescriptionField.getText().trim();
		}

		public ProjectType getProjectType() {
			return ProjectType.fromDisplayString(fTemplateField.getItems()[fTemplateField.getSelectionIndex()]);
		}

		public void postSetFocus() {
			fNameField.postSetFocusOnDialogField(getShell().getDisplay());
		}

		public void setName(String name) {
			fNameField.setText(name);
		}

		public void dialogFieldChanged(DialogField field) {
			fireEvent();
		}
	}

	private final class RooInstallGroup extends Observable {

		private Button useDefault;

		private Button useSpecific;

		private Combo rooInstallCombo;

		public Control createControl(Composite composite) {
			Group rooHomeGroup = new Group(composite, SWT.NONE);
			rooHomeGroup.setFont(composite.getFont());
			rooHomeGroup.setText("Roo Installation");
			rooHomeGroup.setLayout(initGridLayout(new GridLayout(1, false), true));

			useDefault = new Button(rooHomeGroup, SWT.RADIO);

			IRooInstall defaultInstall = RooCoreActivator.getDefault().getInstallManager().getDefaultRooInstall();
			if (defaultInstall != null) {
				useDefault.setText(NewRooWizardMessages.bind(
						NewRooWizardMessages.NewRooProjectWizardPageOne_useDefaultRooInstallation,
						defaultInstall.getName()));
			}
			else {
				setErrorMessage(NewRooWizardMessages.NewRooProjectWizardPageOne_noRooInstallationConfigured);
				setPageComplete(false);
				useDefault.setText(NewRooWizardMessages.NewRooProjectWizardPageOne_useDefaultRooInstallationNoCurrent);
			}
			useDefault.setSelection(true);
			useDefault.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					rooInstallCombo.setEnabled(false);
					fireEvent();
				}
			});

			useSpecific = new Button(rooHomeGroup, SWT.RADIO);
			useSpecific.setText("Use project specific Roo installation:");
			useSpecific.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					rooInstallCombo.setEnabled(true);
					fireEvent();
				}
			});

			final Composite installComposite = new Composite(rooHomeGroup, SWT.NULL);
			installComposite.setFont(composite.getFont());
			installComposite.setLayout(new GridLayout(3, false));
			installComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			Label options = new Label(installComposite, SWT.WRAP);
			options.setText(NewRooWizardMessages.NewRooProjectWizardPageOne_Install);
			options.setLayoutData(new GridData(GridData.BEGINNING));

			rooInstallCombo = new Combo(installComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
			rooInstallCombo.setItems(RooCoreActivator.getDefault().getInstallManager().getAllInstallNames());
			rooInstallCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			rooInstallCombo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					fireEvent();
				}

				@Override
				public void widgetSelected(SelectionEvent e) {
					fireEvent();
				}
			});

			String[] names = rooInstallCombo.getItems();
			for (int i = 0; i < names.length; i++) {
				if (RooCoreActivator.getDefault().getInstallManager().getRooInstall(names[i]).isDefault()) {
					rooInstallCombo.select(i);
					break;
				}
			}
			rooInstallCombo.setEnabled(false);
			rooHomeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			Link link = new Link(installComposite, SWT.NONE);
			link.setFont(composite.getFont());
			link.setText("<A>Configure Roo Installations....</A>"); //$NON-NLS-1$//$NON-NLS-2$
			link.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
			link.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					openPreferences();
				}

				public void widgetDefaultSelected(SelectionEvent e) {
					openPreferences();
				}
			});

			return rooHomeGroup;
		}

		public void refresh() {
			IRooInstall defaultInstall = RooCoreActivator.getDefault().getInstallManager().getDefaultRooInstall();
			if (defaultInstall != null) {
				useDefault.setText(NewRooWizardMessages.bind(
						NewRooWizardMessages.NewRooProjectWizardPageOne_useDefaultRooInstallation,
						defaultInstall.getName()));
			}
			else {
				setErrorMessage(NewRooWizardMessages.NewRooProjectWizardPageOne_noRooInstallationConfigured);
				setPageComplete(false);
				useDefault.setText(NewRooWizardMessages.NewRooProjectWizardPageOne_useDefaultRooInstallationNoCurrent);
			}
			rooInstallCombo.setItems(RooCoreActivator.getDefault().getInstallManager().getAllInstallNames());
			String[] names = rooInstallCombo.getItems();
			for (int i = 0; i < names.length; i++) {
				if (RooCoreActivator.getDefault().getInstallManager().getRooInstall(names[i]).isDefault()) {
					rooInstallCombo.select(i);
					break;
				}
			}
			fireEvent();
		}

		private void openPreferences() {
			String id = "com.springsource.sts.roo.ui.preferencePage";
			PreferencesUtil.createPreferenceDialogOn(getShell(), id, new String[] { id }, Collections.EMPTY_MAP).open();
		}

		protected void fireEvent() {
			setChanged();
			notifyObservers();
		}

	}

	private final class DependencyManagementGroup extends Observable {

		private Combo dependencyManagementCombo;

		public Control createControl(Composite composite) {
			Group group = new Group(composite, SWT.NONE);
			group.setFont(composite.getFont());
			group.setText("Maven Support");
			group.setLayout(initGridLayout(new GridLayout(1, false), true));

			final Composite installComposite = new Composite(group, SWT.NULL);
			installComposite.setFont(composite.getFont());
			installComposite.setLayout(new GridLayout(2, false));
			installComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			Label options = new Label(installComposite, SWT.WRAP);
			options.setText("Provider: ");
			options.setLayoutData(new GridData(GridData.BEGINNING));

			dependencyManagementCombo = new Combo(installComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
			dependencyManagementCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			// get available dependency management providers
			List<String> provider = new ArrayList<String>();
			dependencyManagementCombo.setEnabled(false);

			if (DependencyManagementUtils.IS_M2ECLIPSE_PRESENT || LegacyDependencyManagementUtils.IS_M2ECLIPSE_PRESENT) {
				provider.add("Full Maven build");
				dependencyManagementCombo.setEnabled(true);
			}
			if (DependencyManagementUtils.IS_STS_MAVEN_PRESENT || LegacyDependencyManagementUtils.IS_STS_MAVEN_PRESENT) {
				provider.add("Dependency management only");
				dependencyManagementCombo.setEnabled(true);
			}
			dependencyManagementCombo.setItems(provider.toArray(new String[provider.size()]));
			if (dependencyManagementCombo.isEnabled()) {
				dependencyManagementCombo.select(0);
			}

			group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			return group;
		}

	}
	
	private final class PackagingProviderGroup extends Observable implements Observer {
		
		private Group packagingGroup;
		
		private Button builtinButton;
		
		private Combo packagingProviderCombo;
		
		private Button customButton;
		
		private Text packagingProviderText;
		
		public Control createControl(Composite composite) {
			packagingGroup = new Group(composite, SWT.NONE);
			packagingGroup.setFont(composite.getFont());
			packagingGroup.setText("Packaging Provider");
			packagingGroup.setLayout(initGridLayout(new GridLayout(2, false), true));
			
			builtinButton = new Button(packagingGroup, SWT.RADIO);
			builtinButton.setText("Select a built-in provider");
			builtinButton.setSelection(true);
			builtinButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					packagingProviderCombo.setEnabled(true);
					packagingProviderText.setEnabled(false);
				}
			});
			GridDataFactory.fillDefaults().span(2, 1).applyTo(builtinButton);
			
			Label options = new Label(packagingGroup, SWT.WRAP);
			options.setText("Packaging: ");
			GridDataFactory.swtDefaults().indent(5, 0).applyTo(options);
			
			packagingProviderCombo = new Combo(packagingGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
			packagingProviderCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			packagingProviderCombo.setEnabled(true);
			
			List<String> provider = new ArrayList<String>();			
			provider.add("JAR");
			provider.add("BUNDLE");
			provider.add("POM");
			provider.add("WAR");
			packagingProviderCombo.setItems(provider.toArray(new String[provider.size()]));
			packagingProviderCombo.select(0);
			
			customButton = new Button(packagingGroup, SWT.RADIO);
			customButton.setText("Specify a custom provider");
			customButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					packagingProviderCombo.setEnabled(false);
					packagingProviderText.setEnabled(true);
				}
			});
			GridDataFactory.fillDefaults().span(2, 1).applyTo(customButton);
			
			Label flag = new Label(packagingGroup, SWT.WRAP);
			flag.setText("--provider ");
			GridDataFactory.swtDefaults().indent(5, 0).applyTo(flag);
			
			packagingProviderText = new Text(packagingGroup, SWT.BORDER);
			packagingProviderText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			packagingProviderText.setEnabled(false);
			
			updateEnablement();
			packagingGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			return packagingGroup;
		}
		
		protected void updateEnablement() {
			if (getProjectType() != ProjectType.PROJECT) {
				packagingGroup.setEnabled(false);
				for (Control c : packagingGroup.getChildren()) {
					c.setEnabled(false);
				}
			} else {
				IRooInstall install = null;
				if (useDefaultRooInstall()) {
					install = RooCoreActivator.getDefault().getInstallManager().getDefaultRooInstall();
				}
				else {
					String installName = getRooInstallName();
					if (installName != null) {
						install = RooCoreActivator.getDefault().getInstallManager().getRooInstall(installName);
					}
				}
				
				boolean enableFlag = RooUiUtil.isRoo120OrGreater(install);
				packagingGroup.setEnabled(enableFlag);
				for (Control c : packagingGroup.getChildren()) {
					c.setEnabled(enableFlag);
				}
			}
		}

		public void update(Observable o, Object arg) {
			if (o instanceof RooInstallGroup) {
				updateEnablement();
			}			
		}
		
	}

	/**
	 * Request a location. Fires an event whenever the checkbox or the location
	 * field is changed, regardless of whether the change originates from the
	 * user or has been invoked programmatically.
	 */
	private final class LocationGroup extends Observable implements Observer, IStringButtonAdapter,
			IDialogFieldListener {

		protected final SelectionButtonDialogField fWorkspaceRadio;

		protected final SelectionButtonDialogField fExternalRadio;

		protected final StringButtonDialogField fLocation;

		private String fPreviousExternalLocation;

		private static final String DIALOGSTORE_LAST_EXTERNAL_LOC = JavaUI.ID_PLUGIN + ".last.external.project"; //$NON-NLS-1$

		public LocationGroup() {
			fWorkspaceRadio = new SelectionButtonDialogField(SWT.RADIO);
			fWorkspaceRadio.setDialogFieldListener(this);
			fWorkspaceRadio.setLabelText("Use &default location");

			fExternalRadio = new SelectionButtonDialogField(SWT.RADIO);
			fExternalRadio.setLabelText("Use &external location");

			fLocation = new StringButtonDialogField(this);
			fLocation.setDialogFieldListener(this);
			fLocation.setLabelText(NewWizardMessages.NewJavaProjectWizardPageOne_LocationGroup_locationLabel_desc);
			fLocation.setButtonLabel(NewWizardMessages.NewJavaProjectWizardPageOne_LocationGroup_browseButton_desc);

			fExternalRadio.attachDialogField(fLocation);

			fWorkspaceRadio.setSelection(true);
			fExternalRadio.setSelection(false);

			fPreviousExternalLocation = ""; //$NON-NLS-1$
		}

		public Control createControl(Composite composite) {
			final int numColumns = 3;

			final Group group = new Group(composite, SWT.NONE);
			group.setLayout(initGridLayout(new GridLayout(numColumns, false), true));
			group.setText("Contents");

			fWorkspaceRadio.doFillIntoGrid(group, numColumns);
			fExternalRadio.doFillIntoGrid(group, numColumns);
			fLocation.doFillIntoGrid(group, numColumns);
			LayoutUtil.setHorizontalGrabbing(fLocation.getTextControl(null));

			return group;
		}

		protected void fireEvent() {
			setChanged();
			notifyObservers();
		}

		protected String getDefaultPath(String name) {
			final IPath path = Platform.getLocation().append(name);
			return path.toOSString();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Observer#update(java.util.Observable,
		 * java.lang.Object)
		 */
		public void update(Observable o, Object arg) {
			if (isWorkspaceRadioSelected()) {
				fLocation.setText(getDefaultPath(fNameGroup.getName()));
			}
			fireEvent();
		}

		public IPath getLocation() {
			if (isWorkspaceRadioSelected()) {
				return Platform.getLocation();
			}
			return Path.fromOSString(fLocation.getText().trim());
		}

		public boolean isWorkspaceRadioSelected() {
			return fWorkspaceRadio.isSelected();
		}

		/**
		 * Returns <code>true</code> if the location is in the workspace
		 * 
		 * @return <code>true</code> if the location is in the workspace
		 */
		public boolean isLocationInWorkspace() {
			final String location = fLocationGroup.getLocation().toOSString();
			IPath projectPath = Path.fromOSString(location);
			return Platform.getLocation().isPrefixOf(projectPath);
		}

		public void setLocation(IPath path) {
			fWorkspaceRadio.setSelection(path == null);
			if (path != null) {
				fLocation.setText(path.toOSString());
			}
			else {
				fLocation.setText(getDefaultPath(fNameGroup.getName()));
			}
			fireEvent();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jdt.internal.ui.wizards.dialogfields.IStringButtonAdapter
		 * #changeControlPressed(org.eclipse.jdt
		 * .internal.ui.wizards.dialogfields.DialogField)
		 */
		public void changeControlPressed(DialogField field) {
			final DirectoryDialog dialog = new DirectoryDialog(getShell());
			dialog.setMessage(NewWizardMessages.NewJavaProjectWizardPageOne_directory_message);
			String directoryName = fLocation.getText().trim();
			if (directoryName.length() == 0) {
				String prevLocation = JavaPlugin.getDefault().getDialogSettings().get(DIALOGSTORE_LAST_EXTERNAL_LOC);
				if (prevLocation != null) {
					directoryName = prevLocation;
				}
			}

			if (directoryName.length() > 0) {
				final File path = new File(directoryName);
				if (path.exists())
					dialog.setFilterPath(directoryName);
			}
			final String selectedDirectory = dialog.open();
			if (selectedDirectory != null) {
				String oldDirectory = new Path(fLocation.getText().trim()).lastSegment();
				fLocation.setText(selectedDirectory);
				String lastSegment = new Path(selectedDirectory).lastSegment();
				if (lastSegment != null
						&& (fNameGroup.getName().length() == 0 || fNameGroup.getName().equals(oldDirectory))) {
					fNameGroup.setName(lastSegment);
				}
				JavaPlugin.getDefault().getDialogSettings().put(DIALOGSTORE_LAST_EXTERNAL_LOC, selectedDirectory);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener
		 * #dialogFieldChanged(org.eclipse.jdt.
		 * internal.ui.wizards.dialogfields.DialogField)
		 */
		public void dialogFieldChanged(DialogField field) {
			if (field == fWorkspaceRadio) {
				final boolean checked = fWorkspaceRadio.isSelected();
				if (checked) {
					fPreviousExternalLocation = fLocation.getText();
					fLocation.setText(getDefaultPath(fNameGroup.getName()));
				}
				else {
					fLocation.setText(fPreviousExternalLocation);
				}
			}
			fireEvent();
		}
	}

	// TODO e3.5 replace by IWorkingSetIDs.JAVA
	public static final String IWorkingSetIDs_JAVA = "org.eclipse.jdt.ui.JavaWorkingSetPage";

	// TODO e3.5 replace by IWorkingSetIDs.RESOURCE
	public static final String IWorkingSetIDs_RESOURCE = "org.eclipse.ui.resourceWorkingSetPage";

	private final class WorkingSetGroup {

		private WorkingSetConfigurationBlock fWorkingSetBlock;

		public WorkingSetGroup() {
			String[] workingSetIds = new String[] { IWorkingSetIDs_JAVA, IWorkingSetIDs_RESOURCE };
			fWorkingSetBlock = new WorkingSetConfigurationBlock(workingSetIds, JavaPlugin.getDefault()
					.getDialogSettings());
			// fWorkingSetBlock.setDialogMessage(NewWizardMessages.NewJavaProjectWizardPageOne_WorkingSetSelection_message);
		}

		public Control createControl(Composite composite) {
			Group workingSetGroup = new Group(composite, SWT.NONE);
			workingSetGroup.setFont(composite.getFont());
			workingSetGroup.setText(NewWizardMessages.NewJavaProjectWizardPageOne_WorkingSets_group);
			workingSetGroup.setLayout(new GridLayout(1, false));

			fWorkingSetBlock.createContent(workingSetGroup);

			return workingSetGroup;
		}

		public void setWorkingSets(IWorkingSet[] workingSets) {
			fWorkingSetBlock.setWorkingSets(workingSets);
		}

		public IWorkingSet[] getSelectedWorkingSets() {
			return fWorkingSetBlock.getSelectedWorkingSets();
		}
	}

	/**
	 * Validate this page and show appropriate warnings and error
	 * NewWizardMessages.
	 */
	private final class Validator implements Observer {

		private boolean firstValidation = true;

		private IStatus installError = null;

		public void update(Observable o, Object arg) {

			final IWorkspace workspace = JavaPlugin.getWorkspace();

			final String name = fNameGroup.getName();
			// check whether the project name field is empty
			if (name.length() == 0) {
				setErrorMessage(null);
				setMessage(NewWizardMessages.NewJavaProjectWizardPageOne_Message_enterProjectName);
				setPageComplete(false);
				return;
			}

			// check whether the project name is valid
			final IStatus nameStatus = workspace.validateName(name, IResource.PROJECT);
			if (!nameStatus.isOK()) {
				setErrorMessage(nameStatus.getMessage());
				setPageComplete(false);
				return;
			}

			// check whether project already exists
			final IProject handle = workspace.getRoot().getProject(name);
			if (handle.exists()) {
				setErrorMessage(NewWizardMessages.NewJavaProjectWizardPageOne_Message_projectAlreadyExists);
				setPageComplete(false);
				return;
			}

			// check whether package name is valid
			final String packageName = fNameGroup.getPackageName();
			if (packageName.length() == 0) {
				setErrorMessage(null);
				setMessage("Enter a top level package name.");
				setPageComplete(false);
				return;
			}
			if (JavaConventions.validatePackageName(packageName, JavaCore.VERSION_1_3, JavaCore.VERSION_1_3)
					.getSeverity() == IStatus.ERROR) {
				setErrorMessage(null);
				setMessage("The entered top level package name is not valid.");
				setPageComplete(false);
				return;
			}

			IPath projectLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(name);
			if (projectLocation.toFile().exists()) {
				try {
					// correct casing
					String canonicalPath = projectLocation.toFile().getCanonicalPath();
					projectLocation = new Path(canonicalPath);
				}
				catch (IOException e) {
					JavaPlugin.log(e);
				}

				String existingName = projectLocation.lastSegment();
				if (!existingName.equals(fNameGroup.getName())) {
					setErrorMessage(Messages.format(
							NewWizardMessages.NewJavaProjectWizardPageOne_Message_invalidProjectNameForWorkspaceRoot,
							BasicElementLabels.getResourceName(existingName)));
					setPageComplete(false);
					return;
				}

				setErrorMessage("A project at specified location already exists.");
				setPageComplete(false);
				return;

			}

			final String location = fLocationGroup.getLocation().toOSString();

			// check whether location is empty
			if (location.length() == 0) {
				setErrorMessage(null);
				setMessage(NewWizardMessages.NewJavaProjectWizardPageOne_Message_enterLocation);
				setPageComplete(false);
				return;
			}

			// check whether the location is a syntactically correct path
			if (!Path.EMPTY.isValidPath(location)) {
				setErrorMessage(NewWizardMessages.NewJavaProjectWizardPageOne_Message_invalidDirectory);
				setPageComplete(false);
				return;
			}

			IPath projectPath = Path.fromOSString(location);

			if (fLocationGroup.isWorkspaceRadioSelected())
				projectPath = projectPath.append(fNameGroup.getName());

			if (projectPath.toFile().exists()) {// create from existing source
				if (Platform.getLocation().isPrefixOf(projectPath)) { // create
																		// from
																		// existing
																		// source
																		// in
																		// workspace
					if (!Platform.getLocation().equals(projectPath.removeLastSegments(1))) {
						setErrorMessage(NewRooWizardMessages.NewRooProjectWizardPageOne_Message_notOnWorkspaceRoot);
						setPageComplete(false);
						return;
					}

					if (!projectPath.toFile().exists()) {
						setErrorMessage(NewRooWizardMessages.NewRooProjectWizardPageOne_notExisingProjectOnWorkspaceRoot);
						setPageComplete(false);
						return;
					}
				}
			}
			else if (!fLocationGroup.isWorkspaceRadioSelected()) {// create at
																	// non
																	// existing
																	// external
																	// location
				if (!canCreate(projectPath.toFile())) {
					setErrorMessage(NewWizardMessages.NewJavaProjectWizardPageOne_Message_cannotCreateAtExternalLocation);
					setPageComplete(false);
					return;
				}

				// If we do not place the contents in the workspace validate the
				// location.
				final IStatus locationStatus = workspace.validateProjectLocation(handle, projectPath);
				if (!locationStatus.isOK()) {
					setErrorMessage(locationStatus.getMessage());
					setPageComplete(false);
					return;
				}
			}

			if (firstValidation) {
				firstValidation = false;
				if (RooCoreActivator.getDefault().getInstallManager().getDefaultRooInstall() == null) {
					setErrorMessage("No Roo installation configured in workspace preferences.");
					setPageComplete(false);
					return;
				}
			}
			else {
				IRooInstall install = null;
				if (useDefaultRooInstall()) {
					install = RooCoreActivator.getDefault().getInstallManager().getDefaultRooInstall();
				}
				else {
					String installName = getRooInstallName();
					if (installName != null) {
						install = RooCoreActivator.getDefault().getInstallManager().getRooInstall(installName);
					}
				}
				if (install == null) {
					setErrorMessage("No Roo installation configured in workspace preferences.");
					setPageComplete(false);
					return;
				}
				else {
					installError = install.validate();
					if (installError != null && !installError.isOK()) {
						setErrorMessage(installError.getMessage());
						setPageComplete(false);
						return;
					}
				}
			}

			setPageComplete(true);
			setErrorMessage(null);
			setMessage(null);
		}

		private boolean canCreate(File file) {
			while (!file.exists()) {
				file = file.getParentFile();
				if (file == null)
					return false;
			}

			return file.canWrite();
		}
	}

	private static final String PAGE_NAME = "NewRooProjectWizardPageOne"; //$NON-NLS-1$

	private final NameGroup fNameGroup;

	private final LocationGroup fLocationGroup;

	private final RooInstallGroup rooInstallGroup;

	private final DependencyManagementGroup dependencyManagementGroup;
	
	private final PackagingProviderGroup packagingProviderGroup;

	private final Validator fValidator;

	private final WorkingSetGroup fWorkingSetGroup;

	private final IRooInstallListener listener;

	/**
	 * Creates a new {@link NewRooProjectWizardPageOne}.
	 */
	public NewRooProjectWizardPageOne() {
		super(PAGE_NAME);
		setPageComplete(false);
		setTitle("Create a new Roo Project");
		setDescription("Create a Roo project in the workspace or in an external location.");

		fNameGroup = new NameGroup();
		fLocationGroup = new LocationGroup();
		fWorkingSetGroup = new WorkingSetGroup();
		rooInstallGroup = new RooInstallGroup();
		dependencyManagementGroup = new DependencyManagementGroup();
		packagingProviderGroup = new PackagingProviderGroup();
		listener = new InstallChangeListener();

		RooCoreActivator.getDefault().getInstallManager().addRooInstallListener(listener);

		// establish connections
		fNameGroup.addObserver(fLocationGroup);

		// initialize all elements
		fNameGroup.notifyObservers();

		// create and connect validator
		fValidator = new Validator();
		fNameGroup.addObserver(fValidator);
		fLocationGroup.addObserver(fValidator);
		rooInstallGroup.addObserver(fValidator);
		
		rooInstallGroup.addObserver(packagingProviderGroup);
		
		// initialize defaults
		setProjectName(""); //$NON-NLS-1$
		setProjectLocationURI(null);
		setWorkingSets(new IWorkingSet[0]);
	}

	@Override
	public void dispose() {
		RooCoreActivator.getDefault().getInstallManager().removeRooInstallListener(listener);
		super.dispose();
	}

	/**
	 * The wizard owning this page can call this method to initialize the fields
	 * from the current selection and active part.
	 * 
	 * @param selection used to initialize the fields
	 * @param activePart the (typically active) part to initialize the fields or
	 * <code>null</code>
	 */
	public void init(IStructuredSelection selection, IWorkbenchPart activePart) {
		setWorkingSets(getSelectedWorkingSet(selection, activePart));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		final Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(initGridLayout(new GridLayout(1, false), true));
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		// create UI elements
		Control nameControl = createNameControl(composite);
		nameControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Control rooHomeControl = rooInstallGroup.createControl(composite);
		rooHomeControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		if (DependencyManagementUtils.IS_M2ECLIPSE_PRESENT || DependencyManagementUtils.IS_STS_MAVEN_PRESENT
				|| LegacyDependencyManagementUtils.IS_M2ECLIPSE_PRESENT || LegacyDependencyManagementUtils.IS_STS_MAVEN_PRESENT) {
			Control dependencyManagementControl = dependencyManagementGroup.createControl(composite);
			dependencyManagementControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}
		
		Control providerControl = packagingProviderGroup.createControl(composite);
		providerControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Control locationControl = createLocationControl(composite);
		locationControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Control workingSetControl = createWorkingSetControl(composite);
		workingSetControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		setControl(composite);
	}

	protected void setControl(Control newControl) {
		Dialog.applyDialogFont(newControl);

		PlatformUI.getWorkbench().getHelpSystem().setHelp(newControl, IJavaHelpContextIds.NEW_JAVAPROJECT_WIZARD_PAGE);

		super.setControl(newControl);
	}

	/**
	 * Creates the controls for the name field.
	 * 
	 * @param composite the parent composite
	 * @return the created control
	 */
	protected Control createNameControl(Composite composite) {
		return fNameGroup.createControl(composite);
	}

	/**
	 * Creates the controls for the location field.
	 * 
	 * @param composite the parent composite
	 * @return the created control
	 */
	protected Control createLocationControl(Composite composite) {
		return fLocationGroup.createControl(composite);
	}

	/**
	 * Creates the controls for the working set selection.
	 * 
	 * @param composite the parent composite
	 * @return the created control
	 */
	protected Control createWorkingSetControl(Composite composite) {
		return fWorkingSetGroup.createControl(composite);
	}

	/**
	 * Gets a project name for the new project.
	 * 
	 * @return the new project resource handle
	 */
	public String getProjectName() {
		return fNameGroup.getName();
	}

	public String getPackageName() {
		return fNameGroup.getPackageName();
	}

	public String getDescription() {
		return fNameGroup.getDescription();
	}

	public ProjectType getProjectType() {
		return fNameGroup.getProjectType();
	}

	/**
	 * Sets the name of the new project
	 * 
	 * @param name the new name
	 */
	public void setProjectName(String name) {
		if (name == null)
			throw new IllegalArgumentException();

		fNameGroup.setName(name);
	}

	/**
	 * Returns the current project location path as entered by the user, or
	 * <code>null</code> if the project should be created in the workspace.
	 * 
	 * @return the project location path or its anticipated initial value.
	 */
	public URI getProjectLocationURI() {
		if (fLocationGroup.isLocationInWorkspace()) {
			return null;
		}
		return URIUtil.toURI(fLocationGroup.getLocation());
	}
	
	public boolean isExternalProject() {
		return fLocationGroup.fExternalRadio.isSelected();
	}

	public String getRooInstallName() {
		if (rooInstallGroup.rooInstallCombo.getSelectionIndex() >= 0) {
			return rooInstallGroup.rooInstallCombo.getItem(rooInstallGroup.rooInstallCombo.getSelectionIndex());
		}
		return null;
	}

	public boolean useDefaultRooInstall() {
		return rooInstallGroup.useDefault.getSelection();
	}

	public DependencyManagement getDependencyManagement() {
		if (dependencyManagementGroup.dependencyManagementCombo != null
				&& dependencyManagementGroup.dependencyManagementCombo.isEnabled()) {
			String dm = dependencyManagementGroup.dependencyManagementCombo
					.getItem(dependencyManagementGroup.dependencyManagementCombo.getSelectionIndex());
			if ("Full Maven build".equals(dm)) {
				return DependencyManagement.M2ECLIPSE;
			}
			else if ("Dependency management only".equals(dm)) {
				return DependencyManagement.MAVEN_STS;
			}
		}
		return DependencyManagement.NONE;
	}

	public String getPackagingProvider() {
		if (packagingProviderGroup.packagingGroup != null && packagingProviderGroup.packagingGroup.isEnabled()) {
			if (packagingProviderGroup.builtinButton.getSelection()) {
				return packagingProviderGroup.packagingProviderCombo.getText();
			} else if (packagingProviderGroup.customButton.getSelection()) {
				return packagingProviderGroup.packagingProviderText.getText();
			}
		}
		return "";
	}
	
	/**
	 * Sets the project location of the new project or <code>null</code> if the
	 * project should be created in the workspace
	 * 
	 * @param uri the new project location
	 */
	public void setProjectLocationURI(URI uri) {
		IPath path = uri != null ? URIUtil.toPath(uri) : null;
		fLocationGroup.setLocation(path);
	}

	/**
	 * Returns the working sets to which the new project should be added.
	 * 
	 * @return the selected working sets to which the new project should be
	 * added
	 */
	public IWorkingSet[] getWorkingSets() {
		return fWorkingSetGroup.getSelectedWorkingSets();
	}

	/**
	 * Sets the working sets to which the new project should be added.
	 * 
	 * @param workingSets the initial selected working sets
	 */
	public void setWorkingSets(IWorkingSet[] workingSets) {
		if (workingSets == null) {
			throw new IllegalArgumentException();
		}
		fWorkingSetGroup.setWorkingSets(workingSets);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			fNameGroup.postSetFocus();
		}
	}

	private GridLayout initGridLayout(GridLayout layout, boolean margins) {
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		if (margins) {
			layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
			layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		}
		else {
			layout.marginWidth = 0;
			layout.marginHeight = 0;
		}
		return layout;
	}

	private static final IWorkingSet[] EMPTY_WORKING_SET_ARRAY = new IWorkingSet[0];

	private IWorkingSet[] getSelectedWorkingSet(IStructuredSelection selection, IWorkbenchPart activePart) {
		IWorkingSet[] selected = getSelectedWorkingSet(selection);
		if (selected != null && selected.length > 0) {
			for (int i = 0; i < selected.length; i++) {
				if (!isValidWorkingSet(selected[i]))
					return EMPTY_WORKING_SET_ARRAY;
			}
			return selected;
		}

		if (!(activePart instanceof PackageExplorerPart))
			return EMPTY_WORKING_SET_ARRAY;

		PackageExplorerPart explorerPart = (PackageExplorerPart) activePart;
		if (explorerPart.getRootMode() == PackageExplorerPart.PROJECTS_AS_ROOTS) {
			// Get active filter
			IWorkingSet filterWorkingSet = explorerPart.getFilterWorkingSet();
			if (filterWorkingSet == null)
				return EMPTY_WORKING_SET_ARRAY;

			if (!isValidWorkingSet(filterWorkingSet))
				return EMPTY_WORKING_SET_ARRAY;

			return new IWorkingSet[] { filterWorkingSet };
		}
		else {
			// If we have been gone into a working set return the working set
			Object input = explorerPart.getViewPartInput();
			if (!(input instanceof IWorkingSet))
				return EMPTY_WORKING_SET_ARRAY;

			IWorkingSet workingSet = (IWorkingSet) input;
			if (!isValidWorkingSet(workingSet))
				return EMPTY_WORKING_SET_ARRAY;

			return new IWorkingSet[] { workingSet };
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private IWorkingSet[] getSelectedWorkingSet(IStructuredSelection selection) {
		if (!(selection instanceof ITreeSelection))
			return EMPTY_WORKING_SET_ARRAY;

		ITreeSelection treeSelection = (ITreeSelection) selection;
		if (treeSelection.isEmpty())
			return EMPTY_WORKING_SET_ARRAY;

		List elements = treeSelection.toList();
		if (elements.size() == 1) {
			Object element = elements.get(0);
			TreePath[] paths = treeSelection.getPathsFor(element);
			if (paths.length != 1)
				return EMPTY_WORKING_SET_ARRAY;

			TreePath path = paths[0];
			if (path.getSegmentCount() == 0)
				return EMPTY_WORKING_SET_ARRAY;

			Object candidate = path.getSegment(0);
			if (!(candidate instanceof IWorkingSet))
				return EMPTY_WORKING_SET_ARRAY;

			IWorkingSet workingSetCandidate = (IWorkingSet) candidate;
			if (isValidWorkingSet(workingSetCandidate))
				return new IWorkingSet[] { workingSetCandidate };

			return EMPTY_WORKING_SET_ARRAY;
		}

		ArrayList result = new ArrayList();
		for (Iterator iterator = elements.iterator(); iterator.hasNext();) {
			Object element = iterator.next();
			if (element instanceof IWorkingSet && isValidWorkingSet((IWorkingSet) element)) {
				result.add(element);
			}
		}
		return (IWorkingSet[]) result.toArray(new IWorkingSet[result.size()]);
	}

	private static boolean isValidWorkingSet(IWorkingSet workingSet) {
		String id = workingSet.getId();
		if (!IWorkingSetIDs_JAVA.equals(id) && !IWorkingSetIDs_RESOURCE.equals(id))
			return false;

		if (workingSet.isAggregateWorkingSet())
			return false;

		return true;
	}

	private class InstallChangeListener implements IRooInstallListener {

		public void installChanged(Set<IRooInstall> installs) {
			// Perform refresh from the UI thread if it's not already the current thread.
			if (Display.getCurrent() == null) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						if (getShell() != null && !getShell().isDisposed()) {
							rooInstallGroup.refresh();
						}
					}
				});
			} else {
				rooInstallGroup.refresh();
			}
		}

	}

}
