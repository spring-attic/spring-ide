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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.ide.eclipse.beans.ui.live.model.TypeLookup;
import org.springsource.ide.eclipse.commons.livexp.util.Log;

import com.google.common.base.Objects;

/**
 * Boot 2.0 compatible request mapping implementation
 *
 * @author Alex Boyko
 *
 */
public class RequestMapping2x extends AbstractRequestMapping {

	public RequestMapping2x(TypeLookup typeLookup, String path, String fqClassName, String methodName, String methodString) {
		super(typeLookup);
		this.path = path;
		this.fqClassName = fqClassName;
		this.methodName = methodName;
		this.methodString = methodString;
	}

	private String path;
	private String fqClassName;
	private String methodName;
	private String methodString;

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public String getFullyQualifiedClassName() {
		return fqClassName;
	}

	@Override
	public String getMethodName() {
		return methodName;
	}

	@Override
	public String getMethodString() {
		return methodString;
	}

	public static Collection<RequestMapping2x> create(TypeLookup typeLookup, String methodString, JSONObject details) {
		try {
			if (details != null) {

				JSONObject requestMappingConditionals = details.optJSONObject("requestMappingConditions");
				if (requestMappingConditionals != null) {
					String[] paths = extractPaths(requestMappingConditionals);
					String fqClassName = null;
					String methodName = null;
					if (details.has("handlerMethod")) {
						JSONObject handlerMethod = details.getJSONObject("handlerMethod");
						fqClassName = handlerMethod.getString("className");
						methodName = handlerMethod.getString("name");
					} else if (details.has("handlerFunction")) {
						fqClassName = details.getString("handlerFunction");
						int idx = fqClassName.indexOf("$$");
						if (idx >= 0) {
							fqClassName = fqClassName.substring(0, idx);
						}
					}
					final String clazz = fqClassName;
					final String method = methodName;
					return Arrays.stream(paths)
							.map(path -> new RequestMapping2x(typeLookup, path, clazz, method, methodString))
							.collect(Collectors.toList());
				}
			}
		} catch (JSONException e) {
			Log.log(e);
		}
		return Collections.emptyList();
	}

	private static String[] extractPaths(JSONObject rmConditionals) throws JSONException {
		JSONArray jsonArray = rmConditionals.getJSONArray("patterns");
		String[] paths = new String[jsonArray.length()];
		for (int i = 0; i < jsonArray.length(); i++) {
			paths[i] = jsonArray.getString(i);
		}
		return paths;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((methodString == null) ? 0 : methodString.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
		result = prime * result + ((fqClassName == null) ? 0 : fqClassName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RequestMapping2x other = (RequestMapping2x) obj;
		return Objects.equal(methodString, other.methodString)
			&& Objects.equal(fqClassName, other.fqClassName)
			&& Objects.equal(path, other.path)
			&& Objects.equal(methodName, other.methodName);
	}

	@Override
	public String toString() {
		return "RequestMapping2x("+path+")";
	}

}
