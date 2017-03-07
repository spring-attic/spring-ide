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
package org.springframework.ide.eclipse.boot.launch.util;

import java.io.IOException;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.springsource.ide.eclipse.commons.livexp.ui.Disposable;

/**
 * A JMX client for interacting with specific mbean.
 *
 * @author Stephane Nicoll
 * @author Kris De Volder
 */
public class JMXClient implements Disposable {

	private JMXConnector connector;
	private MBeanServerConnection connection;
	private ObjectName objectName;

	public JMXClient(int port, String objectName) throws IOException {
		this(createLocalJmxConnector(port), objectName);
	}

	private JMXClient(JMXConnector connector, String objectName) throws IOException {
		this(connector, connector.getMBeanServerConnection(), objectName);
	}

	@Override
	public void dispose() {
		try {
			this.connector.close();
		} catch (IOException e) {
			//Ignore
		}
	}

	private JMXClient(JMXConnector connector, MBeanServerConnection connection, String objectName) {
		this.connector = connector;
		this.connection = connection;
		this.objectName = toObjectName(objectName);
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttribute(Class<T> klass, String attributeName) throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		Object value = getAttribute(attributeName);
		if (value==null || klass.isInstance(value)) {
			return (T)value;
		} else {
			throw new ClassCastException("Value '"+value+"' can't be cast to "+klass);
		}
	}

	public Object getAttribute(String attributeName) throws AttributeNotFoundException, InstanceNotFoundException, MBeanException, ReflectionException, IOException {
		return this.connection.getAttribute(objectName, attributeName);
	}

	private ObjectName toObjectName(String name) {
		try {
			return new ObjectName(name);
		}
		catch (MalformedObjectNameException ex) {
			throw new IllegalArgumentException("Invalid jmx name '" + name + "'");
		}
	}

	/**
	 * Create a connector for an {@link javax.management.MBeanServer} exposed on the
	 * current machine and the current port. Security should be disabled.
	 * @param port the port on which the mbean server is exposed
	 * @return a connection
	 * @throws IOException if the connection to that server failed
	 */
	public static JMXConnector createLocalJmxConnector(int port) throws IOException {
		String url = "service:jmx:rmi:///jndi/rmi://127.0.0.1:" + port + "/jmxrmi";
		JMXServiceURL serviceUrl = new JMXServiceURL(url);
		return JMXConnectorFactory.connect(serviceUrl, null);
	}

}
