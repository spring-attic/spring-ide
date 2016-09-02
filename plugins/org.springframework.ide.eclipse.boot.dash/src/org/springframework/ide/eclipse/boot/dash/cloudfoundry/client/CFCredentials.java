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

import org.eclipse.core.runtime.Assert;

public class CFCredentials {

	//At least one of password / refreshToken must be set.
	//Either password or refreshToken can be used to authenticate CF connection.

	private final String password;
	private final String refreshToken;

	public static CFCredentials fromPassword(String password) {
		return new CFCredentials(password, null);
	}

	public static CFCredentials fromRefreshToken(String refreshToken) {
		return new CFCredentials(null, refreshToken);
	}

	public String getPassword() {
		return password;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	/////////////////////////////////////////////////////////////////////////

	/**
	 * Private constuctor, use static `fromXXX` factory methods instead.
	 */
	private CFCredentials(String password, String refreshToken) {
		Assert.isLegal(password!=null || refreshToken!=null);
		this.password = password;
		this.refreshToken = refreshToken;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((password == null) ? 0 : password.hashCode());
		result = prime * result + ((refreshToken == null) ? 0 : refreshToken.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CFCredentials other = (CFCredentials) obj;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (refreshToken == null) {
			if (other.refreshToken != null)
				return false;
		} else if (!refreshToken.equals(other.refreshToken))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CFCredentials [password=" + hidePassword(password) + ", refreshToken=" + refreshToken + "]";
	}

	private String hidePassword(String password) {
		return password==null ? null : "*****";
	}
}