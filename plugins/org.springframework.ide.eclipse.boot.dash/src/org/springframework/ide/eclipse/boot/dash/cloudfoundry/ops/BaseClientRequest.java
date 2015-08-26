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

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.console.LogType;

public abstract class BaseClientRequest extends ClientRequest<Void> {

	protected final String appName;
	protected final String requestName;

	public BaseClientRequest(CloudFoundryBootDashModel model, String appName, String requestName) {
		super(model);
		this.appName = appName;
		this.requestName = requestName;
		addRequestListener(new AppRequestListener());
	}

	@Override
	protected Void doRun(CloudFoundryOperations client) throws Exception {
		runRequest(client);
		return null;
	}

	protected abstract void runRequest(CloudFoundryOperations client) throws Exception;

	class AppRequestListener extends RequestListener {

		@Override
		protected void onAccessToken(Throwable t) {
			BootDashActivator.log(t);
			write("Access token refresh - Error refreshing access token " + t.getMessage(), LogType.LOCALSTDERROR);

			write("Access token refresh - Attempting Cloud Foundry request again: " + requestName,
					LogType.LOCALSTDOUT);
		}

		@Override
		protected void onLoginAttempt() {
			write("Access token refresh - Requesting new access token", LogType.LOCALSTDOUT);
		}

		@Override
		protected void onLoginSucceeded() {
			write("Access token refresh - Access token refresh succeeeded", LogType.LOCALSTDOUT);
		}

		protected void write(String message, LogType type) {
			try {
				model.getElementConsoleManager().writeToConsole(appName, message, LogType.LOCALSTDERROR);
			} catch (Exception e) {
				BootDashActivator.log(e);
			}
		}

	}

}
