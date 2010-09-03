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

import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransfer;
import org.eclipse.ecf.filetransfer.service.IRetrieveFileTransferFactory;
import org.eclipse.ecf.provider.filetransfer.httpclient.HttpClientRetrieveFileTransfer;
import org.springframework.ide.eclipse.internal.uaa.UaaPlugin;

/**
 * ECF {@link IRetrieveFileTransferFactory} implementation that adds a "User-Agent" header when requesting resources
 * <code>springide.org</code>, <code>springsource.org</code> and <code>springsource.com</code>.
 * @author Christian Dupuis
 * @since 2.5.0
 */
public class UserAgentAwareHttpClientRetrieveFileTransfer implements IRetrieveFileTransferFactory {

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
			// Check the url to see if we want to communicate home
			HostConfiguration hostconfig = originalHostConfig;
			HostConfiguration defaulthostconfig = getHostConfiguration();
			if (hostconfig == null) {
				hostconfig = defaulthostconfig;
			}
			URI uri = method.getURI();
			if (hostconfig == defaulthostconfig || uri.isAbsoluteURI()) {
				// make a deep copy of the host defaults
				hostconfig = (HostConfiguration) hostconfig.clone();
				if (uri.isAbsoluteURI()) {
					hostconfig.setHost(uri);
				}
			}

			String url = hostconfig.getHostURL();
			if (url.endsWith("springsource.org") || url.endsWith("springsource.com") || url.endsWith("springide.org")) {
				getParams().setParameter(HttpClientParams.USER_AGENT, UaaPlugin.getDefault().getUserAgentHeader());
			}

			return super.executeMethod(originalHostConfig, method, state);
		}
	}

}
