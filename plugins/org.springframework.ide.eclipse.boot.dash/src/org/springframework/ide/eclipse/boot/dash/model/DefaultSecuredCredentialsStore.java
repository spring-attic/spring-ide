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
package org.springframework.ide.eclipse.boot.dash.model;

import org.eclipse.equinox.security.storage.EncodingUtils;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;

/**
 * Secured storage for {@link RunTarget} passwords.
 *
 */
public class DefaultSecuredCredentialsStore implements SecuredCredentialsStore {

	private static final String KEY_PASSWORD = "password";

	public DefaultSecuredCredentialsStore() {
	}

	public void remove(String runTargetId) {
		ISecurePreferences preferences = getSecurePreferences(runTargetId);
		if (preferences != null) {
			preferences.removeNode();
		}
	}

	public String getPassword(String runTargetId) {
		return readProperty(KEY_PASSWORD, runTargetId);
	}

	public void setPassword(String password, String runTargetId) {
		setProperty(KEY_PASSWORD, password, runTargetId);
	}

	private ISecurePreferences getSecurePreferences(String runTargetId) {
		ISecurePreferences securePreferences = SecurePreferencesFactory.getDefault().node(BootDashActivator.PLUGIN_ID);
		securePreferences = securePreferences.node(EncodingUtils.encodeSlashes(runTargetId));
		return securePreferences;
	}

	private String readProperty(String property, String runTargetId) {
		ISecurePreferences preferences = getSecurePreferences(runTargetId);
		String val = null;
		if (preferences != null) {
			try {
				val = preferences.get(property, null);
			} catch (StorageException e) {
				BootDashActivator.log(e);
			}
		}
		return val;
	}

	private void setProperty(String property, String value, String runTargetId) {
		ISecurePreferences preferences = getSecurePreferences(runTargetId);
		if (preferences != null) {
			try {
				preferences.put(property, value, true);
			} catch (StorageException e) {
				BootDashActivator.log(e);
			}
		}
	}

}
