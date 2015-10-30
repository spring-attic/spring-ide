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

import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudErrors;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.console.LogType;

/**
 * Handles various error conditions when a Client request is performed.
 *
 */
public class RequestErrorListener {

	private final ClientRequest<?> clientRequest;

	public RequestErrorListener(ClientRequest<?> clientRequest) {
		this.clientRequest = clientRequest;
	}

	protected boolean retryOnError(Exception e) throws Exception {
		// If access token error, create a new client session and try again
		// Note: access token errors may not be Exception, thus the reason
		// to handle Throwable instead
		if (CloudErrors.isAccessTokenError(e)) {
			BootDashActivator.log(e);
			write("Reconnecting to the Cloud Foundry target and retrying request: " + clientRequest.requestName
					+ ". Reason for re-attempt: " + e.getMessage(), LogType.LOCALSTDERROR);
			this.clientRequest.model.getCloudTarget().refresh();
			write("Connection to the Cloud Foundry target successful - " + clientRequest.model.getRunTarget().getName(),
					LogType.LOCALSTDERROR);
			return true;
		}
		return false;
	}

	/**
	 *
	 * @param rce
	 * @return true if this is an actual error. If true error will continue to
	 *         propagate through the framework. False means it is an error to be
	 *         ignored.
	 */
	protected boolean shouldIgnoreError(Exception exception) {
		return false;
	}

	protected void write(String message, LogType type) {
		try {
			if (this.clientRequest.getAppName() != null) {
				this.clientRequest.model.getElementConsoleManager().writeToConsole(this.clientRequest.getAppName(),
						message, type);
			}

		} catch (Exception e) {
			BootDashActivator.log(e);
		}
	}

}