/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
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
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.debug.DebugSupport;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class ProjectsDeployer extends CloudOperation {

	private final Set<IProject> projectsToDeploy;
	private final UserInteractions ui;
	private final RunState runOrDebug;
	private final DebugSupport debugSupport;

	public ProjectsDeployer(CloudFoundryBootDashModel model,
			UserInteractions ui,
			Set<IProject> projectsToDeploy,
			RunState runOrDebug,
			DebugSupport debugSupport) {
		super("Deploying projects", model);
		this.projectsToDeploy = projectsToDeploy;
		this.ui = ui;
		this.runOrDebug = runOrDebug;
		this.debugSupport = debugSupport;
	}

	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {

		for (Iterator<IProject> it = projectsToDeploy.iterator(); it.hasNext();) {
			IProject project = it.next();

			if (monitor.isCanceled()) {
				throw new OperationCanceledException();
			}

			try {
				CloudApplicationDeploymentProperties properties = model.createDeploymentProperties(project, ui, monitor);

				CloudApplicationOperation op = model.getApplicationDeploymentOperations().createRestartPush(project, properties, debugSupport, runOrDebug, ui, monitor);
				model.getOperationsExecution(ui).runOpAsynch(op);
			} catch (Exception e) {
				if (!(e instanceof OperationCanceledException)) {
					BootDashActivator.log(e);
					if (ui != null) {
						String message = e.getMessage() != null && e.getMessage().trim().length() > 0 ? e.getMessage()
								: "Error type: " + e.getClass().getName()
										+ ". Check Error Log view for further details.";
						ui.errorPopup("Operation Failure", message);
					}
				}
			}
		}
	}
}