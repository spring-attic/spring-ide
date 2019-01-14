/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.environment.ui.live.model;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.ide.eclipse.beans.ui.live.model.JsonParser;

import com.google.common.collect.ImmutableList;

public class LiveEnvJsonParser1x implements JsonParser<LiveEnvModel> {


	public LiveEnvJsonParser1x() {
	}
	

	
	@Override
	public LiveEnvModel parse(String jsonInput) throws Exception {
		
		JSONArray contextsArray = toJson(jsonInput);
		
		

		LiveEnvModel model = new LiveEnvModel(null, null);

		return model;
	}
	
	protected JSONArray toJson(String json) throws JSONException {		
		return new JSONArray(json);
	}
	
	private List<Profile> parseProfiles(JSONObject envObj) {
		Object _profiles = envObj.opt("profiles"); 

//		if (_profiles instanceof JSONArray) {
//			JSONArray profiles = (JSONArray) _profiles;
//			ImmutableList.Builder<String> list = ImmutableList.builder();
//			for (Object object : profiles) {
//				if (object instanceof String) {
//					list.add((String) object);
//				}
//			}
//			return list.build();
//		}		
		return null;
	}
	
}
