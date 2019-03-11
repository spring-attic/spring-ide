/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.client;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryTargetProperties;

public abstract class CloudFoundryClientFactory {

	public abstract ClientRequests getClient(CFClientParams params);

	/**
	 * Get the client for an existing {@link CloudFoundryRunTarget}. Note that
	 * this may require the password to be set for that runtarget.
	 *
	 * @param runTarget
	 * @return client if connection was successful.
	 * @throws Exception
	 *             if there was an error connecting, including if password is
	 *             not set or invalid.
	 */
	public ClientRequests getClient(CloudFoundryRunTarget runTarget) throws Exception {
		CloudFoundryTargetProperties targetProperties = (CloudFoundryTargetProperties) runTarget.getTargetProperties();
		return getClient(new CFClientParams(targetProperties));
	}

	public final ClientRequests getClient(CloudFoundryTargetProperties targetProperties) throws Exception {
		return getClient(new CFClientParams(targetProperties));
	}

}
