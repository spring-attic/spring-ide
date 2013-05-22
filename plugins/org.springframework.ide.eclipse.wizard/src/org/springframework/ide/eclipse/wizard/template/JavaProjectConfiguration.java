/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template;

import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wst.common.project.facet.core.internal.FacetedProjectNature;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCorePreferences;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

/**
 * Creates and configures a Java based project.
 * 
 */
public abstract class JavaProjectConfiguration extends TemplateProjectConfiguration {

	private final JavaProjectConfigurationDescriptor javaDescriptor;

	public JavaProjectConfiguration(JavaProjectConfigurationDescriptor javaDescriptor,
			TemplateProjectConfigurationDescriptor templateDescriptor, Shell shell) {
		super(templateDescriptor, shell);
		this.javaDescriptor = javaDescriptor;
	}

	@Override
	public void configureProject(IProgressMonitor monitor) throws CoreException {
		// Configure the template first
		super.configureProject(monitor);

		// Do additional Java configuration
		handleJavaConfiguration(monitor);
	}

	protected void handleJavaConfiguration(IProgressMonitor monitor) throws CoreException {
		if (javaDescriptor == null) {
			return;
		}

		if (getProject() == null) {
			return;
		}

		final SpringCorePreferences prefs = SpringCorePreferences.getProjectPreferences(getProject(),
				BeansCorePlugin.PLUGIN_ID);

		// get the data from the UI widgets
		final Set<String> configExtensions = javaDescriptor.getConfigSuffixes();
		final boolean enableImports = javaDescriptor.enableImports();
		final boolean enableProjectFacets = javaDescriptor.enableProjectFacets();
		final boolean ignoreMissingNamespaceHandlers = javaDescriptor.ignoreMissingNamespaceHandlers();
		final boolean loadHandlerFromClasspath = javaDescriptor.loadHandlerFromClasspath();
		final boolean disableNamespaceCaching = javaDescriptor.disableNamespaceCaching();
		final boolean useHighestXsdVersion = javaDescriptor.useHighestXsdVersion();
		final boolean useProjectSettings = javaDescriptor.useProjectSettings();

		// configure the new Spring project
		monitor.beginTask(NewSpringProjectWizardMessages.NewProject_createNewProject, 1000);
		configureSpringProject(getProject(), configExtensions, enableImports, new SubProgressMonitor(monitor, 1000));

		if (useProjectSettings) {
			prefs.putBoolean(BeansCorePlugin.PROJECT_PROPERTY_ID, useProjectSettings);
			prefs.putBoolean(BeansCorePlugin.NAMESPACE_DEFAULT_FROM_CLASSPATH_ID, useHighestXsdVersion);
			prefs.putBoolean(BeansCorePlugin.LOAD_NAMESPACEHANDLER_FROM_CLASSPATH_ID, loadHandlerFromClasspath);
			prefs.putBoolean(BeansCorePlugin.DISABLE_CACHING_FOR_NAMESPACE_LOADING_ID, disableNamespaceCaching);
		}
		prefs.putBoolean(BeansCorePlugin.IGNORE_MISSING_NAMESPACEHANDLER_PROPERTY, ignoreMissingNamespaceHandlers);

		// add project facet nature to enable corresponding
		// functionality
		if (enableProjectFacets) {
			SpringCoreUtils.addProjectNature(getProject(), FacetedProjectNature.NATURE_ID, monitor);
		}
		monitor.done();
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

}
