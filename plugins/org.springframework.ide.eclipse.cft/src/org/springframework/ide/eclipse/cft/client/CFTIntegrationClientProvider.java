/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.cft.client;

import org.cloudfoundry.client.CloudFoundryClient;
import org.eclipse.cft.server.core.internal.CloudErrorUtil;
import org.eclipse.cft.server.core.internal.CloudFoundryServer;
import org.eclipse.cft.server.core.internal.CloudServerUtil;
import org.eclipse.cft.server.core.internal.ProviderPriority;
import org.eclipse.cft.server.core.internal.client.CFClient;
import org.eclipse.cft.server.core.internal.client.CFClientProvider;
import org.eclipse.cft.server.core.internal.client.CFCloudCredentials;
import org.eclipse.cft.server.core.internal.client.CloudInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.osgi.framework.Version;

public class CFTIntegrationClientProvider implements CFClientProvider {

	@Override
	public ProviderPriority getPriority() {
		return ProviderPriority.HIGH;
	}

	@Override
	public boolean supports(String serverUrl, CloudInfo info) {
		return info != null && info.getDopplerUrl() != null && supportsVersion(info.getCCApiVersion());
	}

	@Override
	public CFClient getClient(IServer cloudServer, CFCloudCredentials credentials, String orgName, String spaceName,
			IProgressMonitor monitor) throws CoreException {
		// Passcode not supported yet
		if (credentials.isPasscodeSet()) {
			throw CloudErrorUtil.toCoreException(
					"One-time passcode not supported in this version of v2 client for doppler log streaming.");
		}
		CloudFoundryServer cfServer = CloudServerUtil.getCloudServer(cloudServer);
		
		return new CFTV2Client(cfServer, credentials, orgName, spaceName);
	}

	protected boolean supportsVersion(Version ccApiVersion) {
		if (ccApiVersion == null) {
			return false;
		}
		Version supported = getSupportedV2ClientApiVersion();
		return ccApiVersion.compareTo(supported) > 0;
	}

	public Version getSupportedV2ClientApiVersion() {
		return new Version(CloudFoundryClient.SUPPORTED_API_VERSION);
	}

}
