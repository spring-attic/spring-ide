/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model.runtargettypes;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.equinox.security.storage.StorageException;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;

/**
 * Properties for a particular {@link RunTarget}.
 * <p/>
 * Properties define two things:
 * <p/>
 * 1. Copy of all properties
 * <p/>
 * 2. Properties that should be persisted
 * <p/>
 * Target properties should be associated with both a {@link RunTargetType} and
 * a {@link RunTarget} id.
 *
 */
public class TargetProperties {

	public static final String RUN_TARGET_ID = "runTargetID";
	public static final String USERNAME_PROP = "username";
	public static final String URL_PROP = "url";

	private static final String STORE_PASSWORD = "storePassword";

	protected Map<String, String> map;
	private RunTargetType type;
	private BootDashModelContext context;
	private String password;

	public TargetProperties(Map<String, String> map, RunTargetType type, BootDashModelContext context) {
		this.map = map;
		this.type = type;
		this.context = context;
	}

	public TargetProperties(BootDashModelContext context) {
		this(new HashMap<String, String>(), null, context);
	}

	public TargetProperties(TargetProperties targetProperties, RunTargetType runTargetType) {
		this(targetProperties.getAllProperties(), runTargetType, targetProperties.context);
	}

	public TargetProperties(RunTargetType type, BootDashModelContext context) {
		this(new HashMap<String, String>(), type, context);
	}

	public TargetProperties(RunTargetType type, String runTargetId, BootDashModelContext context) {
		this(new HashMap<String, String>(), type, context);
		put(RUN_TARGET_ID, runTargetId);
	}

	public TargetProperties(Map<String, String> map, RunTargetType type, String runTargetId, BootDashModelContext context) {
		this(type, context);
		if (map != null) {
			this.map = map;
		}
		put(RUN_TARGET_ID, runTargetId);
	}

	public String get(String property) {
		return map.get(property);
	}

	/**
	 *
	 * @return properties that should be persisted. May be a subset of all the
	 *         properties from {@link #getAllProperties()}
	 */
	public Map<String, String> getPropertiesToPersist() {
		return getAllProperties();
	}

	/**
	 * @return non-null map of properties. This is a copy of the actual map
	 */
	public Map<String, String> getAllProperties() {
		return new HashMap<>(map);
	}

	public String getRunTargetId() {
		return map.get(RUN_TARGET_ID);
	}

	public RunTargetType getRunTargetType() {
		return type;
	}

	public String getUsername() {
		return map.get(USERNAME_PROP);
	}

	public boolean isStorePassword() {
		String s = map.get(STORE_PASSWORD);
		return s == null ? false : Boolean.valueOf(s);
	}

	public void setStorePassword(boolean store) {
		map.put(STORE_PASSWORD, String.valueOf(store));
	}

	public String getPassword() throws CannotAccessPropertyException {
		if (password == null && isStorePassword()) {
			try {
				password = context.getSecuredCredentialsStore().getPassword(secureStoreScopeKey(type.getName(), getRunTargetId()));
			} catch (StorageException e) {
				throw new CannotAccessPropertyException("Cannot read password.", e);
			}
		}
		return password;
	}

	public void setPassword(String password) throws CannotAccessPropertyException {
		this.password = password;
		if (isStorePassword()) {
			try {
				context.getSecuredCredentialsStore().setPassword(password, secureStoreScopeKey(type.getName(), getRunTargetId()));
			} catch (StorageException e) {
				throw new CannotAccessPropertyException("Cannot store password.", e);
			}
		} else {
			// Shall we clear out the password that might be stored if we don't remember the password?
		}
	}

	public String getUrl() {
		return map.get(URL_PROP);
	}

	public void put(String key, String value) {
		if (value == null) {
			map.remove(key);
		} else {
			map.put(key, value);
		}
	}

	protected String secureStoreScopeKey(String targetTypeName, String targetId) {
		return targetTypeName+":"+targetId;
	}

}
