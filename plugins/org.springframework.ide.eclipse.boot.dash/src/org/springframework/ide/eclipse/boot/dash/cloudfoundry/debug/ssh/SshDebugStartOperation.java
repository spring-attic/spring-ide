/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.debug.ssh;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.CloudApplicationOperation;

/**
 * @author Kris De Volder
 */
public class SshDebugStartOperation extends CloudApplicationOperation {

	private CloudDashElement app;

	public SshDebugStartOperation(CloudDashElement app, SshDebugSupport debugSupport) {
		super("Starting SSH debugging for app '"+app.getName()+"'", app.getCloudModel(), app.getName());
		this.app = app;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		SshDebugLaunchConfigurationDelegate.doLaunch(app, monitor);
	}
}
