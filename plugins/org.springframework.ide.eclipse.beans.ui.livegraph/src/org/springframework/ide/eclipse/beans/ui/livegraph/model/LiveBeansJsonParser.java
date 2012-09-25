/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.livegraph.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Leo Dos Santos
 */
public class LiveBeansJsonParser {

	public static Collection<LiveBean> parse(String jsonInput) throws JSONException {
		Map<String, LiveBean> beansMap = new HashMap<String, LiveBean>();
		JSONObject json = new JSONObject(jsonInput);
		JSONArray names = json.names();

		// construct LiveBeans
		for (int i = 0; i < names.length(); i++) {
			JSONObject candidate = json.optJSONObject(names.getString(i));
			if (candidate != null && candidate.has("bean")) {
				LiveBean bean = new LiveBean(candidate.getString("bean"));
				if (candidate.has("scope")) {
					bean.addAttribute("scope", candidate.getString("scope"));
				}
				if (candidate.has("type")) {
					bean.addAttribute("type", candidate.getString("type"));
				}
				if (candidate.has("resource")) {
					bean.addAttribute("resource", candidate.getString("resource"));
				}
				beansMap.put(bean.getId(), bean);
			}
		}

		// populate LiveBean dependencies
		for (int i = 0; i < names.length(); i++) {
			JSONObject candidate = json.optJSONObject(names.getString(i));
			if (candidate != null && candidate.has("bean")) {
				LiveBean bean = beansMap.get(candidate.getString("bean"));
				JSONArray dependencies = candidate.optJSONArray("dependencies");
				if (dependencies != null) {
					for (int j = 0; j < dependencies.length(); j++) {
						String child = dependencies.getString(j);
						LiveBean childBean = beansMap.get(child);
						if (childBean != null) {
							bean.addChild(childBean);
						}
					}
				}
			}
		}
		return beansMap.values();
	}

}
