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
package org.springframework.ide.eclipse.boot.dash.model.actuator;

import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansJsonParser;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansJsonParser2;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansModel;
import org.springframework.ide.eclipse.beans.ui.live.model.TypeLookup;
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

	private static final VersionRange BEANS_PARSER_VERSION_1_RANGE = new VersionRange("[1.0.0, 2.0.0)");

	private final TypeLookup typeLookup;

	public ActuatorClient(TypeLookup typeLookup) {
		this.typeLookup = typeLookup;
	}


	private List<RequestMapping> parseRequestMappings(String json, String version) throws JSONException {
 		JSONObject obj = new JSONObject(json);
 		RequestMappingsParser parser;
		if (version.equals("2")) {
			// Boot 2.x
			parser = new Boot2RequestMappingsParser();
		} else {
			//Boot 1.x
			parser = new Boot1RequestMappingsParser();
		}
		return parser.parse(obj, typeLookup);
	}

	public List<RequestMapping> getRequestMappings() {
		try {
			ImmutablePair<String, String> data = getRequestMappingData();
			if (data != null) {
				String json = data.left;
				if (json!=null) {
					System.out.println(json);
					return parseRequestMappings(json, data.right);
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

	public LiveBeansModel getBeans() {
		try {
			ImmutablePair<String, String> data = getBeansData();
			if (data != null) {
				String json = data.left;
				String version = data.right;
				if (json != null) {
					if (version != null) {
						if (BEANS_PARSER_VERSION_1_RANGE.includes(Version.valueOf(version))) {
							return new LiveBeansJsonParser(typeLookup, json).parse();
						}
					}
					return new LiveBeansJsonParser2(typeLookup, json).parse();
				}
			}
		} catch (Exception e) {
			Log.log(e);
		}
		return null;
	}

	protected abstract ImmutablePair<String, String> getRequestMappingData() throws Exception;

	protected abstract ImmutablePair<String, String> getBeansData() throws Exception;
}
