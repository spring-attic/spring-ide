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
package org.springframework.ide.eclipse.boot.launch.util;

import javax.management.remote.JMXConnector;

/**
 * Creates and manages an instance of {@link SpringApplicationLifecycleClient}.
 *
 * @author Kris De Volder
 */
public class SpringApplicationLifeCycleClientManager {

	private int jmxPort;
	private JMXConnector connector;
	private SpringApplicationLifecycleClient client;

	public SpringApplicationLifeCycleClientManager(int jmxPort) {
		this.jmxPort = jmxPort;
	}

	/**
	 * Dispose of current client and JMX connection. This does not
	 * make the manager itself unusable, as an attempt will be made to
	 * re-establish the connection the next time it is needed.
	 */
	public synchronized void disposeClient() {
		try {
			if (connector!=null) {
				connector.close();
			}
		} catch (Exception e) {
			//ignore
		}
		client = null;
		connector = null;
	}

	/**
	 * Try to obtain a client, may return null if a connection could not be established.
	 */
	public SpringApplicationLifecycleClient getLifeCycleClient() {
		try {
			if (client==null) {
				connector = SpringApplicationLifecycleClient.createLocalJmxConnector(jmxPort);
				client = new SpringApplicationLifecycleClient(
						connector.getMBeanServerConnection(),
						SpringApplicationLifecycleClient.DEFAULT_OBJECT_NAME
				);
			}
			return client;
		} catch (Exception e) {
			//e.printStackTrace();
			//Someting went wrong creating client (most likely process we are trying to connect
			// doesn't exist yet or has been terminated.
			disposeClient();
		}
		return null;
	}

}
