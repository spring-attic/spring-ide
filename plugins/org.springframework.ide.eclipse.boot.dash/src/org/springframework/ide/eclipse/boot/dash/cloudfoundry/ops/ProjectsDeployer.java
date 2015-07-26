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

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.Operation;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class ProjectsDeployer extends Operation<Void> {

	private final CloudFoundryBootDashModel model;
	private final Map<IProject, BootDashElement> projectsToDeploy;
	private final CloudFoundryOperations client;
	private final UserInteractions ui;
	private final boolean shouldAutoReplaceApps;

	public ProjectsDeployer(CloudFoundryBootDashModel cloudFoundryBootDashModel, CloudFoundryOperations client,
			UserInteractions ui, Map<IProject, BootDashElement> projectsToDeploy, boolean shouldAutoReplaceApps) {
		super("Deploying projects");
		this.model = cloudFoundryBootDashModel;
		this.projectsToDeploy = projectsToDeploy;
		this.client = client;
		this.ui = ui;
		this.shouldAutoReplaceApps = shouldAutoReplaceApps;
	}

	public ProjectsDeployer(CloudFoundryBootDashModel cloudFoundryBootDashModel, CloudFoundryOperations client,
			UserInteractions ui, List<BootDashElement> elementsToRedeploy, boolean shouldAutoReplaceApps) {
		super("Deploying projects");
		this.projectsToDeploy = new LinkedHashMap<IProject, BootDashElement>();

		for (BootDashElement element : elementsToRedeploy) {
			this.projectsToDeploy.put(element.getProject(), element);
		}

		this.model = cloudFoundryBootDashModel;
		this.client = client;
		this.ui = ui;
		this.shouldAutoReplaceApps = shouldAutoReplaceApps;
	}

	public Void runOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {

		SubMonitor subMonitor = SubMonitor.convert(monitor, projectsToDeploy.size() * 100);
		for (Iterator<Entry<IProject, BootDashElement>> it = projectsToDeploy.entrySet().iterator(); it.hasNext();) {
			Entry<IProject, BootDashElement> entry = it.next();

			CloudApplicationOperation compositeOp = new ApplicationDeploymentOperation(client, entry.getKey(),
					entry.getValue(), model, ui, shouldAutoReplaceApps);

			compositeOp.run(subMonitor.newChild(100));

		}
		return null;
	}

}