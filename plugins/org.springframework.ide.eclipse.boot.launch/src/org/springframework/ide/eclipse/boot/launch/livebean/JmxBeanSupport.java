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
package org.springframework.ide.eclipse.boot.launch.livebean;

import java.util.EnumSet;

import org.springframework.ide.eclipse.boot.launch.BootLaunchConfigurationDelegate;

/**
 * Helper methods to generate vm args for launch config with LiveBean support enabled.
 *
 * @author Kris De Volder
 */
public class JmxBeanSupport {

	public static enum Feature {
		LIVE_BEAN_GRAPH("-Dspring.liveBeansView.mbeanDomain"),
		LIFE_CYCLE("-Dspring.application.admin.enabled=true");

		public final String vmArg;

		Feature(String vmArg) {
			this.vmArg = vmArg;
		}
	}

	/**
	 * VM args that enable 'live bean graph' and jmx.
	 */
	public static String jmxBeanVmArgs(int jmxPort, EnumSet<Feature> enabled) {
		return jmxBeanVmArgs(""+jmxPort, enabled);
	}

	public static String jmxBeanVmArgs(String jmxPort, EnumSet<Feature> enabled) {
		if (!enabled.isEmpty()) {
			//At least one feature enabled
			StringBuilder str = new StringBuilder();
			for (String a : enableJmxArgs(jmxPort)) {
				str.append(a+"\n");
			}
			for (Feature feature : enabled) {
				str.append(feature.vmArg+"\n");
			}
			return str.toString();
		} else {
			//No features enabled
			return "";
		}
	}

	public static String[] enableJmxArgs(String jmxPort) {
		return new String[] {
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
