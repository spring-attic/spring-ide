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
package org.springframework.ide.eclipse.boot.dash.test.yaml;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.CFRoute;


public class CFRouteTests {


	public static final List<String> SPRING_CLOUD_DOMAINS = Arrays.<String>asList(
			"springsource.org",
			"spring.io",
			"tcp.spring.io",
			"spring.framework");




	@Test
	public void test_domain_no_host() throws Exception {
		CFRoute route = CFRoute.builder().from("spring.io", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("spring.io",route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
	}

	@Test
	public void test_domain_host() throws Exception {
		CFRoute route = CFRoute.builder().from("myapp.spring.io", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("spring.io",route.getDomain());
		Assert.assertEquals("myapp",route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
	}

	@Test
	public void test_domain_host_path() throws Exception {
		CFRoute route = CFRoute.builder().from("myapp.spring.io/appPath/additionalSegment", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("spring.io",route.getDomain());
		Assert.assertEquals("myapp",route.getHost());
		Assert.assertEquals("appPath/additionalSegment", route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
	}

	@Test
	public void test_tcp_port() throws Exception {
		CFRoute route = CFRoute.builder().from("tcp.spring.io:9000", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("tcp.spring.io",route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(9000, route.getPort());
	}

}

