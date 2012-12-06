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

	public static Collection<LiveBean> parse(String jsonInput, String applicationName) throws JSONException {
		Map<String, LiveBean> beansMap = new HashMap<String, LiveBean>();
		// structure is an array of context descriptions, each containing an
		// array of beans
		JSONArray contextsArray = new JSONArray(jsonInput);
		for (int i = 0; i < contextsArray.length(); i++) {
			JSONObject contextObj = contextsArray.optJSONObject(i);
			if (contextObj != null) {
				// TODO: group beans by context
				JSONArray beansArray = contextObj.optJSONArray("beans");
				String context = contextObj.getString(LiveBean.ATTR_CONTEXT);
				beansMap.putAll(parseBeans(beansArray, applicationName, context));
			}
		}
		return beansMap.values();
	}

	private static Map<String, LiveBean> parseBeans(JSONArray beansArray, String applicationName, String context)
			throws JSONException {
		Map<String, LiveBean> beansMap = new HashMap<String, LiveBean>();
		if (beansArray != null) {
			// construct LiveBeans
			for (int i = 0; i < beansArray.length(); i++) {
				JSONObject candidate = beansArray.getJSONObject(i);
				if (candidate != null && candidate.has(LiveBean.ATTR_BEAN)) {
					LiveBean bean = new LiveBean(candidate.getString(LiveBean.ATTR_BEAN));
					bean.addAttribute(LiveBean.ATTR_CONTEXT, context);
					if (candidate.has(LiveBean.ATTR_SCOPE)) {
						bean.addAttribute(LiveBean.ATTR_SCOPE, candidate.getString(LiveBean.ATTR_SCOPE));
					}
					if (candidate.has(LiveBean.ATTR_TYPE)) {
						bean.addAttribute(LiveBean.ATTR_TYPE, candidate.getString(LiveBean.ATTR_TYPE));
					}
					if (candidate.has(LiveBean.ATTR_RESOURCE)) {
						bean.addAttribute(LiveBean.ATTR_RESOURCE, candidate.getString(LiveBean.ATTR_RESOURCE));
					}
					if (applicationName != null) {
						bean.addAttribute(LiveBean.ATTR_APPLICATION, applicationName);
					}
					beansMap.put(bean.getId(), bean);
				}
			}
			// populate LiveBean dependencies
			for (int i = 0; i < beansArray.length(); i++) {
				JSONObject candidate = beansArray.optJSONObject(i);
				if (candidate != null && candidate.has(LiveBean.ATTR_BEAN)) {
					LiveBean bean = beansMap.get(candidate.getString(LiveBean.ATTR_BEAN));
					JSONArray dependencies = candidate.optJSONArray(LiveBean.ATTR_DEPENDENCIES);
					if (dependencies != null) {
						for (int j = 0; j < dependencies.length(); j++) {
							String dependency = dependencies.getString(j);
							LiveBean dependencyBean = beansMap.get(dependency);
							if (dependencyBean != null) {
								bean.addDependency(dependencyBean);
							}
						}
					}
				}
			}
		}
		return beansMap;
	}

}
