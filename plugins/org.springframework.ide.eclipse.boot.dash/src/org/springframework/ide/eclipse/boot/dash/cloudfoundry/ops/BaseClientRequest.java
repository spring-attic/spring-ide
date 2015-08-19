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
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;

public abstract class BaseClientRequest extends ClientRequest<Void> {

	public BaseClientRequest(CloudFoundryBootDashModel model) {
		super(model);
	}

	@Override
	protected Void doRun(CloudFoundryOperations client) throws Exception {
		runRequest(client);
		return null;
	}

	protected abstract void runRequest(CloudFoundryOperations client) throws Exception;

}
