/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model.actuator;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.ide.eclipse.beans.ui.live.model.TypeLookup;

public class Boot2RequestMappingsParser implements RequestMappingsParser {

	@Override
	public List<RequestMapping> parse(JSONObject obj, TypeLookup typeLookup) throws JSONException {
		JSONObject contexts = obj.getJSONObject("contexts");
		List<RequestMapping> result = new ArrayList<>();
		for (String contextId : keys(contexts)) {
			JSONObject mappings = contexts.getJSONObject(contextId).getJSONObject("mappings");

			JSONArray rmArray = null;
			if (mappings.has("dispatcherServlets")) {
				// Regular Web starter endpoints RMs JMX beans format
				rmArray = mappings.getJSONObject("dispatcherServlets").getJSONArray("dispatcherServlet");
			} else if (mappings.has("dispatcherHandlers")) {
				// WebFlux endpoints RMs JMX bean format
				rmArray = mappings.getJSONObject("dispatcherHandlers").getJSONArray("webHandler");
			}

			if (rmArray != null) {
				for (int i = 0; i < rmArray.length(); i++) {
					JSONObject servlet = rmArray.getJSONObject(i);
					JSONObject details = servlet.optJSONObject("details");
					if (details == null) {
						// Fall back to 1.x for missing "details" property, i.e. no method handler defined
						result.addAll(RequestMapping1x.create(servlet.getString("predicate"), servlet.getString("handler"), typeLookup));
					} else {
						result.addAll(RequestMapping2x.create(typeLookup, servlet.getString("handler"), details));
					}
				}
			}

		}
		return result;
	}

	/**
	 * Convenience method, makes up for the fact that in Eclipse land we have to use an ancient version of org.json.
	 */
	private Iterable<String> keys(JSONObject obj) {
		return obj::keys;
	}

}
