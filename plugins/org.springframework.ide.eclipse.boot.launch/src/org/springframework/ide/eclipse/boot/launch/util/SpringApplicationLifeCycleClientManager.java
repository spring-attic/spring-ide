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

import javax.inject.Provider;
import javax.management.remote.JMXConnector;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.ILaunch;
import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;

/**
 * Creates and manages an instance of {@link SpringApplicationLifecycleClient}.
 *
 * @author Kris De Volder
 */
public class SpringApplicationLifeCycleClientManager {

	private Provider<Integer> jmxPort;
	private JMXConnector connector;
	private SpringApplicationLifecycleClient client;

	public SpringApplicationLifeCycleClientManager(Provider<Integer> jmxPort) {
		this.jmxPort = jmxPort;
	}

	/**
	 * Convenenience method, use ILaunch as the jmxPort provider.
	 */
	public SpringApplicationLifeCycleClientManager(ILaunch l) {
		this(() -> BootLaunchConfigurationDelegate.getJMXPortAsInt(l));
	}


	/**
	 * Convenenience method, use a given fixed port.
	 */
	public SpringApplicationLifeCycleClientManager(int resolvedPort) {
		this(fixedPort(resolvedPort));
	}

	private static Provider<Integer> fixedPort(int resolvedPort) {
		Assert.isLegal(resolvedPort>0, "JMX port must be > 0");
		return () -> resolvedPort;
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
				Integer resolvedPort = jmxPort.get();
				if (resolvedPort==null || resolvedPort <=0) {
					throw new IllegalStateException("JMX port not specified");
				}
				connector = SpringApplicationLifecycleClient.createLocalJmxConnector(resolvedPort);
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
