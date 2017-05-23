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

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;


/**
 * Concretization of {@link ActuatorClient} which uses spring rest template to
 * connect to actuator endpoint.
 *
 * @author Kris De Volder
 */
public class RestActuatorClient extends ActuatorClient {

	private URI target;
	private Client client;

	public RestActuatorClient(URI target, TypeLookup typeLookup) {
		this(target, typeLookup, ClientBuilder.newClient());
	}

	public RestActuatorClient(URI target, TypeLookup typeLookup, Client client) {
		super(typeLookup);
		this.target = target;
		this.client = client;
	}

	@Override
	protected String getRequestMappingData() throws Exception {
		return client.target(target).path("/mappings").request().get(String.class);
	}
}
