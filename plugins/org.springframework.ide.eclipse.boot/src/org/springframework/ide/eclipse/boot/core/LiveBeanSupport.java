/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core;

/**
 * @author Kris De Volder
 */
public class LiveBeanSupport {

	//TODO: this stuff doesn't really belong in here. It should
	// be part of BootLaunchConfigurationDelegate's functionality
	// to enable live bean support on a launch, and this should
	// be controlled by 'higher-level' on/off switch in the
	// launch config editor.

	/**
	 * VM args that enable 'live bean graph' and jmx.
	 */
	public static String liveBeanVmArgs(int jmxPort) {
		return liveBeanVmArgs(""+jmxPort);
	}

	public static String liveBeanVmArgs(String jmxPort) {
		return
				"-Dspring.liveBeansView.mbeanDomain\n" + //enable live beans construction
				"-Dcom.sun.management.jmxremote\n" + //enable jmx to access the beans
				"-D"+ JMX_PORT_PROP +"="+jmxPort + "\n" +
				"-Dcom.sun.management.jmxremote.authenticate=false\n" +
				"-Dcom.sun.management.jmxremote.ssl=false\n";
	}

	public static final String JMX_PORT_PROP = "com.sun.management.jmxremote.port";
	public static final String LAUNCH_CONFIG_TYPE_ID = "org.springframework.ide.eclipse.boot.launch";

}
