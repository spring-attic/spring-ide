/*******************************************************************************
 * Copyright (c) 2015-2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model.requestmappings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.requestmappings.JLRMethodParser.JLRMethod;
import org.springframework.ide.eclipse.boot.util.Log;

/**
 * Abstract implementation of a ActuatorClient. The actuar client connects
 * to an actuator endpoint retrieving some information from a running spring boot app.
 * <p>
 * This implementation is abstract because there is more than one way that we can
 * connect to an actuator endpoint and retrieve the data from it. The method
 * to retrieve the data is therefore an abstract method.
 *
 * @author Kris De Volder
 */
public abstract class ActuatorClient {

	private final TypeLookup typeLookup;

	public ActuatorClient(TypeLookup typeLookup) {
		this.typeLookup = typeLookup;
	}

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

		private JSONObject beanInfo;
		private String path;
		private JLRMethod methodData;

		RequestMappingImpl(String path, JSONObject beanInfo, TypeLookup typeLookup) {
			super(typeLookup);
			this.path = path;
			this.beanInfo = beanInfo;
		}

		@Override
		public String getPath() {
			return path;
		}

		@Override
		public String toString() {
			return "RequestMapping("+path+")";
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

		private static Stream<String> processOrPaths(String pathExp) {
			if (pathExp.contains("||")) {
				String[] paths = pathExp.split(Pattern.quote("||"));
				ArrayList<RequestMapping> list = new ArrayList<>();
				return Stream.of(paths).map(String::trim);
			} else {
				return Stream.of(pathExp);
			}
		}


		private static String extractPath(String key) {
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


		public static Collection<RequestMappingImpl> create(String key, JSONObject value, TypeLookup typeLookup) {
			return processOrPaths(extractPath(key))
					.map(path -> new RequestMappingImpl(path, value, typeLookup))
					.collect(Collectors.toList());
		}
	}

	@SuppressWarnings("unchecked")
	private List<RequestMapping> parse(String json) throws JSONException {
		JSONObject obj = new JSONObject(json);
		Iterator<String> keys = obj.keys();
		List<RequestMapping> result = new ArrayList<>();
		while (keys.hasNext()) {
			String rawKey = keys.next();
			JSONObject value = obj.getJSONObject(rawKey);
			Collection<RequestMappingImpl> mappings = RequestMappingImpl.create(rawKey, value, typeLookup);
			result.addAll(mappings);
		}
		return result;
	}

	public List<RequestMapping> getRequestMappings() {
		try {
			String json = getRequestMappingData();
			if (json!=null) {
				return parse(json);
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

	protected abstract String getRequestMappingData() throws Exception;
}
