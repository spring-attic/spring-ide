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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
import org.springframework.ide.eclipse.wizard.template.infrastructure.ui.WizardUIInfo;
import org.springframework.ide.eclipse.wizard.template.newproject.NewProjectProcessingInfo;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;

public class TemplateProjectConfiguration extends ProjectConfiguration {

	private final Shell shell;

	public TemplateProjectConfiguration(IProjectConfigurationDescriptor descriptor, Shell shell) {
		super(descriptor);
		this.shell = shell;
	}

	protected void handleError(String errorMessage) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, errorMessage));
	}

	protected void handleError(String errorMessage, Exception error) throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, errorMessage, error));
	}

	@Override
	public void configureProject(IProject project, IProgressMonitor monitor) throws CoreException {

		final TemplateProjectConfigurationDescriptor descriptor = (TemplateProjectConfigurationDescriptor) getConfigurationDescriptor();

		WizardUIInfo info = null;

		Template template = null;

		if (descriptor != null) {
			info = descriptor.getUiInfo();
			template = descriptor.getTemplate();
		}
		else {
			handleError("No descriptor to configure the project: " + project.getName() + " is available");
			return;
		}

		if (template == null) {
			handleError("No project template selected to configure the project: " + project.getName());
			return;
		}

		IPath newPath = descriptor.getProjectLocationPath();

		String[] topLevelPackageTokens = info != null ? info.getTopLevelPackageTokens() : null;
		String projectName = info != null ? info.getProjectNameToken() : project.getName();

		try {
			ProcessingInfo processingInfo = new NewProjectProcessingInfo(template.getZippedLocation(),
					project.getName());

			Processor processor = new Processor(processingInfo);
			IProject processedProject = processor.process(project, newPath, topLevelPackageTokens, projectName,
					descriptor.getCollectedInput(), descriptor.getInputKinds(), shell, monitor);
			if (processedProject != null) {
				processedProject.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
				SpringCoreUtils.buildFullProject(processedProject);
			}
		}
		catch (Exception e) {
			handleError("Failure while processing template for project: " + projectName, e);
		}
	}

}
