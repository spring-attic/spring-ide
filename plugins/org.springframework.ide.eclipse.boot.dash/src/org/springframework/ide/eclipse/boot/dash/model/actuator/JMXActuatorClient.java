/*******************************************************************************
 * Copyright (c) 2017, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model.actuator;

import java.util.Set;

import javax.inject.Provider;
import javax.management.InstanceNotFoundException;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.ide.eclipse.beans.ui.live.model.TypeLookup;
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
		final String version;
		public OperationInfo(String objectName, String operationName, String version) {
			this.objectName = objectName;
			this.operationName = operationName;
			this.version = version;
		}
	}

	private static final OperationInfo[] REQUEST_MAPPINGS_OPERATIONS = {
			new OperationInfo("org.springframework.boot:type=Endpoint,name=Mappings", "mappings", "2"), //Boot 2.x
			new OperationInfo("org.springframework.boot:type=Endpoint,name=requestMappingEndpoint", "getData", "1") //Boot 1.x
	};

	private static final OperationInfo[] BEANS_OPERATIONS = {
			new OperationInfo("org.springframework.boot:type=Endpoint,name=Beans", "beans", "2"), //Boot 2.x
			new OperationInfo("org.springframework.boot:type=Endpoint,name=beansEndpoint", "getData", "1") //Boot 1.x
	};

	private static final OperationInfo[] ENV_OPERATIONS = {
			new OperationInfo("org.springframework.boot:type=Endpoint,name=Env", "environment", "2"), //Boot 2.x
			new OperationInfo("org.springframework.boot:type=Endpoint,name=environmentEndpoint", "Data", "1") //Boot 1.x
	};

	private JMXClient client = null;
	private Integer port = null;

	public JMXActuatorClient(TypeLookup typeLookup, Provider<Integer> jmxPort) {
		super(typeLookup);
		this.portProvider = jmxPort;
	}

	@Override
	protected ImmutablePair<String, String> getRequestMappingData() throws Exception {
		return getDataFrom(REQUEST_MAPPINGS_OPERATIONS);
	}

	@Override
	protected ImmutablePair<String, String> getBeansData() throws Exception {
		return getDataFrom(BEANS_OPERATIONS);
	}

	@Override
	protected ImmutablePair<String, String> getEnvData() throws Exception {
		return getDataFrom(ENV_OPERATIONS);
	}

	protected ImmutablePair<String, String> getDataFrom(OperationInfo[] infos) throws Exception {
		try {
			JMXClient client = getClient();
			if (client!=null && infos!=null) {
				for (OperationInfo op : infos) {
					try {
						Object obj = client.callOperation(op.objectName, op.operationName);
						if (obj!=null) {
							return ImmutablePair.of(new ObjectMapper().writeValueAsString(obj), op.version);
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
