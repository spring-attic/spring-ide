/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CloudFoundryClientFactory;

public class DefaultCloudFoundryClientFactoryV2 extends CloudFoundryClientFactory {

	public static final DefaultCloudFoundryClientFactoryV2 INSTANCE = new DefaultCloudFoundryClientFactoryV2();

	/**
	 * Use 'INSTANCE' constant instead. This class is a singleton.
	 */
	private DefaultCloudFoundryClientFactoryV2() {}

	private CloudFoundryClientCache clientFactory = new CloudFoundryClientCache();

	@Override
	public ClientRequests getClient(CFClientParams params) {
		return new DefaultClientRequestsV2(clientFactory, params);
	}
}
