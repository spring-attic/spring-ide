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
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.CFRouteBuilder;

public class CFRouteTests {

	public static final List<String> SPRING_CLOUD_DOMAINS = Arrays.<String>asList("springsource.org", "spring.io",
			"myowndomain.spring.io", "tcp.spring.io", "spring.framework");

	@Test
	public void test_domain_no_host() throws Exception {
		CFRoute route = CFRoute.builder().from("spring.io", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("spring.io", route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
	}

	@Test
	public void test_domain_host() throws Exception {
		CFRoute route = CFRoute.builder().from("myapp.spring.io", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("spring.io", route.getDomain());
		Assert.assertEquals("myapp", route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
	}

	@Test
	public void test_domain_only() throws Exception {
		CFRoute route = CFRoute.builder().from("spring.io", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("spring.io", route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
	}

	@Test
	public void test_longer_domain_match() throws Exception {
		CFRoute route = CFRoute.builder().from("myowndomain.spring.io", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("myowndomain.spring.io", route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
	}

	@Test
	public void test_longer_domain_nonexisting() throws Exception {
		CFRoute route = CFRoute.builder().from("app.doesnotexist.io", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("doesnotexist.io", route.getDomain());
		Assert.assertEquals("app",route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
	}

	@Test
	public void test_longer_domain_nonexisting_path() throws Exception {
		CFRoute route = CFRoute.builder().from("app.doesnotexist.io/withpath", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("doesnotexist.io", route.getDomain());
		Assert.assertEquals("app",route.getHost());
		Assert.assertEquals("/withpath",route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
	}

	@Test
	public void test_longer_domain_nonexisting_path_port() throws Exception {
		CFRoute route = CFRoute.builder().from("app.doesnotexist.io:60100/withpath", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("doesnotexist.io", route.getDomain());
		Assert.assertEquals("app",route.getHost());
		Assert.assertEquals("/withpath",route.getPath());
		Assert.assertEquals(60100, route.getPort());
	}

	@Test
	public void test_longer_domain_match_2() throws Exception {
		CFRoute route = CFRoute.builder().from("myapp.myowndomain.spring.io", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("myowndomain.spring.io", route.getDomain());
		Assert.assertEquals("myapp", route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
	}

	@Test
	public void test_domain_host_path() throws Exception {
		CFRoute route = CFRoute.builder().from("myapp.spring.io/appPath", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("spring.io", route.getDomain());
		Assert.assertEquals("myapp", route.getHost());
		Assert.assertEquals("/appPath", route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
	}

	@Test
	public void test_domain_host_path_2() throws Exception {
		CFRoute route = CFRoute.builder().from("myapp.spring.io/appPath/additionalSegment", SPRING_CLOUD_DOMAINS)
				.build();
		Assert.assertEquals("spring.io", route.getDomain());
		Assert.assertEquals("myapp", route.getHost());
		Assert.assertEquals("/appPath/additionalSegment", route.getPath());
		Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
	}

	@Test
	public void test_tcp_port() throws Exception {
		CFRoute route = CFRoute.builder().from("tcp.spring.io:9000", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("tcp.spring.io", route.getDomain());
		Assert.assertNull(route.getHost());
		Assert.assertNull(route.getPath());
		Assert.assertEquals(9000, route.getPort());
	}

	@Test
	public void parse_null_domain() throws Exception {
		String domain = CFRouteBuilder.findDomain("", SPRING_CLOUD_DOMAINS);
		Assert.assertNull(domain);

		domain = CFRouteBuilder.findDomain(null, SPRING_CLOUD_DOMAINS);
		Assert.assertNull(domain);

		domain = CFRouteBuilder.findDomain(".", SPRING_CLOUD_DOMAINS);
		Assert.assertNull(domain);

		domain = CFRouteBuilder.findDomain(".cfapps", SPRING_CLOUD_DOMAINS);
		Assert.assertNull(domain);

		domain = CFRouteBuilder.findDomain("cfapps.", SPRING_CLOUD_DOMAINS);
		Assert.assertNull(domain);

		domain = CFRouteBuilder.findDomain("...", SPRING_CLOUD_DOMAINS);
		Assert.assertNull(domain);

		domain = CFRouteBuilder.findDomain("..cfapps..", SPRING_CLOUD_DOMAINS);
		Assert.assertNull(domain);

		domain = CFRouteBuilder.findDomain(".cfapps..", SPRING_CLOUD_DOMAINS);
		Assert.assertNull(domain);

		domain = CFRouteBuilder.findDomain("..cfapps.", SPRING_CLOUD_DOMAINS);
		Assert.assertNull(domain);
	}

	@Test
	public void parse_valid_domain() throws Exception {

		// These exist
		String domain = CFRouteBuilder.findDomain("spring.io", SPRING_CLOUD_DOMAINS);
		Assert.assertEquals("spring.io", domain);

		domain = CFRouteBuilder.findDomain(".spring.io", SPRING_CLOUD_DOMAINS);
		Assert.assertEquals("spring.io", domain);

		domain = CFRouteBuilder.findDomain("..spring.io", SPRING_CLOUD_DOMAINS);
		Assert.assertEquals("spring.io", domain);

		domain = CFRouteBuilder.findDomain("myapp.spring.io", SPRING_CLOUD_DOMAINS);
		Assert.assertEquals("spring.io", domain);

		domain = CFRouteBuilder.findDomain("myowndomain.spring.io", SPRING_CLOUD_DOMAINS);
		Assert.assertEquals("myowndomain.spring.io", domain);

		domain = CFRouteBuilder.findDomain("myapp.myowndomain.spring.io", SPRING_CLOUD_DOMAINS);
		Assert.assertEquals("myowndomain.spring.io", domain);
	}

	@Test
	public void parse_invalid_domain() throws Exception {

		// These variations of existing domains don't exist
		String domain = CFRouteBuilder.findDomain("spring.io.", SPRING_CLOUD_DOMAINS);
		Assert.assertEquals(null, domain);

		domain = CFRouteBuilder.findDomain("spring.cfapps.io", SPRING_CLOUD_DOMAINS);
		Assert.assertEquals(null, domain);

		domain = CFRouteBuilder.findDomain("spring.io.cfapps", SPRING_CLOUD_DOMAINS);
		Assert.assertEquals(null, domain);
	}

	@Test
	public void bug_142279275_parse_hostAndPathSameName() throws Exception {

		// Fixes Pivotal Tracker item 142279275
		CFRoute route = CFRoute.builder().from("hello-user.myowndomain.spring.io/hello", SPRING_CLOUD_DOMAINS).build();
		Assert.assertEquals("hello-user", route.getHost());
		Assert.assertEquals("myowndomain.spring.io", route.getDomain());
		Assert.assertEquals("/hello", route.getPath());
        Assert.assertEquals(CFRoute.NO_PORT, route.getPort());
        Assert.assertEquals("hello-user.myowndomain.spring.io/hello", route.getRoute());
	}

	@Test
	public void build_route_value_empty() throws Exception {

		String val = CFRouteBuilder.buildRouteVal(null, null, null, CFRoute.NO_PORT);
		Assert.assertEquals("", val);

		val = CFRouteBuilder.buildRouteVal("", "", "", CFRoute.NO_PORT);
		Assert.assertEquals("", val);

	}

	@Test
	public void build_route_value() throws Exception {

		String val = CFRouteBuilder.buildRouteVal("appHost", null, null, CFRoute.NO_PORT);
		Assert.assertEquals("appHost", val);

		val = CFRouteBuilder.buildRouteVal(null, "cfapps.io", "", CFRoute.NO_PORT);
		Assert.assertEquals("cfapps.io", val);

		val = CFRouteBuilder.buildRouteVal("appHost", "cfapps.io", "", CFRoute.NO_PORT);
		Assert.assertEquals("appHost.cfapps.io", val);

		val = CFRouteBuilder.buildRouteVal(null, null, "/path/to/app", CFRoute.NO_PORT);
		Assert.assertEquals("/path/to/app", val);

		val = CFRouteBuilder.buildRouteVal(null, null, "/path/to/app", CFRoute.NO_PORT);
		Assert.assertEquals("/path/to/app", val);

		val = CFRouteBuilder.buildRouteVal(null, null, null, 60101);
		Assert.assertEquals(":60101", val);

		val = CFRouteBuilder.buildRouteVal("appHost", "cfapps.io", "/path/to/app", CFRoute.NO_PORT);
		Assert.assertEquals("appHost.cfapps.io/path/to/app", val);

		val = CFRouteBuilder.buildRouteVal("appHost", "cfapps.io", "/path/to/app", 60101);
		Assert.assertEquals("appHost.cfapps.io:60101/path/to/app", val);

	}

}
