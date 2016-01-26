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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.client;

import java.util.List;
import java.util.Map;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.ApplicationStats;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudErrors;

public class AllApplicationInstancesRequest extends ClientRequest<Map<CloudApplication, ApplicationStats>> {

	private final List<CloudApplication> appsToLookUp;

	public AllApplicationInstancesRequest(CloudFoundryOperations client, List<CloudApplication> appsToLookUp) {
		super(client, "Getting instances for all applications");
		this.appsToLookUp = appsToLookUp;
	}

	protected Map<CloudApplication, ApplicationStats> doRun(CloudFoundryOperations client) throws Exception {
		return client.getApplicationStats(this.appsToLookUp);
	}

	static class ApplicationInstanceRequestErrorHandler extends RequestErrorHandler {
		@Override
		public boolean throwError(Throwable e) {
			if (e instanceof Exception) {
				Exception exception = (Exception) e;
				return CloudErrors.is503Error(exception);
			}
			return false;
		}
	}
}
