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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.osgi.util.NLS;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class ProjectsDeployer extends CloudOperation {

	private final static String APP_FOUND_TITLE = "Replace Existing Application";

	private final static String APP_FOUND_MESSAGE = "Replace Existing Application: {0} - already exists. Continue replacing the existing application?";

	private final Map<IProject, BootDashElement> projectsToDeploy;
	private final UserInteractions ui;
	private final boolean shouldAutoReplaceApps;
	private final RunState runOrDebug;

	public ProjectsDeployer(CloudFoundryBootDashModel model, UserInteractions ui,
			Map<IProject, BootDashElement> projectsToDeploy, boolean shouldAutoReplaceApps) {
		super("Deploying projects", model);
		this.projectsToDeploy = projectsToDeploy;
		this.ui = ui;
		this.shouldAutoReplaceApps = shouldAutoReplaceApps;
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
		this.shouldAutoReplaceApps = shouldAutoReplaceApps;
		this.runOrDebug = runOrDebug;
	}

	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		for (Iterator<Entry<IProject, BootDashElement>> it = projectsToDeploy.entrySet().iterator(); it.hasNext();) {
			Entry<IProject, BootDashElement> entry = it.next();

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			List<CloudApplicationOperation> ops = new ArrayList<CloudApplicationOperation>();

			IProject project = entry.getKey();

			FullApplicationDeployment fullDeploymentOp = new FullApplicationDeployment(entry.getKey(), model, ui,
					shouldAutoReplaceApps, runOrDebug);

			String appName = fullDeploymentOp.appName;

			if (model.getAppCache().getApp(project) != null && !shouldAutoReplaceApps
					&& !ui.confirmOperation(APP_FOUND_TITLE, NLS.bind(APP_FOUND_MESSAGE, appName))) {
				continue;
			}

			// Check if another application with the same project mapping already
			// exists.
			// Only ONE application with the same project mapping can exist in the
			// same space
			List<BootDashElement> existingElements = this.model.getElements().getValues();
			if (existingElements != null) {
				for (BootDashElement el : existingElements) {
					if (!appName.equals(el.getName()) && project.equals(el.getProject())) {
						ui.errorPopup("Project Already Linked",
								"Unable to create application [" + appName + "]. Another application ["
										+ el.getName() + "] is already linked to the same project - "
										+ project
												.getName()
								+ " - in the same Cloud target. Please delete the existing one and try deploying again.");
						throw new OperationCanceledException();
					}
				}
			}
			ops.add(fullDeploymentOp);
			if (runOrDebug == RunState.DEBUGGING) {
				ops.add(new RemoteDevClientStartOperation(model, appName, runOrDebug));
			}
			model.getOperationsExecution(ui)
					.runOpAsynch(new ApplicationOperationWithModelUpdate(getName(), model, appName, ops, false));
		}
	}
}