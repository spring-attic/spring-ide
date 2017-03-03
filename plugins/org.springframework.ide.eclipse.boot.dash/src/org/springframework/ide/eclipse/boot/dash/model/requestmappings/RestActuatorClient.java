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
import java.util.List;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

/**
 * Concretization of {@link ActuatorClient} which uses spring rest template to
 * connect to actuator endpoint.
 *
 * @author Kris De Volder
 */
public class RestActuatorClient extends ActuatorClient {


	private RestOperations rest;
	private URI target;

	public RestActuatorClient(URI target, TypeLookup typeLookup) {
		this(target, typeLookup, new RestTemplate());
	}

	public RestActuatorClient(URI target, TypeLookup typeLookup, RestTemplate rest) {
		super(typeLookup);
		this.target = target;
		this.rest = rest;
	}

	@Override
	protected String getRequestMappingData() throws Exception {
		return rest.getForObject(target+"/mappings", String.class);
	}
}
