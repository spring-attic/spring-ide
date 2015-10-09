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
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudErrors;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;

public abstract class CloudOperation extends Operation<Void> {

	protected final CloudFoundryBootDashModel model;
	protected final ClientRequests requests;

	public CloudOperation(String opName, CloudFoundryBootDashModel model) {
		super(opName);
		this.model = model;
		this.requests = new ClientRequests(model);
	}

	abstract protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException;

	@Override
	protected Void runOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {

		if (monitor.isCanceled()) {
			throw new OperationCanceledException();
		}
		try {
			doCloudOp(monitor);
		} catch (Exception e) {
			CloudErrors.checkAndRethrowCloudException(e, getOpErrorPrefix());
		}
		return null;
	}

	String getOpErrorPrefix() {
		return "Error in target: " + model.getRunTarget().getName();
	}
}
