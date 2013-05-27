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
import java.util.List;
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
import org.springframework.ide.eclipse.wizard.template.infrastructure.ui.WizardUIInfoElement;
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
public class TemplateProjectConfiguration extends ProjectConfiguration {

	private final Shell shell;

	private final Map<String, Object> collectedInput;

	private final Map<String, String> inputKinds;

	private final TemplateProjectConfigurationDescriptor configurationDescriptor;

	public TemplateProjectConfiguration(TemplateProjectConfigurationDescriptor configurationDescriptor, Shell shell) {
		this.configurationDescriptor = configurationDescriptor;
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

		Template template = configurationDescriptor.getTemplate();
		IProject project = getProject();

		if (template == null) {
			handleError("No project template selected to configure the project: " + getProject().getName());
			return;
		}

		if (project == null) {
			handleError("Failed to create a project. No project available to configure.");
			return;
		}

		// Now collect all the template variable inputs
		IStatus status = collectInput();

		if (status == null || status.getSeverity() != IStatus.ERROR) {

			if (status != null && status.getSeverity() == IStatus.WARNING) {
				WizardPlugin.getDefault().getLog().log(status);
			}
			IPath newPath = configurationDescriptor.getProjectLocationPath();

			String[] topLevelPackageTokens = configurationDescriptor.getTopLevelPackageTokens();
			String projectName = project.getName();

			try {
				ProcessingInfo processingInfo = new NewProjectProcessingInfo(template.getZippedLocation(), getProject()
						.getName(), configurationDescriptor.getSpringVersion());

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
		else {
			throw new CoreException(status);
		}

	}

	@Override
	protected IProject create(IProgressMonitor monitor) throws CoreException {
		String projectName = configurationDescriptor.getProjectName();
		return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	}

	/**
	 * Collect user input and input kinds, and return status that indicates
	 * whether there were clashes or error when collecting input. Note that
	 * exceptions are not throw as the if there are issues which are just
	 * warnings, the template processing should still occur with the collected
	 * input. Error status will halt the template processing
	 * @return OK status, or warning if template processing should continue, or
	 * error if template processing should stop
	 */
	protected IStatus collectInput() {
		List<TemplateInputCollector> inputHandlers = configurationDescriptor.getInputHandlers();

		IStatus status = Status.OK_STATUS;

		for (TemplateInputCollector handler : inputHandlers) {
			Map<String, String> desInputKinds = handler.getInputKinds();
			if (desInputKinds != null) {
				inputKinds.putAll(desInputKinds);
			}

			Map<String, Object> desCollectedInput = handler.getCollectedInput();
			if (desCollectedInput != null) {
				collectedInput.putAll(desCollectedInput);
			}
		}

		// Also handle the Spring version replacement
		Template template = configurationDescriptor.getTemplate();
		String templateSpringVersion = template != null && template.getTemplateData() != null
				&& template.getTemplateData().getDescriptor() != null ? template.getTemplateData().getDescriptor()
				.getSpringVersion() : null;
		SpringVersion userDefinedSpringVersion = configurationDescriptor.getSpringVersion();
		// Check if there is another token that is also using the same value as
		// the template-defined Spring version.
		// If so, log a warning that Spring version cannot be replaced as the
		// token is used by some other token definition
		if (inputKinds.containsKey(templateSpringVersion)) {
			status = new Status(
					IStatus.WARNING,
					WizardPlugin.PLUGIN_ID,
					"Unable to replace Spring version: "
							+ templateSpringVersion
							+ " with "
							+ userDefinedSpringVersion
							+ " as the original Spring version token value is already used by another token definition. Check the token definitions in the template's wizard input definition file.");
		}
		else if (templateSpringVersion != null && userDefinedSpringVersion != null) {
			inputKinds.put(templateSpringVersion, WizardUIInfoElement.FIXED_TOKEN_KIND);
			collectedInput.put(templateSpringVersion, userDefinedSpringVersion.getVersion());
		}

		return status;

	}

}
