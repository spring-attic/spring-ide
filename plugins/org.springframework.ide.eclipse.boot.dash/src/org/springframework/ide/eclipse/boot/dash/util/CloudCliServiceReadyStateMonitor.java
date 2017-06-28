/*******************************************************************************
 * Copyright (c) 2017 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.util;

import java.io.IOException;

import javax.inject.Provider;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;

import org.springframework.ide.eclipse.boot.launch.util.SpringApplicationLifecycleClient;

/**
 * Spring Cloud CLI 1.3.X Service ready state monitor implementation. Based on
 * JMX Connection and examining bean's attribute value.
 *
 * @author Alex Boyko
 *
 */
public class CloudCliServiceReadyStateMonitor extends AbstractPollingAppReadyStateMonitor {

	private Provider<JMXConnector> jmxConnectionProvider;
	private JMXConnector connector;
	private ObjectName objectName;

	public CloudCliServiceReadyStateMonitor(Provider<JMXConnector> jmxConnectionProvider, String serviceId) {
		super();
		this.jmxConnectionProvider = jmxConnectionProvider;
		this.objectName = SpringApplicationLifecycleClient.toObjectName("launcher." + serviceId + ":type=RestartEndpoint,name=restartEndpoint");
	}

	@Override
	public void dispose() {
		if (connector != null) {
			try {
				connector.close();
			} catch (IOException e) {
				// Ignore - process might be dead already
			}
			connector = null;
		}
		super.dispose();
	}

	@Override
	protected boolean checkReady() {
		try {
			if (connector == null) {
				connector = jmxConnectionProvider.get();
			}
			if (connector != null) {
				MBeanServerConnection connection = connector.getMBeanServerConnection();
				try {
					return (Boolean) connection.getAttribute(objectName, "Running");
				} catch (AttributeNotFoundException e) {
					throw new IllegalStateException(
							"Unexpected: attribute 'Running' not available", e);
				} catch (InstanceNotFoundException e) {
					return false; // Instance not available yet
				} catch (MBeanException e) {
					throw new Exception(e.getCause());
				} catch (ReflectionException e) {
					throw new Exception("Failed to retrieve Running attribute",
							e.getCause());
				}
			}
		} catch (Exception e) {
			if (connector != null) {
				try {
					connector.close();
				} catch (IOException ex) {
					// Ignore - process might be dead already
				}
				connector = null;
			}
		}
		return false;
	}

}
