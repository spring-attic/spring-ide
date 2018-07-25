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
import org.springframework.ide.eclipse.boot.dash.model.RunState;
import org.springframework.ide.eclipse.boot.dash.util.JmxSshTunnelManager;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

public class JmxSupportTest {

	CloudAppDashElement cde = mock(CloudAppDashElement.class);
	JmxSshTunnelManager tunnels = new JmxSshTunnelManager();

	@Test
	public void setupEnvVars() throws Exception {
		int testPort = 1234;
		when(cde.getBaseRunStateExp()).thenReturn(LiveExpression.constant(RunState.INACTIVE));
		JmxSupport jmx = new JmxSupport(cde, tunnels) {
			public int getPort() {return testPort; }
		};

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
		int testPort = 1234;
		when(cde.getBaseRunStateExp()).thenReturn(LiveExpression.constant(RunState.INACTIVE));
		JmxSupport jmx = new JmxSupport(cde, tunnels) {
			public int getPort() {return testPort; }
		};

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
