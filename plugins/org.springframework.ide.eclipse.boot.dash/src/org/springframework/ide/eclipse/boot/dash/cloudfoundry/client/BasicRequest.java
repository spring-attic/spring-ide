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

public abstract class BasicRequest extends ClientRequest<Void> {

	public BasicRequest(CloudFoundryOperations client, String appName, String requestName) {
		super(client, appName, requestName, null);
	}

	@Override
	protected Void doRun(CloudFoundryOperations client) throws Exception {
		runRequest(client);
		return null;
	}

	protected abstract void runRequest(CloudFoundryOperations client) throws Exception;

}
