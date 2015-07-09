/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;

public class PropertiesMapper<T> {

	private ObjectMapper mapper;

	public PropertiesMapper() {

	}

	protected ObjectMapper getMapper() {
		if (mapper == null) {
			mapper = new ObjectMapper();
		}
		return mapper;
	}

	public T convert(String json) {
		T val = null;
		if (json != null) {
			try {
				val = getMapper().readValue(json, new TypeReference<T>() {
				});
			} catch (IOException e) {
				BootDashActivator.log(e);
			}
		}
		return val;
	}

	public String convertToString(T properties) {

		if (getMapper().canSerialize(properties.getClass())) {
			try {
				return getMapper().writeValueAsString(properties);
			} catch (IOException e) {
				BootDashActivator.log(e);
			}
		} else {
			BootDashActivator.log(new Error("Failed to serialize: " + properties.getClass().getName()));
		}
		return null;
	}

}