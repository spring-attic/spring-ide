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

import static org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v1.CFWrapping.unwrapApps;

import java.util.List;
import java.util.Map;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudErrors;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplicationStats;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.ClientRequest;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.RequestErrorHandler;

public class AllApplicationInstancesRequest extends ClientRequest<Map<CFApplication, CFApplicationStats>> {

	private final List<CFApplication> appsToLookUp;

	public AllApplicationInstancesRequest(CloudFoundryOperations client, List<CFApplication> appsToLookUp) {
		super(client, "Getting instances for all applications");
		this.appsToLookUp = appsToLookUp;
	}

	protected Map<CFApplication, CFApplicationStats> doRun(CloudFoundryOperations client) throws Exception {
		return CFWrapping.wrap(client.getApplicationStats(unwrapApps(this.appsToLookUp)));
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
