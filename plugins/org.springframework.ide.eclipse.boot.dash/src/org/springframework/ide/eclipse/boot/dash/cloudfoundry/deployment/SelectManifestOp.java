/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.CloudOperation;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class SelectManifestOp extends CloudOperation {

	protected final CloudDashElement cde;
	protected final UserInteractions ui;

	public SelectManifestOp(CloudDashElement cde, UserInteractions ui) {
		super("Select a manifest file", cde.getCloudModel());
		this.cde = cde;
		this.ui = ui;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {

		IProject project = cde.getProject();

		if (cde == null || project == null) {
			return;
		}
		cde.setDeploymentManifestFile(ui.selectDeploymentManifestFile(project, cde.getDeploymentManifestFile()));
	}

}
