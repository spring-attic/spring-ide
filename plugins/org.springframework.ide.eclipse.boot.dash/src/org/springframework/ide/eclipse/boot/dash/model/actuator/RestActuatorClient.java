/*******************************************************************************
 * Copyright (c) 2015, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model.actuator;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.ide.eclipse.beans.ui.live.model.TypeLookup;


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
	protected ImmutablePair<String, String> getRequestMappingData() throws Exception {
		return ImmutablePair.of(client.target(target).path("/mappings").request().get(String.class), null);
	}

	@Override
	protected ImmutablePair<String, String> getBeansData() throws Exception {
		return null;
	}

	@Override
	protected ImmutablePair<String, String> getEnvData() throws Exception {
		return null;
	}

}
