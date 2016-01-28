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

import java.net.URL;
import java.util.Properties;

import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.MissingPasswordException;

public class DefaultCloudFoundryClientFactory extends CloudFoundryClientFactory {

	/*
	 * System property. Set to "true" if connection pool is to be used. "false"
	 * otherwise or omit as a system property
	 */
	public static final String BOOT_DASH_CONNECTION_POOL = "sts.boot.dash.connection.pool";

	public CloudFoundryOperations getClient(CloudCredentials credentials, URL apiUrl, String orgName, String spaceName,
			boolean isSelfsigned) throws Exception {
		checkPassword(credentials.getPassword(), credentials.getEmail());

		Properties properties = System.getProperties();
		// By default disable connection pool (i.e. flag is set to true) unless
		// a property exists that sets
		// USING connection pool to "true" (so, i.e., disable connection pool is
		// false)
		boolean disableConnectionPool = properties == null || !properties.containsKey(BOOT_DASH_CONNECTION_POOL)
				|| !"true".equals(properties.getProperty(BOOT_DASH_CONNECTION_POOL));

		return spaceName != null
				? new CloudFoundryClient(credentials, apiUrl, orgName, spaceName, isSelfsigned, disableConnectionPool)
				: new CloudFoundryClient(credentials, apiUrl, isSelfsigned, disableConnectionPool);

	}

	private static void checkPassword(String password, String id) throws MissingPasswordException {
		if (password == null) {
			throw new MissingPasswordException("No password stored or set for: " + id
					+ ". Please ensure that the password is set in the run target and it is up-to-date.");
		}
	}

}
