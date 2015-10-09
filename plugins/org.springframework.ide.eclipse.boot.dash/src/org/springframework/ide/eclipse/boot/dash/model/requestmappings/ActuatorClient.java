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

import org.eclipse.jdt.core.IMethod;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.JLRMethodParser.JLRMethod;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

public class ActuatorClient {

	/**
	 * Wraps a (key,value) pair from the json returned from the 'mappings' endpoint in the
	 * actuator.
	 *
	 * @author Kris De Volder
	 */
	static class RequestMappingImpl extends AbstractRequestMapping {

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
		private JLRMethod methodData;

		RequestMappingImpl(String key, JSONObject beanInfo, TypeLookup typeLookup) {
			super(typeLookup);
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

		@Override
		public String getFullyQualifiedClassName() {
			JLRMethod m = getMethodData();
			if (m!=null) {
				return m.getFQClassName();
			}
			return null;
		}

		@Override
		public String getMethodName() {
			JLRMethod m = getMethodData();
			if (m!=null) {
				return m.getMethodName();
			}
			return null;
		}

		/**
		 * Returns the raw string found in the requestmapping info. This is a 'toString' value
		 * of java.lang.reflect.Method object.
		 */
		public String getMethodString() {
			try {
				if (beanInfo!=null) {
					if (beanInfo.has("method")) {
						return beanInfo.getString("method");
					}
				}
			} catch (Exception e) {
				BootDashActivator.log(e);
			}
			return null;
		}

		private JLRMethod getMethodData() {
			if (methodData==null) {
				methodData = JLRMethodParser.parse(getMethodString());
			}
			return methodData;
		}



	}

	private RestOperations rest;
	private URI target;
	private TypeLookup typeLookup;

	public ActuatorClient(URI target, TypeLookup typeLookup) {
		this(target, typeLookup, new RestTemplate());
	}

	public ActuatorClient(URI target, TypeLookup typeLookup, RestTemplate rest) {
		this.target = target;
		this.typeLookup = typeLookup;
		this.rest = rest;
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
			result.add(new RequestMappingImpl(key, value, typeLookup));
		}
		return result;
	}


}
