/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model.requestmappings;

import javax.inject.Provider;

import org.springframework.ide.eclipse.boot.launch.util.JMXClient;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Concretization of abstract {@link ActuatorClient} which uses JMX to connect
 * to actuator endpoint(s).
 *
 * @author Kris De Volder
 */
public class JMXActuatorClient extends ActuatorClient {

	private static final String OBJECT_NAME = "org.springframework.boot:type=Endpoint,name=requestMappingEndpoint";
	private static final String ATTRIBUTE_NAME = "Data";
	private final Provider<Integer> jmxPort;

	public JMXActuatorClient(TypeLookup typeLookup, Provider<Integer> jmxPort) {
		super(typeLookup);
		this.jmxPort = jmxPort;
	}

	@Override
	protected String getRequestMappingData() throws Exception {
		Integer port = jmxPort.get();
		if (port==null || port <= 0) {
			throw new IllegalStateException("JMX port not set");
		}
		JMXClient client = new JMXClient(port, OBJECT_NAME);
		Object obj = client.getAttribute(ATTRIBUTE_NAME);
		if (obj!=null) {
			//TODO: Can we avoid conversion to string only to parse it again later?
			return new ObjectMapper().writeValueAsString(obj);
		}
		return null;
	}

}
