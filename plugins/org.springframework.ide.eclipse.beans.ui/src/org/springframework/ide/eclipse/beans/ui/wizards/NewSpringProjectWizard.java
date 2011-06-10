/*******************************************************************************
 * Copyright (c) 2006 - 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageTwo;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.wst.common.project.facet.core.internal.FacetedProjectNature;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCorePreferences;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

/**
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class NewSpringProjectWizard extends NewElementWizard implements
		IExecutableExtension {

	private IConfigurationElement configElement;

	private NewJavaProjectWizardPageOne firstPage;

	private NewJavaProjectWizardPageTwo lastPage;

	private NewSpringProjectCreationPage mainPage;

	public NewSpringProjectWizard() {
		setDefaultPageImageDescriptor(BeansUIImages.DESC_WIZ_PROJECT);
		setDialogSettings(BeansUIPlugin.getDefault().getDialogSettings());
		setWindowTitle(BeansWizardsMessages.NewProject_windowTitle);
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		firstPage = new NewJavaProjectWizardPageOne();
		firstPage.setTitle(BeansWizardsMessages.NewProject_title);
		firstPage.setDescription(BeansWizardsMessages.NewProject_description);
		addPage(firstPage);
		mainPage = new NewSpringProjectCreationPage("springNewProjectPage");
		mainPage.setTitle(BeansWizardsMessages.NewProject_springSettingsTitle);
		mainPage.setDescription(BeansWizardsMessages.NewProject_springSettingsDescription);
		addPage(mainPage);
		lastPage = new NewJavaProjectWizardPageTwo(firstPage);
		lastPage.setTitle(BeansWizardsMessages.NewProject_buildSettingsTitle);
		lastPage.setDescription(BeansWizardsMessages.NewProject_buildSettingsDescription);
		addPage(lastPage);
	}

	@Override
	public IJavaElement getCreatedElement() {
		return lastPage.getJavaProject();
	}

	@Override
	public boolean performCancel() {
		lastPage.performCancel();
		return super.performCancel();
	}

	@Override
	public boolean performFinish() {
		boolean res = super.performFinish();
		if (res) {
			final IJavaElement newElement = getCreatedElement();
			final IProject project = ((IJavaProject) getCreatedElement())
					.getProject();
			final SpringCorePreferences prefs = SpringCorePreferences
					.getProjectPreferences(project, BeansCorePlugin.PLUGIN_ID);

			// get the data from the UI widgets
			final Set<String> configExtensions = mainPage.getConfigSuffixes();
			final boolean enableImports = mainPage.enableImports();
			final boolean enableProjectFacets = mainPage.enableProjectFacets();
			final boolean ignoreMissingNamespaceHandlers = mainPage
					.ignoreMissingNamespaceHandlers();
			final boolean loadHandlerFromClasspath = mainPage
					.loadHandlerFromClasspath();
			final boolean loadHandlerFromSourceFolders = mainPage.loadHandlerFromSourceFolders();
			final boolean useHighestXsdVersion = mainPage
					.useHighestXsdVersion();
			final boolean useProjectSettings = mainPage.useProjectSettings();

			// configure the new Spring project operation
			WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
				@Override
				protected void execute(IProgressMonitor monitor)
						throws CoreException, InvocationTargetException,
						InterruptedException {
					monitor.beginTask(
							BeansWizardsMessages.NewProject_createNewProject,
							1000);
					configureSpringProject(project, configExtensions,
							enableImports,
							new SubProgressMonitor(monitor, 1000));

					if (useProjectSettings) {
						prefs.putBoolean(BeansCorePlugin.PROJECT_PROPERTY_ID,
								useProjectSettings);
						prefs.putBoolean(
								BeansCorePlugin.NAMESPACE_DEFAULT_FROM_CLASSPATH_ID,
								useHighestXsdVersion);
						prefs.putBoolean(
								BeansCorePlugin.LOAD_NAMESPACEHANDLER_FROM_CLASSPATH_ID,
								loadHandlerFromClasspath);
						prefs.putBoolean(BeansCorePlugin.LOAD_NAMESPACEHANDLER_FROM_SOURCE_FOLDERS_ID, loadHandlerFromSourceFolders);
					}
					prefs.putBoolean(
							BeansCorePlugin.IGNORE_MISSING_NAMESPACEHANDLER_PROPERTY,
							ignoreMissingNamespaceHandlers);

					// add project facet nature to enable corresponding
					// functionality
					if (enableProjectFacets) {
						SpringCoreUtils.addProjectNature(project,
								FacetedProjectNature.NATURE_ID, monitor);
					}
					monitor.done();
				}
			};

			// run the new project configuration operation
			try {
				getContainer().run(true, true, op);
			} catch (InterruptedException e) {
				return false;
			} catch (InvocationTargetException e) {
				// ie.- one of the steps resulted in a core exception
				Throwable t = e.getTargetException();
				if (t instanceof CoreException) {
					if (((CoreException) t).getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS) {
						MessageDialog
								.openError(
										getShell(),
										BeansWizardsMessages.NewProject_errorMessage,
										NLS.bind(
												BeansWizardsMessages.NewProject_caseVariantExistsError,
												project.getName()));
					} else {
						// no special message
						ErrorDialog.openError(getShell(),
								BeansWizardsMessages.NewProject_errorMessage,
								null, ((CoreException) t).getStatus());
					}
				} else {
					// CoreExceptions are handled above, but unexpected runtime
					// exceptions and errors may still occur.
					BeansUIPlugin
							.getDefault()
							.getLog()
							.log(new Status(IStatus.ERROR,
									BeansUIPlugin.PLUGIN_ID, 0, t.toString(), t));
					MessageDialog
							.openError(
									getShell(),
									BeansWizardsMessages.NewProject_errorMessage,
									NLS.bind(
											BeansWizardsMessages.NewProject_internalError,
											t.getMessage()));
				}
				return false;
			}

			IWorkingSet[] workingSets = firstPage.getWorkingSets();
			if (workingSets.length > 0) {
				PlatformUI.getWorkbench().getWorkingSetManager()
						.addToWorkingSets(newElement, workingSets);
			}
			selectAndReveal(project);
		}

		return res;
	}

	/**
	 * Stores the configuration element for the wizard. The config element will
	 * be used in <code>performFinish</code> to set the result perspective.
	 */
	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) {
		configElement = config;
	}

	/**
	 * Configures the given project with a Spring nature and other Spring
	 * related properties.
	 * 
	 * @param projectHandle
	 *            the project handle to create a project resource for
	 * @param monitor
	 *            the progress monitor to show visual progress with
	 * @exception CoreException
	 *                if the operation fails
	 * @exception OperationCanceledException
	 *                if the operation is canceled
	 */
	protected void configureSpringProject(IProject projectHandle,
			Set<String> configExtensions, boolean enableImports,
			IProgressMonitor monitor) throws CoreException,
			OperationCanceledException {
		try {
			monitor.beginTask(BeansWizardsMessages.NewProject_createProject, 4);

			SpringCoreUtils.addProjectNature(projectHandle,
					SpringCore.NATURE_ID, monitor);
			monitor.worked(2);
			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			IBeansModel model = BeansCorePlugin.getModel();
			BeansProject project = new BeansProject(model, projectHandle);
			project.setConfigSuffixes(configExtensions);
			project.setImportsEnabled(enableImports);
			project.saveDescription();
			monitor.worked(2);
		} finally {
			monitor.done();
		}
	}

	@Override
	protected void finishPage(IProgressMonitor monitor)
			throws InterruptedException, CoreException {
		lastPage.performFinish(monitor);
	}

	protected void handleFinishException(Shell shell,
			InvocationTargetException e) {
		String title = NewWizardMessages.JavaProjectWizard_op_error_title;
		String message = NewWizardMessages.JavaProjectWizard_op_error_create_message;
		ExceptionHandler.handle(e, getShell(), title, message);
	}

}
