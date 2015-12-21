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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryTargetProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.MissingPasswordException;
import org.springframework.ide.eclipse.boot.dash.model.RefreshState;

/**
 * Operation for connecting/disconnecting CF run target
 *
 * @author Alex Boyko
 *
 */
public class ConnectOperation extends CloudOperation {

	private boolean connect;

	public ConnectOperation(CloudFoundryBootDashModel model, boolean connect) {
		super("Connecting run target " + model.getCloudTarget().getName(), model);
		this.connect = connect;
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		if (model.getCloudTarget() != null) {
			if (connect && !model.getCloudTarget().isConnected()) {
				model.setRefreshState(RefreshState.loading("Connecting..."));
				model.getViewModel().updateTargetPropertiesInStore();
				try {
					model.getCloudTarget().getTargetProperties().put(CloudFoundryTargetProperties.DISCONNECTED, null);
					model.getCloudTarget().connect();
					model.setRefreshState(RefreshState.READY);
				} catch (MissingPasswordException e) {
					model.setRefreshState(RefreshState.READY);
					BootDashActivator.log(e);
				} catch (Exception e) {
					model.setRefreshState(RefreshState.error(e));
					throw e;
				}
			} else if (!connect && model.getCloudTarget().isConnected()) {
				model.getCloudTarget().getTargetProperties().put(CloudFoundryTargetProperties.DISCONNECTED, "true"); //$NON-NLS-1$
				model.getViewModel().updateTargetPropertiesInStore();
				model.setRefreshState(RefreshState.loading("Disconnecting..."));
				model.getCloudTarget().disconnect();
				model.setRefreshState(RefreshState.READY);
			}
		}
	}

	public ISchedulingRule getSchedulingRule() {
		return new RefreshSchedulingRule(model.getRunTarget());
	}

}
