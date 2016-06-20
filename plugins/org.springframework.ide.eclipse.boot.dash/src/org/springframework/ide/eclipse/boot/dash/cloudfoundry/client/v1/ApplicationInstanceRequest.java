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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v1;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.ApplicationStats;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudErrors;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.RequestErrorHandler;

public class ApplicationInstanceRequest extends ClientRequest<ApplicationStats> {

	public ApplicationInstanceRequest(CloudFoundryOperations client, String appName) {
		super(client, appName, "Getting application instances for " + appName, new ApplicationInstanceRequestErrorHandler());
	}

	@Override
	protected ApplicationStats doRun(CloudFoundryOperations client) throws Exception {
		return client.getApplicationStats(getAppName());
	}

	static class ApplicationInstanceRequestErrorHandler extends RequestErrorHandler {
		@Override
		public boolean throwError(Throwable e) {
			// App doesn't exist anymore or instances are not available yet.
			if (e instanceof Exception) {
				Exception exception = (Exception) e;
				return !CloudErrors.isNotFoundException(exception) && !CloudErrors.is503Error(exception);
			}
			return true;
		}
	}
}
