/*******************************************************************************
 * Copyright (c) 2005, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.internal.uaa.filetransfer;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransfer;
import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransferFactory;
import org.eclipse.ecf.provider.filetransfer.httpclient.HttpClientRetrieveFileTransfer;
import org.springframework.ide.eclipse.uaa.IUaa;
import org.springframework.ide.eclipse.uaa.UaaPlugin;
import org.springframework.uaa.client.UrlHelper;

/**
 * ECF {@link IRetrieveFileTransferFactory} implementation that adds a "User-Agent" header when requesting resources
 * <code>springide.org</code>, <code>springsource.org</code> and <code>springsource.com</code>.
 * @author Christian Dupuis
 * @since 2.5.2
 */
public class UserAgentAwareHttpClientRetrieveFileTransfer implements IRetrieveFileTransferFactory {

	public static final String SETUP_UAA_REQUIRED = "At this time you have not authorized %s to download resources from any VMware domain including '%s'. Some %s features are therefore unavailable. Please "
			+ "consult the Spring UAA preferences at 'Preferences -> Spring -> User Agent Analysis' for further information.";

	public static final String PLATFORM_NAME = Platform.getBundle("com.springsource.sts") != null ? "SpringSource Tool Suite"
			: "Spring IDE";

	/**
	 * {@inheritDoc}
	 */
	public IRetrieveFileTransfer newInstance() {
		return new HttpClientRetrieveFileTransfer(
				new UserAgentAwareHttpClient(new MultiThreadedHttpConnectionManager()));
	}

	private static class UserAgentAwareHttpClient extends HttpClient {

		public UserAgentAwareHttpClient(MultiThreadedHttpConnectionManager multiThreadedHttpConnectionManager) {
			super(multiThreadedHttpConnectionManager);
		}

		@Override
		public int executeMethod(HostConfiguration originalHostConfig, HttpMethod method, HttpState state)
				throws IOException, HttpException {

			HostConfiguration hostconfig = originalHostConfig;
			HostConfiguration defaulthostconfig = getHostConfiguration();
			if (hostconfig == null) {
				hostconfig = defaulthostconfig;
			}
			URI uri = method.getURI();
			if (hostconfig == defaulthostconfig || uri.isAbsoluteURI()) {
				// Make a deep copy of the host defaults
				hostconfig = (HostConfiguration) hostconfig.clone();
				if (uri.isAbsoluteURI()) {
					hostconfig.setHost(uri);
				}
			}

			URL url = new URL(hostconfig.getHostURL());
			IUaa uaa = UaaPlugin.getUAA();

			// Check if we are allowed to open the VMware domain by privacy level of UAA
			if (uaa.canOpenUrl(url)) {

				boolean useUaaHeader = false;
				// Add UAA header to VMware controlled domains only
				if (UrlHelper.isUaaEnabledHost(url)) {
					getParams().setParameter(HttpClientParams.USER_AGENT, uaa.getUserAgentHeader());
					useUaaHeader = true;
				}

				int responseCode = super.executeMethod(originalHostConfig, method, state);

				// Clear UAA internal caches to prevent growing
				if (useUaaHeader && responseCode == 200) {
					uaa.clear();
				}

				return responseCode;
			}

			// If we get to here throw exception
			throw new IOException(String.format(SETUP_UAA_REQUIRED, PLATFORM_NAME, url.getHost(), PLATFORM_NAME));
		}
	}

}
