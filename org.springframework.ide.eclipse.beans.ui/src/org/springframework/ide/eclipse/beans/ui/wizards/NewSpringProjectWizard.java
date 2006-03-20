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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPage;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.wizards.newresource.ResourceMessages;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

/**
 * @author Torsten Juergeleit
 */
public class NewSpringProjectWizard extends Wizard implements INewWizard {

	private NewSpringProjectCreationPage mainPage;
	private NewJavaProjectWizardPage javaPage;

    // cache of newly-created project
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

		javaPage = new NewJavaProjectWizardPage(null, mainPage);
		addPage(javaPage);
	}

    public boolean performFinish() {
    		if (createNewProject() != null) {
			SpringCoreUtils.addProjectNature(newProject, SpringCore.NATURE_ID);
			BeansProject project = new BeansProject(newProject);
			project.setConfigExtensions(mainPage.getConfigExtensions(), true);

			if (mainPage.isJavaProject()) {
				Display display = getShell().getDisplay();
				if (display != null) {
					display.syncExec(new Runnable() {
						public void run() {
							try {
								javaPage.getRunnable().run(null);
							} catch (InterruptedException e) {
							} catch (InvocationTargetException e) {
								// TODO
							}
						}
					});
				}
			}
		}
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

        // get a project handle
        final IProject newProjectHandle = mainPage.getProjectHandle();

        // get a project descriptor
        IPath newPath = null;
        if (!mainPage.useDefaults())
            newPath = mainPage.getLocationPath();

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IProjectDescription description = workspace
                .newProjectDescription(newProjectHandle.getName());
        description.setLocation(newPath);

        // create the new project operation
        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
            protected void execute(IProgressMonitor monitor)
                    throws CoreException {
                createProject(description, newProjectHandle, monitor);
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
                if (((CoreException) t).getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS) {
                    MessageDialog
                            .openError(
                                    getShell(),
                                    ResourceMessages.NewProject_errorMessage, 
                                    NLS.bind(ResourceMessages.NewProject_caseVariantExistsError, newProjectHandle.getName()) 
                            );
                } else {
                    ErrorDialog.openError(getShell(), ResourceMessages.NewProject_errorMessage,
                            null, // no special message
                            ((CoreException) t).getStatus());
                }
            } else {
                // CoreExceptions are handled above, but unexpected runtime
                // exceptions and errors may still occur.
                IDEWorkbenchPlugin.getDefault().getLog().log(
                        new Status(IStatus.ERROR,
                                IDEWorkbenchPlugin.IDE_WORKBENCH, 0, t
                                        .toString(), t));
                MessageDialog
                        .openError(
                                getShell(),
                                ResourceMessages.NewProject_errorMessage,
                                NLS.bind(ResourceMessages.NewProject_internalError, t.getMessage()));
            }
            return null;
        }
        newProject = newProjectHandle;
        return newProject;
    }

    /**
     * Creates a project resource given the project handle and description.
     * 
     * @param description
     *            the project description to create a project resource for
     * @param projectHandle
     *            the project handle to create a project resource for
     * @param monitor
     *            the progress monitor to show visual progress with
     * 
     * @exception CoreException
     *                if the operation fails
     * @exception OperationCanceledException
     *                if the operation is canceled
     */
    void createProject(IProjectDescription description, IProject projectHandle,
            IProgressMonitor monitor) throws CoreException,
            OperationCanceledException {
        try {
            monitor.beginTask("", 2000);//$NON-NLS-1$

            projectHandle.create(description, new SubProgressMonitor(monitor,
                    1000));

            if (monitor.isCanceled())
                throw new OperationCanceledException();

            projectHandle.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 1000));

        } finally {
            monitor.done();
        }
    }
}
