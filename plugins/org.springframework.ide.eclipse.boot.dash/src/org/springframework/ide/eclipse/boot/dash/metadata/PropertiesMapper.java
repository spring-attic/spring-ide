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
package org.springframework.ide.eclipse.boot.dash.metadata;

import java.io.IOException;

import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

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
				Log.log(e);
			}
		}
		return val;
	}

	public String convertToString(T properties) throws Exception {

		if (getMapper().canSerialize(properties.getClass())) {
			try {
				return getMapper().writeValueAsString(properties);
			} catch (IOException e) {
				Log.log(e);
			}
		} else {
			throw ExceptionUtil.coreException("Failed to serialize: " + properties.getClass().getName());
		}
		return null;
	}

}