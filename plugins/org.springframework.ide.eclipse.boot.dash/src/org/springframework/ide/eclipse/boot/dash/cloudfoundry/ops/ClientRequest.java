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
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudErrors;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;

public abstract class ClientRequest<T> {

	protected final CloudFoundryBootDashModel model;

	public ClientRequest(CloudFoundryBootDashModel model) {
		this.model = model;
	}

	public T run() throws Exception {

		try {
			return fetchClientAndRun();
		} catch (Exception e) {
			// if 403, could be access token error. Refresh client and Try again
			// once
			if (CloudErrors.isAccessException(e)) {
				model.getCloudTarget().refresh();
				return fetchClientAndRun();
			} else {
				throw e;
			}
		}
	}

	private T fetchClientAndRun() throws Exception {
		CloudFoundryOperations client = model.getCloudTarget().getClient();
		return doRun(client);
	}

	protected abstract T doRun(CloudFoundryOperations client) throws Exception;
}
