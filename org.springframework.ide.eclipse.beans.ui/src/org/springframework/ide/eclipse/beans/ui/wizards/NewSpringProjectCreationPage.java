/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.beans.ui.wizards;

import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.ui.SpringUIMessages;
import org.springframework.ide.eclipse.ui.SpringUIPlugin;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.util.StringUtils;

/**
 * @author Torsten Juergeleit
 */
public class NewSpringProjectCreationPage extends WizardNewProjectCreationPage {
	
	private Button isJavaButton;
	private Text extensionsText;

	public NewSpringProjectCreationPage(String pageName) {
		super(pageName);
	}

	public boolean isJavaProject() {
		return isJavaButton.getSelection();
	}

	public Set getConfigExtensions() {
		return StringUtils.commaDelimitedListToSet(extensionsText.getText());
	}

	/**
	 * Returns the runnable that will create the Java project. The runnable will create 
	 * and open the project if needed. The runnable will add the Java nature to the 
	 * project, and set the project's classpath and output location. 
	 * <p>
	 * To create the new java project, execute this runnable
	 * </p>
	 *
	 * @return the runnable
	 */		
	public IRunnableWithProgress getRunnable() {
		return new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor)
														 throws CoreException {
				if (monitor == null) {
					monitor = new NullProgressMonitor();
				}				
				monitor.beginTask(
							 BeansWizardsMessages.NewProject_createProject, 4); 
				try {
					createProject(monitor);
					addProjectNature(monitor);
				} finally {
					monitor.done();
				}
			}
		};
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		Composite control = (Composite)getControl();
		GridLayout layout = new GridLayout();
		layout.verticalSpacing = 10;
		control.setLayout(layout);

		createProjectTypeGroup(control);

		Dialog.applyDialogFont(control);
		setControl(control);
	}

	private void createProjectTypeGroup(Composite container) {
		Group group = new Group(container, SWT.NONE);
		group.setText(BeansWizardsMessages.NewProjectPage_settings);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		group.setLayout(layout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		isJavaButton = SpringUIUtils.createCheckBox(group,
									 BeansWizardsMessages.NewProjectPage_java);
		isJavaButton.setSelection(true);
		isJavaButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				setPageComplete(validatePage());
			}
		});

		extensionsText = SpringUIUtils.createTextField(group,
							   BeansWizardsMessages.NewProjectPage_extensions);
		extensionsText.setText(IBeansProject.DEFAULT_CONFIG_EXTENSION);
		extensionsText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setPageComplete(validatePage());
			}
		});
	}

    public boolean canFlipToNextPage() {
        return super.canFlipToNextPage() && isJavaProject();
    }

    protected boolean validatePage() {
    		boolean isValid = super.validatePage();
    		if (!isValid) {
    			return false;
    		}
    		String extensions = extensionsText.getText().trim();
    		if (extensions.length() == 0) {
    			setErrorMessage(BeansWizardsMessages.NewProjectPage_noExtensions);
    			return false;
    		}
		StringTokenizer tokenizer = new StringTokenizer(extensions, ",");
		while (tokenizer.hasMoreTokens()) {
			String extension = tokenizer.nextToken().trim();
			if (!isValidExtension(extension)) {
	    			setErrorMessage(BeansWizardsMessages.NewProjectPage_invalidExtensions);
	    			return false;
			}
		}
    		return true;
    }

	private boolean isValidExtension(String extension) {
		if (extension.length() == 0) {
			return false;
		} else {
			for (int i = 0; i < extension.length(); i++) {
				char c = extension.charAt(i);
				if (!Character.isLetterOrDigit(c)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Creates a project resource given the project handle and description.
	 * 
	 * @param monitor
	 * 				progress monitor to show visual progress with
	 * @exception OperationCanceledException
	 * 				if the operation is canceled or an error occured
	 */
	private void createProject(IProgressMonitor monitor)
											throws OperationCanceledException {
		monitor.subTask(BeansWizardsMessages.NewProject_createNewProject);

		IProject projectHandle = getProjectHandle();
		IPath newPath = null;
		if (!useDefaults()) {
			newPath = getLocationPath();
		} 
		
		IProjectDescription description = ResourcesPlugin.getWorkspace()
							   .newProjectDescription(projectHandle.getName());
		description.setLocation(newPath);

		try {

			// Create and open new project
			projectHandle.create(description,
								 new SubProgressMonitor(monitor, 1));
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			projectHandle.open(IResource.BACKGROUND_REFRESH,
							   new SubProgressMonitor(monitor, 1));
		} catch (CoreException e) {
			if (e.getStatus().getCode() ==
										 IResourceStatus.CASE_VARIANT_EXISTS) {
				MessageDialog.openError(getShell(),
					BeansWizardsMessages.NewProject_errorMessage,
					NLS.bind(
						BeansWizardsMessages.NewProject_caseVariantExistsError,
						projectHandle.getName()));
			} else {
				ErrorDialog.openError(getShell(),
							BeansWizardsMessages.NewProject_errorMessage, null,
							e.getStatus());
			}
			throw new OperationCanceledException();
		}
	}

	/**
	 * Adds Spring nature and beans config file extensions to given project.
	 * @param monitor  progress monitor to show visual progress with
	 * @exception OperationCanceledException  if the operation is canceled
	 */
	private void addProjectNature(IProgressMonitor monitor)
											throws OperationCanceledException {
		monitor.subTask(BeansWizardsMessages.NewProject_addProjectNature);
		IProject projectHandle = getProjectHandle();
		try {
			SpringCoreUtils.addProjectNature(projectHandle, SpringCore.NATURE_ID);
			monitor.worked(1);
			if (monitor != null && monitor.isCanceled()) {
				throw new OperationCanceledException();
			}
			BeansProject project = new BeansProject(projectHandle);
			project.setConfigExtensions(getConfigExtensions(), true);
			monitor.worked(1);
		} catch (CoreException e) {
			MessageDialog.openError( SpringUIPlugin.getActiveWorkbenchShell(),
					SpringUIMessages.ProjectNature_errorMessage,
					NLS.bind(SpringUIMessages.ProjectNature_addError,
							projectHandle.getName(), e.getLocalizedMessage()));
		}
	}
}
