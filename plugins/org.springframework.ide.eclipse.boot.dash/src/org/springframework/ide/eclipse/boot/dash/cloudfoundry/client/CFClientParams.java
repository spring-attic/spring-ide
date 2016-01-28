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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.client;

import org.cloudfoundry.client.lib.HttpProxyConfiguration;
import org.eclipse.core.runtime.Assert;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryTargetProperties;

/**
 * All the parameters needed to create a CF client.
 *
 * @author Kris De Volder
 */
public class CFClientParams {

	private final String apiUrl;
	private final String username;
	private final String password;
	private final boolean isSelfSigned;

	private String orgName; // optional
	private String spaceName; //optional

	public CFClientParams(String apiUrl,
			String username, String password,
			boolean isSelfSigned,
			String orgName,
			String spaceName
	) {
		Assert.isNotNull(apiUrl, "apiUrl is required");
		Assert.isNotNull(username, "username is required");
		Assert.isNotNull(password, "password is required");
		this.apiUrl = apiUrl;
		this.username = username;
		this.password = password;
		this.isSelfSigned = isSelfSigned;
		this.orgName = orgName;
		this.spaceName = spaceName;
	}

	public CFClientParams(CloudFoundryTargetProperties targetProperties) throws Exception {
		this(
			targetProperties.getUrl(),
			targetProperties.getUsername(),
			targetProperties.getPassword(),
			targetProperties.isSelfsigned(),
			targetProperties.getOrganizationName(),
			targetProperties.getSpaceName()
		);
	}

	public String getPassword() {
		return password;
	}

	public String getUsername() {
		return username;
	}

	public boolean isSelfsigned() {
		return isSelfSigned;
	}

	public HttpProxyConfiguration getProxyConf() {
		//TODO: there's no support for this, but probably there should be.
		return null;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getSpaceName() {
		return spaceName;
	}

	public void setSpaceName(String spaceName) {
		this.spaceName = spaceName;
	}

}
