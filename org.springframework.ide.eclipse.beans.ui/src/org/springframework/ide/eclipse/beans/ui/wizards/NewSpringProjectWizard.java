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

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewProjectReferencePage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

/**
 * @author Torsten Juergeleit
 */
public class NewSpringProjectWizard extends Wizard
			implements INewWizard, IExecutableExtension  {

	private NewSpringProjectCreationPage mainPage;
	private WizardNewProjectReferencePage referencePage;
	private JavaCapabilityConfigurationPage javaPage;

    private IConfigurationElement configElement;
	private IProject newProject;

	public NewSpringProjectWizard() {
		setDialogSettings(BeansUIPlugin.getDefault().getDialogSettings());
	}

	/**
	 * Returns the newly created project.
	 * 
	 * @return the created project, or <code>null</code> if project not
	 *         created
	 */
	public IProject getNewProject() {
		return newProject;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setNeedsProgressMonitor(true);
		setWindowTitle(BeansWizardsMessages.NewProject_windowTitle);
		setDefaultPageImageDescriptor(BeansUIImages.DESC_WIZ_PROJECT);
	}

	public void addPages() {
		super.addPages();
		mainPage = new NewSpringProjectCreationPage("springNewProjectPage");
		mainPage.setTitle(BeansWizardsMessages.NewProject_title);
		mainPage.setDescription(BeansWizardsMessages.NewProject_description);
		addPage(mainPage);

		// only add page if there are already projects in the workspace
		if (ResourcesPlugin.getWorkspace().getRoot().getProjects().length > 0) {
			referencePage = new WizardNewProjectReferencePage(
					"springReferenceProjectPage");
			referencePage.setTitle(BeansWizardsMessages.
					NewProject_referenceTitle);
			referencePage.setDescription(BeansWizardsMessages.
					NewProject_referenceDescription);
			addPage(referencePage);
		}

		javaPage = new JavaCapabilityConfigurationPage();
		addPage(javaPage);
	}

	public IWizardPage getNextPage(IWizardPage page) {
		IWizardPage nextPage = super.getNextPage(page);
		if (nextPage instanceof JavaCapabilityConfigurationPage) {
			((JavaCapabilityConfigurationPage) nextPage).init(JavaCore.create(mainPage.getProjectHandle()), null, null, false);
		}
		return nextPage;
	}

    /**
     * Stores the configuration element for the wizard. The config element will
     * be used in <code>performFinish</code> to set the result perspective.
     */
    public void setInitializationData(IConfigurationElement cfig,
            String propertyName, Object data) {
        configElement = cfig;
    }

	public boolean performFinish() {
		createNewProject();
		if (newProject == null) {
			return false;
		}

		BasicNewProjectResourceWizard.updatePerspective(configElement);
		BasicNewResourceWizard.selectAndReveal(newProject,
				BeansUIPlugin.getActiveWorkbenchWindow());
		return true;
	}

	/**
	 * Creates a new project resource with the selected name.
	 * <p>
	 * In normal usage, this method is invoked after the user has pressed Finish
	 * on the wizard; the enablement of the Finish button implies that all
	 * controls on the pages currently contain valid values.
	 * </p>
	 * <p>
	 * Note that this wizard caches the new project once it has been
	 * successfully created; subsequent invocations of this method will answer
	 * the same project resource without attempting to create it again.
	 * </p>
	 * 
	 * @return the created project resource, or <code>null</code> if the
	 *         project was not created
	 */
	private IProject createNewProject() {
		if (newProject != null) {
			return newProject;
		}

		// get the data from the UI widgets
		final IProject projectHandle = mainPage.getProjectHandle();
		final Set configExtensions = mainPage.getConfigExtensions();
		final boolean isJavaProject = mainPage.isJavaProject();

		// get a project descriptor
		IPath newPath = null;
		if (!mainPage.useDefaults()) {
			newPath = mainPage.getLocationPath();
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProjectDescription description = workspace
				.newProjectDescription(projectHandle.getName());
		description.setLocation(newPath);

		// create the new Spring project operation
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor)
					throws CoreException {
				createSpringProject(projectHandle, description,
						configExtensions, monitor);
				if (isJavaProject) {
					javaPage.init(JavaCore.create(projectHandle),
							javaPage.getOutputLocation(),
							javaPage.getRawClassPath(), false);
					try {
						javaPage.configureJavaProject(monitor);
					} catch (InterruptedException e) {
						throw new OperationCanceledException(e.getMessage());
					}
				}
			}
		};

		// run the new project creation operation
		try {
			getContainer().run(true, true, op);
		} catch (InterruptedException e) {
			return null;
		} catch (InvocationTargetException e) {
			// ie.- one of the steps resulted in a core exception
			Throwable t = e.getTargetException();
			if (t instanceof CoreException) {
				if (((CoreException) t).getStatus().getCode() ==
							IResourceStatus.CASE_VARIANT_EXISTS) {
					MessageDialog.openError(getShell(),
								BeansWizardsMessages.NewProject_errorMessage,
								NLS.bind(BeansWizardsMessages.
										NewProject_caseVariantExistsError,
													projectHandle.getName()));
				} else {
					ErrorDialog.openError(getShell(),
							BeansWizardsMessages.NewProject_errorMessage,
							null, // no special message
							((CoreException) t).getStatus());
				}
			} else {
				// CoreExceptions are handled above, but unexpected runtime
				// exceptions and errors may still occur.
				BeansUIPlugin.getDefault().getLog().log(new Status(
						IStatus.ERROR, BeansUIPlugin.PLUGIN_ID, 0,
						t.toString(), t));
				MessageDialog.openError(getShell(),
						BeansWizardsMessages.NewProject_errorMessage,
						NLS.bind(BeansWizardsMessages.NewProject_internalError,
								t.getMessage()));
			}
			return null;
		}

		newProject = projectHandle;
		return newProject;
	}

	/**
	 * Creates a project resource given the project handle and description.
	 * 
	 * @param projectHandle
	 *            the project handle to create a project resource for
	 * @param description
	 *            the project description to create a project resource for
	 * @param monitor
	 *            the progress monitor to show visual progress with
	 * 
	 * @exception CoreException
	 *                if the operation fails
	 * @exception OperationCanceledException
	 *                if the operation is canceled
	 */
	protected void createSpringProject(IProject projectHandle,
				IProjectDescription description, Set configExtensions,
				IProgressMonitor monitor) throws CoreException,
						OperationCanceledException {
		try {
			monitor.beginTask(BeansWizardsMessages.NewProject_createProject, 8);

			projectHandle.create(description, monitor);
			monitor.worked(2);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			projectHandle.open(IResource.BACKGROUND_REFRESH, monitor);
			monitor.worked(2);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			SpringCoreUtils.addProjectNature(projectHandle,
					SpringCore.NATURE_ID, monitor);
			monitor.worked(2);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			BeansProject project = new BeansProject(projectHandle);
			project.setConfigExtensions(configExtensions, true);
			monitor.worked(2);
		} finally {
			monitor.done();
		}
	}
}
