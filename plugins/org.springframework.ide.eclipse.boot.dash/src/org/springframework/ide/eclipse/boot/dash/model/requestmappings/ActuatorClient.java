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
package org.springframework.ide.eclipse.boot.dash.model.requestmappings;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.web.client.RestTemplate;

public class ActuatorClient {

	/**
	 * Wraps a (key,value) pair from the json returned from the 'mappings' endpoint in the
	 * actuator.
	 *
	 * @author Kris De Volder
	 */
	private static class RequestMappingImpl implements RequestMapping {

		/*
   There are two styles of entries:

   1) key is a 'path' String. May contain patters like "**"
	   "/** /favicon.ico":{
          "bean":"faviconHandlerMapping"
       }

   2) key is a 'almost json' String
       "{[/bye],methods=[],params=[],headers=[],consumes=[],produces=[],custom=[]}":{
          "bean":"requestMappingHandlerMapping",
          "method":"public java.lang.String demo.MyController.bye()"
       }
		 */

		private String key;
		private JSONObject beanInfo;
		private String path;

		RequestMappingImpl(String key, JSONObject beanInfo) {
			this.key = key;
			this.beanInfo = beanInfo;
		}

		@Override
		public String getPath() {
			if (path==null) {
				path = extractPath(key);
			}
			return path;
		}

		private String extractPath(String key) {
			if (key.startsWith("{[")) { //Case 2 (see above)
				//An almost json string. Unfortunately not really json so we can't
				//use org.json or jackson Mapper to properly parse this.
				int start = 2; //right after first '['
				int end = key.indexOf(']');
				if (end>=2) {
					return key.substring(start, end);
				}
			}
			//Case 1, or some unanticipated stuff.
			//Assume the key is the path, which is right for Case 1
			// and  probably more useful than null for 'unanticipated stuff'.
			return key;
		}

		@Override
		public String toString() {
			return "RequestMapping("+key+")";
		}
	}

	private RestTemplate rest = new RestTemplate();
	private URI target;

	public ActuatorClient(URI target) {
		this.target = target;
	}

	public List<RequestMapping> getRequestMappings() {
		try {
			String json = rest.getForObject(target+"/mappings", String.class);
			if (json!=null) {
				//System.out.println("Got some json:\n"+json);
				return parse(json);
			}
		} catch (Exception e) {
			BootDashActivator.log(e);
		}
		return null;
	}


	@SuppressWarnings("unchecked")
	private List<RequestMapping> parse(String json) throws Exception {
		JSONTokener tokener = new JSONTokener(json);
		JSONObject obj = new JSONObject(tokener);
		Iterator<String> keys = obj.keys();
		List<RequestMapping> result = new ArrayList<RequestMapping>();
		while (keys.hasNext()) {
			String key = keys.next();
			JSONObject value = obj.getJSONObject(key);
			result.add(new RequestMappingImpl(key, value));
		}
		return result;
	}


}
