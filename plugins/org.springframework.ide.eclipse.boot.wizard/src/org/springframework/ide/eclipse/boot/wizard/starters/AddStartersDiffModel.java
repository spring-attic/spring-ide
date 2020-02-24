/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.starters;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrProjectDownloader;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.wizard.HierarchicalMultiSelectionFieldModel;
import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

public class AddStartersDiffModel implements Disposable {

	private final ISpringBootProject bootProject;
	private final HierarchicalMultiSelectionFieldModel<Dependency> dependencies;
	private final InitializrProjectDownloader projectDownloader;


	public AddStartersDiffModel(ISpringBootProject bootProject,
			HierarchicalMultiSelectionFieldModel<Dependency> dependencies,
			InitializrProjectDownloader projectDownloader) {
		this.bootProject = bootProject;
		this.dependencies = dependencies;
		this.projectDownloader = projectDownloader;
	}

	public AddStartersCompareInput getCompareInput() throws Exception {
		IProject project = bootProject.getProject();

		// TODO: this is just downloading the project. we need to modify the compare input
		// so that it supports the project as a local resource, rather than just the pom.xml
		downloadProject();

		boolean editable = true;
		LocalResource localResource = new LocalResource(project, getSelectedResource(), editable);

		GeneratedResource generatedResource = new GeneratedResource(getSelectedResource(), localResource.getImage(),
				bootProject.generatePom(dependencies.getCurrentSelection()));

		return new AddStartersCompareInput(localResource, generatedResource);
	}

	private String getSelectedResource() {
		return "pom.xml";
	}

	@Override
	public void dispose() {
		this.projectDownloader.dispose();
	}

	public void downloadProject() {
		Job job = new Job("Downloading project from Initializr") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					File generatedProject = projectDownloader.getProject(dependencies.getCurrentSelection(), bootProject);
					return Status.OK_STATUS;
				} catch (Exception e) {
					Log.log(e);
					return ExceptionUtil.status(e);
				}
			}
		};
		job.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		job.schedule();
	}

}
