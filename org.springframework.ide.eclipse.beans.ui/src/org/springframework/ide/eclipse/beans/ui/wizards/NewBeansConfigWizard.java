/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * @author Torsten Juergeleit
 */
public class NewBeansConfigWizard extends Wizard implements INewWizard {

	private WizardNewFileCreationPage mainPage;
	private IStructuredSelection selection;
	private IBeansConfig newConfig;

	public NewBeansConfigWizard() {
		setDialogSettings(BeansUIPlugin.getDefault().getDialogSettings());
	}

	/**
	 * Returns the newly created beans config.
	 * 
	 * @return the created beans config, or <code>null</code> if config not
	 * 			 created
	 */
	public IBeansConfig getNewConfig() {
		return newConfig;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.selection = selection;
		setNeedsProgressMonitor(true);
//		setWindowTitle(BeansWizardsMessages.NewConfig_windowTitle);
		setDefaultPageImageDescriptor(BeansUIImages.DESC_WIZ_CONFIG);
	}

	@Override
	public void addPages() {
		super.addPages();
		mainPage = new WizardNewFileCreationPage("springNewConfigPage", selection);
//		mainPage.setTitle(BeansWizardsMessages.NewConfig_title);
//		mainPage.setDescription(BeansWizardsMessages.NewConfig_description);
		addPage(mainPage);
	}

	@Override
	public boolean performFinish() {
		createNewConfig();
		if (newConfig == null) {
			return false;
		}

		BasicNewResourceWizard.selectAndReveal(newConfig.getElementResource(),
				BeansUIPlugin.getActiveWorkbenchWindow());
		SpringUIUtils.openInEditor(newConfig);
		return true;
	}

    /**
     * Creates a new file resource in the selected container and with the selected
     * name. Creates any missing resource containers along the path; does nothing if
     * the container resources already exist.
     * <p>
     * In normal usage, this method is invoked after the user has pressed Finish on
     * the wizard; the enablement of the Finish button implies that all controls on 
     * on this page currently contain valid values.
     * </p>
     * <p>
     * Note that this page caches the new file once it has been successfully
     * created; subsequent invocations of this method will answer the same
     * file resource without attempting to create it again.
     * </p>
     * <p>
     * This method should be called within a workspace modify operation since
     * it creates resources.
     * </p>
     *
     * @return the created file resource, or <code>null</code> if the file
     *    was not created
     */
	private void createNewConfig() {
//
//		// get the data from the UI widgets
//		final IProject projectHandle = mainPage.getProjectHandle();
//		final Set configExtensions = mainPage.getConfigExtensions();
//		final boolean isJavaProject = mainPage.isJavaProject();
//
//		// get a project descriptor
//		IPath newPath = null;
//		if (!mainPage.useDefaults()) {
//			newPath = mainPage.getLocationPath();
//		}
//
//		IWorkspace workspace = ResourcesPlugin.getWorkspace();
//		final IProjectDescription description = workspace
//				.newProjectDescription(projectHandle.getName());
//		description.setLocation(newPath);
//
//		// create the new Spring project operation
//		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
//			protected void execute(IProgressMonitor monitor)
//					throws CoreException {
//				monitor.beginTask(BeansWizardsMessages
//						.NewProject_createNewProject, 2000);
//				createSpringProject(projectHandle, description,
//						configExtensions, new SubProgressMonitor(monitor,
//								1000));
//				if (isJavaProject) {
//					javaPage.init(JavaCore.create(projectHandle), javaPage
//							.getOutputLocation(), javaPage.getRawClassPath(),
//							false);
//					try {
//						javaPage.configureJavaProject(new SubProgressMonitor(
//								monitor, 1000));
//					} catch (InterruptedException e) {
//						throw new OperationCanceledException(e.getMessage());
//					}
//				}
//				monitor.done();
//			}
//		};
//
//		// run the new project creation operation
//		try {
//			getContainer().run(true, true, op);
//		} catch (InterruptedException e) {
//			return null;
//		} catch (InvocationTargetException e) {
//			// ie.- one of the steps resulted in a core exception
//			Throwable t = e.getTargetException();
//			if (t instanceof CoreException) {
//				if (((CoreException) t).getStatus().getCode() ==
//							IResourceStatus.CASE_VARIANT_EXISTS) {
//					MessageDialog.openError(getShell(),
//								BeansWizardsMessages.NewProject_errorMessage,
//								NLS.bind(BeansWizardsMessages.
//										NewProject_caseVariantExistsError,
//													projectHandle.getName()));
//				} else {
//					ErrorDialog.openError(getShell(),
//							BeansWizardsMessages.NewProject_errorMessage,
//							null, // no special message
//							((CoreException) t).getStatus());
//				}
//			} else {
//				// CoreExceptions are handled above, but unexpected runtime
//				// exceptions and errors may still occur.
//				BeansUIPlugin.getDefault().getLog().log(new Status(
//						IStatus.ERROR, BeansUIPlugin.PLUGIN_ID, 0,
//						t.toString(), t));
//				MessageDialog.openError(getShell(),
//						BeansWizardsMessages.NewProject_errorMessage,
//						NLS.bind(BeansWizardsMessages.NewProject_internalError,
//								t.getMessage()));
//			}
//			return null;
//		}
//
//		newConfig = projectHandle;
//		return newConfig;
	}
}
