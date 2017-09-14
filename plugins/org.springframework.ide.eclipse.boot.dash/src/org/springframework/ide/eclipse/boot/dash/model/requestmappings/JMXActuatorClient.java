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

import java.util.Set;

import javax.inject.Provider;
import javax.management.InstanceNotFoundException;

import org.springframework.ide.eclipse.boot.launch.util.JMXClient;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;

/**
 * Concretization of abstract {@link ActuatorClient} which uses JMX to connect
 * to actuator endpoint(s).
 *
 * @author Kris De Volder
 */
public class JMXActuatorClient extends ActuatorClient {

	private final Provider<Integer> portProvider;

	static class OperationInfo {
		final String objectName;
		final String operationName;
		public OperationInfo(String objectName, String operationName) {
			this.objectName = objectName;
			this.operationName = operationName;
		}
	}

	private static final OperationInfo[] OPERATIONS = {
			new OperationInfo("org.springframework.boot:type=Endpoint,name=Mappings", "mappings"), //Boot 2.x
			new OperationInfo("org.springframework.boot:type=Endpoint,name=requestMappingEndpoint", "getData") //Boot 1.x
	};

	private JMXClient client = null;
	private Integer port = null;

	public JMXActuatorClient(TypeLookup typeLookup, Provider<Integer> jmxPort) {
		super(typeLookup);
		this.portProvider = jmxPort;
	}

	@Override
	protected String getRequestMappingData() throws Exception {
		try {
			JMXClient client = getClient();
			if (client!=null) {
				for (OperationInfo op : OPERATIONS) {
					try {
						Object obj = client.callOperation(op.objectName, op.operationName);
						if (obj!=null) {
							return new ObjectMapper().writeValueAsString(obj);
						}
					} catch (InstanceNotFoundException e) {
						//Ignore and try other mbean
					}
				}
			}
		} catch (Exception e) {
			disposeClient(); //Client may be in broken state, do not reuse.
			if (!isExpectedException(e)) {
				throw e;
			}
		}
		return null;
	}

	private static final Set<String> EXPECTED_EXCEPTIONS = ImmutableSet.of(
			"ConnectException",
			"InstanceNotFoundException"
	);

	private boolean isExpectedException(Exception _e) {
		Throwable e = ExceptionUtil.getDeepestCause(_e);
		String className = e.getClass().getSimpleName();
		return EXPECTED_EXCEPTIONS.contains(className);
	}

	private synchronized JMXClient getClient() throws Exception {
		Integer currentPort = portProvider.get();
		if (currentPort==null) return null;
		if (!currentPort.equals(port) || client==null) {
			disposeClient();
			port = currentPort;
			client = new JMXClient(currentPort);
		}
		return client;
	}

	private void disposeClient() {
		JMXClient client = this.client;
		if (client!=null) {
			this.client = null;
			client.dispose();
		}
	}

}
