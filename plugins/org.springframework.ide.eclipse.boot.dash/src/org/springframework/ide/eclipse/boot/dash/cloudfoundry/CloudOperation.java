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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.springframework.ide.eclipse.boot.dash.model.Operation;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public abstract class CloudOperation<T> extends Operation<T> {

	protected final UserInteractions ui;
	protected final CloudFoundryOperations client;

	public CloudOperation(String opName, CloudFoundryOperations client, UserInteractions ui) {
		super(opName);
		this.ui = ui;
		this.client = client;
	}

	abstract protected T doCloudOp(CloudFoundryOperations client, IProgressMonitor monitor) throws Exception;

	@Override
	protected T runOp(IProgressMonitor monitor) throws Exception {

		try {
			return doCloudOp(client, monitor);
		} catch (Exception e) {
			final String message = e.getMessage();
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					if (ui != null) {
						ui.errorPopup("Error performing Cloud operation: ", message);
					}
				}
			});
		}
		return null;
	}
}
