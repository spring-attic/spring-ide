/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class ProjectsDeployer extends CloudOperation {

	private final Map<IProject, BootDashElement> projectsToDeploy;
	private final UserInteractions ui;
	private final RunState runOrDebug;

	public ProjectsDeployer(CloudFoundryBootDashModel model, UserInteractions ui,
			Map<IProject, BootDashElement> projectsToDeploy) {
		super("Deploying projects", model);
		this.projectsToDeploy = projectsToDeploy;
		this.ui = ui;
		this.runOrDebug = RunState.RUNNING;
	}

	public ProjectsDeployer(CloudFoundryBootDashModel model, UserInteractions ui,
			List<BootDashElement> elementsToRedeploy, boolean shouldAutoReplaceApps, RunState runOrDebug) {
		super("Deploying projects", model);
		this.projectsToDeploy = new LinkedHashMap<IProject, BootDashElement>();

		for (BootDashElement element : elementsToRedeploy) {
			this.projectsToDeploy.put(element.getProject(), element);
		}

		this.ui = ui;
		this.runOrDebug = runOrDebug;
	}

	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {

		for (Iterator<Entry<IProject, BootDashElement>> it = projectsToDeploy.entrySet().iterator(); it.hasNext();) {
			Entry<IProject, BootDashElement> entry = it.next();

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			CloudApplicationOperation op = new DeploymentOperationFactory(model, entry.getKey(), ui)
					.getCreateAndDeploy(runOrDebug, monitor);

			op.run(monitor);
		}
	}
}