/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.test.yaml;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationManifestHandler;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;

/**
 * Tests for parsing YAML deployment manifest into
 * {@link CloudApplicationDeploymentProperties}
 * 
 * @author Alex Boyko
 *
 */
public class Yaml2DeploymentProperties {
	
	private static final List<CloudDomain> SPRING_CLOUD_DOMAINS = Arrays.asList(
			new CloudDomain(null, "springsource.org", null), new CloudDomain(null, "spring.io", null),
			new CloudDomain(null, "spring.framework", null));
	
	private static CloudApplicationDeploymentProperties readDeploymentProperties(final String filePath) throws Exception {
		ApplicationManifestHandler handler = new ApplicationManifestHandler(null, SPRING_CLOUD_DOMAINS) {
			@Override
			protected InputStream getInputStream() throws Exception {
				return new FileInputStream(ManifestCompareMergeTests.getTestFile(filePath));
			}
		};
		return handler.load(new NullProgressMonitor()).get(0);
	}
	
	@Test
	public void test_no_route_1() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/no-route-1.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>();
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_no_route_2() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/no-route-2.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>();
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_no_route_3() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/no-route-3.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>();
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_no_route_4() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/no-route-4.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>();
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_no_hostname_1() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/no-hostname-1.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Collections.singletonList("my-app.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_no_hostname_2() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/no-hostname-2.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Collections.singletonList("my-app.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_no_hostname_3() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/no-hostname-3.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Collections.singletonList("my-app.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_no_hostname_4() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/no-hostname-4.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Collections.singletonList("springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_no_hostname_5() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/no-hostname-5.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Collections.singletonList("springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_no_hostname_6() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/no-hostname-6.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Arrays.asList("springsource.org", "spring.framework"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_random_route_1() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/random-route-1.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Collections.singletonList("my-app.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_random_route_2() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/random-route-2.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Collections.singletonList("my-app.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_random_route_3() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/random-route-3.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		assertEquals(1, uris.size());
		String uri = uris.iterator().next();
		String host = uri.substring(0, uri.indexOf('.'));
		HashSet<String> expected = new HashSet<>(Collections.singletonList(host + ".springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_random_route_4() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/random-route-4.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		assertEquals(1, uris.size());
		String uri = uris.iterator().next();
		String host = uri.substring(0, uri.indexOf('.'));
		HashSet<String> expected = new HashSet<>(Collections.singletonList(host + ".springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_random_route_5() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/random-route-5.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		assertEquals(1, uris.size());
		String uri = uris.iterator().next();
		String host = uri.substring(0, uri.indexOf('.'));
		HashSet<String> expected = new HashSet<>(Collections.singletonList(host + ".springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_domains_1() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/domains-1.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Collections.singletonList("my-app.spring.io"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_domains_2() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/domains-2.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Collections.singletonList("my-app.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_domains_3() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/domains-3.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Collections.singletonList("my-app.spring.framework"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_domains_4() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/domains-4.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Collections.singletonList("my-app.spring.io"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_domains_5() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/domains-5.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Arrays.asList("my-app.spring.framework", "my-app.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_domains_6() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/domains-6.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Collections.singletonList("my-app.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_domains_7() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/domains-7.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Arrays.asList("my-app.springsource.org", "my-app.spring.framework", "my-app.spring.io"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_domains_8() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/domains-8.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Arrays.asList("my-app.springsource.org", "my-app.spring.framework", "my-app.spring.io"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_domains_9() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/domains-9.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Arrays.asList("my-app.springsource.org", "my-app.spring.framework", "my-app.spring.io"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_hosts_1() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/hosts-1.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Arrays.asList("app.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_hosts_2() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/hosts-2.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Arrays.asList("my-app.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_hosts_3() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/hosts-3.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Arrays.asList("my-app.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_hosts_4() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/hosts-4.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Arrays.asList("my-app.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_hosts_5() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/hosts-5.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Arrays.asList("my-app-1.springsource.org", "my-app-2.springsource.org", "my-app-3.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_hosts_6() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/hosts-6.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Arrays.asList("my-app-1.springsource.org", "my-app-2.springsource.org",
				"my-app-3.springsource.org", "my-root-2.springsource.org", "my-root-3.springsource.org"));
		assertEquals("Uris sets not equal", expected, uris);
	}

	@Test
	public void test_uris_1() throws Exception {
		CloudApplicationDeploymentProperties props = readDeploymentProperties("manifest-parse-data/uris-1.yml");
		HashSet<String> uris = new HashSet<>(props.getUris());
		HashSet<String> expected = new HashSet<>(Arrays.asList("my-app-1.springsource.org", "my-app-2.springsource.org",
				"my-app-3.springsource.org", "my-app-1.spring.io", "my-app-2.spring.io", "my-app-3.spring.io",
				"my-app-1.spring.framework", "my-app-2.spring.framework", "my-app-3.spring.framework"));
		assertEquals("Uris sets not equal", expected, uris);
	}

}
