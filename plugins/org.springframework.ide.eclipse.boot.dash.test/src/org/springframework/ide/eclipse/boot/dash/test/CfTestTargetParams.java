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
package org.springframework.ide.eclipse.boot.dash.test;

import org.eclipse.core.runtime.Assert;
import org.springframework.ide.eclipse.boot.util.StringUtil;

/**
 * @author Kris De Volder
 */
public class CfTestTargetParams {

	private String apiUrl;
	private String user;
	private String password;
	private String org;
	private String space;
	private boolean isSelfsigned = false;

	public CfTestTargetParams() {
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getOrg() {
		return org;
	}

	public void setOrg(String org) {
		this.org = org;
	}

	public String getSpace() {
		return space;
	}

	public void setSpace(String space) {
		this.space = space;
	}
	
	public boolean isSelfsigned() {
		return this.isSelfsigned;
	}

	public void setSelfsigned(boolean isSelfsigned) {
		this.isSelfsigned = isSelfsigned;
	}

	public static CfTestTargetParams fromEnv() {
		CfTestTargetParams params = new CfTestTargetParams();
		params.setApiUrl(fromEnv("CF_TEST_API_URL"));
		params.setUser(fromEnv("CF_TEST_USER"));
		params.setPassword(fromEnv("CF_TEST_PASSWORD"));
		params.setOrg(fromEnv("CF_TEST_ORG"));
		params.setSpace(fromEnv("CF_TEST_SPACE"));
		return params;
	}

	private static String fromEnv(String name) {
		String value = System.getenv(name);
		Assert.isLegal(StringUtil.hasText(value), "The environment varable '"+name+"' must be set");
		return value;
	}

}
