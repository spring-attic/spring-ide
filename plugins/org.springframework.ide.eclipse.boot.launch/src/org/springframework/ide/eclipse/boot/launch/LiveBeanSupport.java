/*******************************************************************************
 * Copyright (c) 2014-2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.launch;

/**
 * Helper methods to generate vm args for launch config with LiveBean support enabled.
 *
 * @author Kris De Volder
 */
public class LiveBeanSupport {

	/**
	 * VM args that enable 'live bean graph' and jmx.
	 */
	public static String liveBeanVmArgs(int jmxPort) {
		return liveBeanVmArgs(""+jmxPort);
	}

	public static String liveBeanVmArgs(String jmxPort) {
		StringBuilder str = new StringBuilder();
		for (String a : liveBeanVmArgsArray(jmxPort)) {
			str.append(a+"\n");
		}
		return str.toString();
	}

	public static String[] liveBeanVmArgsArray(String jmxPort) {
		return new String[] {
				"-Dspring.liveBeansView.mbeanDomain", //enable live beans construction
				"-Dcom.sun.management.jmxremote", //enable jmx to access the beans
				"-D"+ JMX_PORT_PROP +"="+jmxPort,
				"-Dcom.sun.management.jmxremote.authenticate=false",
				"-Dcom.sun.management.jmxremote.ssl=false"
		};
	}

	public static final String JMX_PORT_PROP = "com.sun.management.jmxremote.port";
	public static final String LAUNCH_CONFIG_TYPE_ID = BootLaunchConfigurationDelegate.LAUNCH_CONFIG_TYPE_ID;
	public static int randomPort() {
		return (int) (5000 + Math.random()*60000);
	}

}
