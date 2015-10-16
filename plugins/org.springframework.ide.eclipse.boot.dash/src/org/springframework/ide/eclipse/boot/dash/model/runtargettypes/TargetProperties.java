/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
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
	public static final String PASSWORD_PROP = "password";
	public static final String URL_PROP = "url";

	protected Map<String, String> map;
	private RunTargetType type;

	public TargetProperties(Map<String, String> map, RunTargetType type) {
		this.map = map;
		this.type = type;
	}

	public TargetProperties() {
		this.map = new HashMap<String, String>();
	}

	public TargetProperties(RunTargetType type) {
		this(new HashMap<String, String>(), type);
	}

	public TargetProperties(RunTargetType type, String runTargetId) {
		this(new HashMap<String, String>(), type);
		put(RUN_TARGET_ID, runTargetId);
	}

	public TargetProperties(Map<String, String> map, RunTargetType type, String runTargetId) {
		this(type);
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
		return new HashMap<String, String>(map);
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

	public String getPassword() {
		return map.get(PASSWORD_PROP);
	}

	public void setPassword(String password) {
		map.put(PASSWORD_PROP, password);
	}

	public String getUrl() {
		return map.get(URL_PROP);
	}

	public void put(String key, String value) {
		map.put(key, value);
	}
}
