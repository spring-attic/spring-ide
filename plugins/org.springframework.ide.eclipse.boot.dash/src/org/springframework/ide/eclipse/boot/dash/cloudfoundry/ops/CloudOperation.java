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

import org.cloudfoundry.client.lib.CloudFoundryException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.Operation;

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
			checkError(e);
		}
		return null;
	}

	protected void checkError(Exception e) throws Exception {

		// Special case for CF exceptions:
		// CF exceptions may not contain the error in the message but rather
		// the description
		if (e instanceof CloudFoundryException) {
			String message = ((CloudFoundryException) e).getDescription();

			if (message == null || message.trim().length() == 0) {
				message = "Cloud operation failure of type: " + e.getClass().getName();
			}
			IStatus status = BootDashActivator.createErrorStatus(e, message);
			throw new CoreException(status);
		}

		throw e;
	}
}
