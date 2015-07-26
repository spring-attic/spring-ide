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
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.swt.widgets.Display;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.Operation;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public abstract class CloudOperation<T> extends Operation<T> {

	protected final UserInteractions ui;
	protected final CloudFoundryOperations client;
	protected final CloudFoundryBootDashModel model;

	public CloudOperation(String opName, CloudFoundryOperations client, CloudFoundryBootDashModel model,
			UserInteractions ui) {
		super(opName);
		this.ui = ui;
		this.client = client;
		this.model = model;
	}

	abstract protected T doCloudOp(CloudFoundryOperations client, IProgressMonitor monitor)
			throws Exception, OperationCanceledException;

	@Override
	protected T runOp(IProgressMonitor monitor) throws Exception {

		try {
			return doCloudOp(client, monitor);
		} catch (Exception e) {
			handleError(e);
		}
		return null;
	}

	protected void handleError(Exception e) throws Exception {

		final Exception[] error = { e };
		if (!(e instanceof OperationCanceledException)) {
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {

					String message = error[0].getMessage();
					if (error[0] instanceof CloudFoundryException) {
						message = ((CloudFoundryException) error[0]).getDescription();
					}

					if (message == null || message.trim().length() == 0) {
						message = "Cloud operation failure of type: " + error[0].getClass().getName();
					}

					if (ui != null) {
						ui.errorPopup("Error performing Cloud operation: ", message);
					}
				}
			});
		}
		throw e;
	}

}
