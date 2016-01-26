/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.client;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudErrors;

public abstract class ApplicationRequest<T> extends ClientRequest<T> {

	public ApplicationRequest(CloudFoundryOperations client, String appName) {
		super(client, appName, "Getting application " + appName, new ApplicationRequestErrorHandler());
	}

	static class ApplicationRequestErrorHandler extends RequestErrorHandler {
		@Override
		public boolean throwError(Throwable e) {
			// App doesn't exist anymore.
			return e instanceof Exception && !CloudErrors.isNotFoundException((Exception) e);
		}
	}

}
