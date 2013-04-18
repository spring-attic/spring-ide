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

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.template.ProjectWizardDescriptor.BuildType;
import org.springframework.ide.eclipse.wizard.template.ProjectWizardDescriptor.ProjectType;
import org.springframework.ide.eclipse.wizard.template.infrastructure.processor.ProcessingInfo;
import org.springframework.ide.eclipse.wizard.template.infrastructure.processor.Processor;
import org.springframework.ide.eclipse.wizard.template.newproject.NewProjectProcessingInfo;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.springsource.ide.eclipse.commons.ui.UiStatusHandler;

/**
 * Section responsible for creating a Spring project from a template.
 * 
 */
public class TemplateWizardSection extends SpringProjectWizardSection {

	private TemplateSelectionWizardPage templateSelectionPage;

	public TemplateWizardSection(NewSpringProjectWizard wizard) {
		super(wizard);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == getWizard().getMainPage()) {
			if (templateSelectionPage == null) {
				templateSelectionPage = new TemplateSelectionWizardPage();
				templateSelectionPage.setWizard(getWizard());
			}
			return templateSelectionPage;
		}
		return null;
	}

	@Override
	public boolean canFinish() {
		return templateSelectionPage != null ? templateSelectionPage.isPageComplete() : false;
	}

	protected IPath getLocationPath() {
		URI uri = getWizard().getMainPage().getProjectLocationURI();
		if (uri != null) {
			return new Path(uri.toString());
		}
		return null;
	}

	@Override
	public boolean configureProject(final IProject projectHandle) {
		final Map<String, Object> collectedInput = new HashMap<String, Object>();
		final Map<String, String> inputKinds = new HashMap<String, String>();
		collectInput(collectedInput, inputKinds);

		IPath newPath = getLocationPath();

		final IPath finalPath = newPath;

		try {
			getWizard().getContainer().run(true, true, new IRunnableWithProgress() {

				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						ProcessingInfo info = new NewProjectProcessingInfo(templateSelectionPage.getProjectLocation(),
								projectHandle.getName());

						Processor processor = new Processor(info);
						IProject project = processor.process(projectHandle, finalPath, templateSelectionPage
								.getTopLevelPackage(), templateSelectionPage.getProjectNameToken(), collectedInput,
								inputKinds, getWizard().getContainer().getShell(), monitor);
						if (project != null) {
							selectAndReveal(project);
							try {
								project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
								SpringCoreUtils.buildFullProject(project);
							}
							catch (CoreException e) {
								UiStatusHandler.logAndDisplay(e.getStatus());
							}
						}
					}
					catch (Exception e) {
						StatusHandler.log(new Status(Status.ERROR, WizardPlugin.PLUGIN_ID,
								"Exception during template project creation", e));
					}
				}
			});
		}
		catch (InvocationTargetException e) {
			StatusHandler.log(new Status(Status.ERROR, WizardPlugin.PLUGIN_ID, "Template project creation failed", e));
		}
		catch (InterruptedException e) {
			StatusHandler.log(new Status(Status.ERROR, WizardPlugin.PLUGIN_ID, "Template project creation interrupted",
					e));
		}
		return projectHandle != null;
	}

	@Override
	public boolean canProvide(ProjectWizardDescriptor descriptor) {
		return ProjectType.TEMPLATE == descriptor.getProjectType() && BuildType.Maven == descriptor.getBuildType();
	}

	public void selectAndReveal(final IResource newResource) {
		getWizard().getContainer().getShell().getDisplay().syncExec(new Runnable() {

			public void run() {
				BasicNewResourceWizard.selectAndReveal(newResource, getWizard().getWorkbench()
						.getActiveWorkbenchWindow());
			}
		});
	}

	public void collectInput(Map<String, Object> collectedInput, Map<String, String> inputKinds) {
		IWizardPage page = templateSelectionPage.getNextPage();
		while (page != null) {
			if (page instanceof NewTemplateWizardPage) {
				((NewTemplateWizardPage) page).collectInput(collectedInput, inputKinds);
			}
			page = page.getNextPage();
		}
	}

	@Override
	public IProject createProject(IProgressMonitor monitor) throws CoreException {
		String projectName = getWizard().getMainPage().getProjectName();
		return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
	}

}
