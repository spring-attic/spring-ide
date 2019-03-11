/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/

package org.springframework.ide.eclipse.boot.dash.prefs;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class RemoteAppsPrefs {

	public static final String REMOTE_APPS_KEY = "remote-apps";

	private IEclipsePreferences prefs = BootDashActivator.getPreferences();

	public void setRawJson(String json) {
		prefs.put(REMOTE_APPS_KEY, json);
	}

	public String getRawJson() {
		return prefs.get(REMOTE_APPS_KEY, "");
	}

	public List<Pair<String,String>> getRemoteAppData() {
		String json = getRawJson();
		try {
			return parse(json);
		} catch (Exception e) {
			Log.warn("Problem parsing manually configured boot remote apps data: "+ExceptionUtil.getMessage(e));
			return ImmutableList.of();
		}
	}

	public static List<Pair<String, String>> parse(String json) throws JSONException {
		Builder<Pair<String,String>> buider = ImmutableList.builder();
		if (!json.trim().equals("")) {
			JSONArray remoteApps = new JSONArray(json);
			for (int i = 0; i < remoteApps.length(); i++) {
				JSONObject app = remoteApps.getJSONObject(i);
				String host = app.getString("host");
				String jmxUrl = app.getString("jmxurl");
				if (host!=null && jmxUrl!=null) {
					buider.add(Pair.of(jmxUrl, host));
				}
			}
		}
		return buider.build();
	}

	public static void addListener(Runnable runnable) {
		BootDashActivator.getPreferences().addPreferenceChangeListener(event -> {
			if (event.getKey().equals(REMOTE_APPS_KEY)) {
				runnable.run();
			}
		});
	}

}
