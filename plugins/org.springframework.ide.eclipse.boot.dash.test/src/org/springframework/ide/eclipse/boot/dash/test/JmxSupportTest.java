/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.mockito.Mockito.*;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.JmxSupport;
import org.springframework.ide.eclipse.boot.dash.util.JmxSshTunnelManager;

public class JmxSupportTest {

	CloudAppDashElement cde = mock(CloudAppDashElement.class);
	JmxSshTunnelManager tunnels = new JmxSshTunnelManager();

	@Test
	public void setupEnvVars() throws Exception {
		when(cde.getCfJmxPort()).thenReturn(1234);
		JmxSupport jmx = new JmxSupport(cde, tunnels);

		Map<String, String> env = new HashMap<>();
		jmx.setupEnvVars(env);

		assertEquals(
				"-Dcom.sun.management.jmxremote.ssl=false "
				+ "-Dcom.sun.management.jmxremote.authenticate=false "
				+ "-Dcom.sun.management.jmxremote.port=1234 "
				+ "-Dcom.sun.management.jmxremote.rmi.port=1234 "
				+ "-Djava.rmi.server.hostname=localhost "
				+ "-Dcom.sun.management.jmxremote.local.only=false",
				env.get("JAVA_OPTS"));

		jmx.setupEnvVars(env); // should erase old and recreate
		assertEquals(
				"-Dcom.sun.management.jmxremote.ssl=false "
				+ "-Dcom.sun.management.jmxremote.authenticate=false "
				+ "-Dcom.sun.management.jmxremote.port=1234 "
				+ "-Dcom.sun.management.jmxremote.rmi.port=1234 "
				+ "-Djava.rmi.server.hostname=localhost "
				+ "-Dcom.sun.management.jmxremote.local.only=false",
				env.get("JAVA_OPTS"));
	}

	@Test
	public void setupEnvVars_preserve_unrelated_java_opts() throws Exception {
		when(cde.getCfJmxPort()).thenReturn(1234);
		JmxSupport jmx = new JmxSupport(cde, tunnels);

		Map<String, String> env = new HashMap<>();
		env.put("JAVA_OPTS", "-Dsomething.already=here");
		jmx.setupEnvVars(env);

		assertEquals("-Dsomething.already=here "
				+ "-Dcom.sun.management.jmxremote.ssl=false "
				+ "-Dcom.sun.management.jmxremote.authenticate=false "
				+ "-Dcom.sun.management.jmxremote.port=1234 "
				+ "-Dcom.sun.management.jmxremote.rmi.port=1234 "
				+ "-Djava.rmi.server.hostname=localhost "
				+ "-Dcom.sun.management.jmxremote.local.only=false",
				env.get("JAVA_OPTS"));

		jmx.setupEnvVars(env); // should erase old and recreate
		assertEquals("-Dsomething.already=here "
				+ "-Dcom.sun.management.jmxremote.ssl=false "
				+ "-Dcom.sun.management.jmxremote.authenticate=false "
				+ "-Dcom.sun.management.jmxremote.port=1234 "
				+ "-Dcom.sun.management.jmxremote.rmi.port=1234 "
				+ "-Djava.rmi.server.hostname=localhost "
				+ "-Dcom.sun.management.jmxremote.local.only=false",
				env.get("JAVA_OPTS"));

	}

}
