/*******************************************************************************
 *  Copyright (c) 2012, 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageTwo;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.wst.common.project.facet.core.internal.FacetedProjectNature;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCorePreferences;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.template.ProjectWizardDescriptor.BuildType;
import org.springframework.ide.eclipse.wizard.template.ProjectWizardDescriptor.ProjectType;

/**
 * Contributes the creation of a Java project with Spring configuration to the
 * New Spring Project Wizard. This only handles the case of a user selecting to
 * creation a simple Java project as the base for their Spring project using
 * Java builds. Maven projects are not handled by this section provider.
 * 
 */
public class JavaWizardSection extends SpringProjectWizardSection {

	private final NewJavaProjectWizardPageTwo javaPageTwo;

	private final NewSpringProjectCreationPage springCreationPage;

	public JavaWizardSection(NewSpringProjectWizard wizard) {
		super(wizard);
		springCreationPage = new NewSpringProjectCreationPage("Spring Configuration");
		springCreationPage.setWizard(getWizard());
		javaPageTwo = new NewJavaProjectWizardPageTwo(wizard.getMainPage());
		javaPageTwo.setWizard(getWizard());
	}

	public IJavaElement getCreatedElement() {
		return javaPageTwo.getJavaProject();
	}

	@Override
	public boolean canFinish() {
		if (springCreationPage.isPageComplete()) {
			return javaPageTwo.isPageComplete();
		}
		return false;
	}

	@Override
	public boolean configureProject(final IProject project) {

		final SpringCorePreferences prefs = SpringCorePreferences.getProjectPreferences(project,
				BeansCorePlugin.PLUGIN_ID);

		// get the data from the UI widgets
		final Set<String> configExtensions = springCreationPage.getConfigSuffixes();
		final boolean enableImports = springCreationPage.enableImports();
		final boolean enableProjectFacets = springCreationPage.enableProjectFacets();
		final boolean ignoreMissingNamespaceHandlers = springCreationPage.ignoreMissingNamespaceHandlers();
		final boolean loadHandlerFromClasspath = springCreationPage.loadHandlerFromClasspath();
		final boolean disableNamespaceCaching = springCreationPage.disableNamespaceCaching();
		final boolean useHighestXsdVersion = springCreationPage.useHighestXsdVersion();
		final boolean useProjectSettings = springCreationPage.useProjectSettings();

		// configure the new Spring project operation
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			@Override
			protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException,
					InterruptedException {
				monitor.beginTask(NewSpringProjectWizardMessages.NewProject_createNewProject, 1000);
				configureSpringProject(project, configExtensions, enableImports, new SubProgressMonitor(monitor, 1000));

				if (useProjectSettings) {
					prefs.putBoolean(BeansCorePlugin.PROJECT_PROPERTY_ID, useProjectSettings);
					prefs.putBoolean(BeansCorePlugin.NAMESPACE_DEFAULT_FROM_CLASSPATH_ID, useHighestXsdVersion);
					prefs.putBoolean(BeansCorePlugin.LOAD_NAMESPACEHANDLER_FROM_CLASSPATH_ID, loadHandlerFromClasspath);
					prefs.putBoolean(BeansCorePlugin.DISABLE_CACHING_FOR_NAMESPACE_LOADING_ID, disableNamespaceCaching);
				}
				prefs.putBoolean(BeansCorePlugin.IGNORE_MISSING_NAMESPACEHANDLER_PROPERTY,
						ignoreMissingNamespaceHandlers);

				// add project facet nature to enable corresponding
				// functionality
				if (enableProjectFacets) {
					SpringCoreUtils.addProjectNature(project, FacetedProjectNature.NATURE_ID, monitor);
				}
				monitor.done();
			}
		};

		// run the new project configuration operation
		try {
			getWizard().getContainer().run(true, true, op);
		}
		catch (InterruptedException e) {
			return false;
		}
		catch (InvocationTargetException e) {
			// ie.- one of the steps resulted in a core exception
			Throwable t = e.getTargetException();
			if (t instanceof CoreException) {
				if (((CoreException) t).getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS) {
					MessageDialog.openError(
							getWizard().getShell(),
							NewSpringProjectWizardMessages.NewProject_errorMessage,
							NLS.bind(NewSpringProjectWizardMessages.NewProject_caseVariantExistsError,
									project.getName()));
				}
				else {
					// no special message
					ErrorDialog.openError(getWizard().getShell(),
							NewSpringProjectWizardMessages.NewProject_errorMessage, null,
							((CoreException) t).getStatus());
				}
			}
			else {
				// CoreExceptions are handled above, but unexpected runtime
				// exceptions and errors may still occur.
				BeansUIPlugin.getDefault().getLog()
						.log(new Status(IStatus.ERROR, BeansUIPlugin.PLUGIN_ID, 0, t.toString(), t));
				MessageDialog.openError(getWizard().getShell(), NewSpringProjectWizardMessages.NewProject_errorMessage,
						NLS.bind(NewSpringProjectWizardMessages.NewProject_internalError, t.getMessage()));
			}
			return false;
		}

		IWorkingSet[] workingSets = getWizard().getWorkingSets();
		if (workingSets.length > 0) {
			PlatformUI.getWorkbench().getWorkingSetManager().addToWorkingSets(getCreatedElement(), workingSets);
		}
		getWizard().selectAndReveal(project);

		return true;
	}

	/**
	 * Configures the given project with a Spring nature and other Spring
	 * related properties.
	 * 
	 * @param projectHandle the project handle to create a project resource for
	 * @param monitor the progress monitor to show visual progress with
	 * @exception CoreException if the operation fails
	 * @exception OperationCanceledException if the operation is canceled
	 */
	protected void configureSpringProject(IProject projectHandle, Set<String> configExtensions, boolean enableImports,
			IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		try {
			monitor.beginTask(NewSpringProjectWizardMessages.NewProject_createProject, 4);

			SpringCoreUtils.addProjectNature(projectHandle, SpringCore.NATURE_ID, monitor);
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
		}
		finally {
			monitor.done();
		}
	}

	@Override
	public boolean canProvide(ProjectWizardDescriptor descriptor) {
		return ProjectType.SIMPLE == descriptor.getProjectType() && BuildType.Java == descriptor.getBuildType();
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {

		if (page == springCreationPage) {
			return javaPageTwo;
		}
		else if (page == getWizard().getMainPage()) {
			return springCreationPage;
		}
		else {
			return null;
		}
	}

	@Override
	public Control getMainPageControl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IProject createProject(IProgressMonitor monitor) throws CoreException {
		try {
			javaPageTwo.performFinish(monitor);
		}

		catch (InterruptedException e) {
			throw new CoreException(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, e.getMessage(), e));
		}
		return ((IJavaProject) getCreatedElement()).getProject();
	}

	@Override
	public void cancel() {
		javaPageTwo.performCancel();

	}

}
