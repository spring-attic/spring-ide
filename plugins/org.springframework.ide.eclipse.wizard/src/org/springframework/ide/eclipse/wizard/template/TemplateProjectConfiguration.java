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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.template.infrastructure.Template;
import org.springframework.ide.eclipse.wizard.template.infrastructure.processor.ProcessingInfo;
import org.springframework.ide.eclipse.wizard.template.infrastructure.processor.Processor;
import org.springframework.ide.eclipse.wizard.template.newproject.NewProjectProcessingInfo;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;

/**
 * Creates and configures a project based on a selected template. In some cases,
 * the template data is downloaded when the project is being created. For
 * example, this would be the case of templates that do not contribute any UI to
 * the New Spring Wizard, and are therefore downloaded and processed only when a
 * user clicks "Finish" in the wizard
 * 
 */
public abstract class TemplateProjectConfiguration extends ProjectConfiguration {

	private final Shell shell;

	private final Map<String, Object> collectedInput;

	private final Map<String, String> inputKinds;

	private IProject project;

	public TemplateProjectConfiguration(IProjectConfigurationDescriptor descriptor, Shell shell) {
		super(descriptor);
		this.shell = shell;
		collectedInput = new HashMap<String, Object>();
		inputKinds = new HashMap<String, String>();
	}

	/**
	 * User this constructor if no descriptor is available at the time that a
	 * request is made for a project configuration.
	 * @param shell required to display errors or prompt user while creating a
	 * project.
	 */
	public TemplateProjectConfiguration(Shell shell) {
		this(null, shell);
	}

	protected Shell getShell() {
		return shell;
	}

	protected void handleError(String errorMessage) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, errorMessage));
	}

	protected void handleError(String errorMessage, Exception error) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, errorMessage, error));
	}

	@Override
	public void configureProject(IProgressMonitor monitor) throws CoreException {

		final TemplateProjectConfigurationDescriptor descriptor = (TemplateProjectConfigurationDescriptor) getConfigurationDescriptor();

		Template template = null;

		if (descriptor == null) {
			handleError("No descriptor to configure the project: " + project.getName() + " is available");
			return;
		}
		template = descriptor.getTemplate();

		if (template == null) {
			handleError("No project template selected to configure the project: " + project.getName());
			return;
		}

		IPath newPath = descriptor.getProjectLocationPath();

		String[] topLevelPackageTokens = descriptor.getTopLevelPackageTokens();
		String projectName = project.getName();

		try {
			ProcessingInfo processingInfo = new NewProjectProcessingInfo(template.getZippedLocation(),
					project.getName(), descriptor.getSpringVersion());

			Processor processor = new Processor(processingInfo);
			IProject processedProject = processor.process(project, newPath, topLevelPackageTokens, projectName,
					collectedInput, inputKinds, shell, monitor);
			if (processedProject != null) {
				processedProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
				SpringCoreUtils.buildFullProject(processedProject);
			}
		}
		catch (Exception e) {
			handleError("Failure while processing template for project: " + projectName, e);
		}
	}

	@Override
	public IProject createProject(IProgressMonitor monitor) throws CoreException {

		final TemplateProjectConfigurationDescriptor descriptor = (TemplateProjectConfigurationDescriptor) getConfigurationDescriptor();

		collectInput(collectedInput, inputKinds);

		String projectName = descriptor.getProjectName();
		project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

		return project;
	}

	protected void collectInput(Map<String, Object> collectedInput, Map<String, String> inputKinds) {
		final TemplateProjectConfigurationDescriptor descriptor = (TemplateProjectConfigurationDescriptor) getConfigurationDescriptor();
		if (descriptor != null) {
			Map<String, String> desInputKinds = descriptor.getInputKinds();
			if (desInputKinds != null) {
				inputKinds.putAll(desInputKinds);
			}
			Map<String, Object> desCollectedInput = descriptor.getCollectedInput();
			if (desCollectedInput != null) {
				collectedInput.putAll(desCollectedInput);
			}
		}
	}

}
