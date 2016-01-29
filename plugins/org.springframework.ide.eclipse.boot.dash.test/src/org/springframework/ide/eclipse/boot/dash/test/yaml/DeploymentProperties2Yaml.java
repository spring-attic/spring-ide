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

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationManifestHandler;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springsource.ide.eclipse.commons.frameworks.core.util.IOUtil;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.LineBreak;
import org.yaml.snakeyaml.Yaml;

/**
 * Tests for generating YAML files from {@link CloudApplicationDeploymentProperties}
 * 
 * @author Alex Boyko
 *
 */
public class DeploymentProperties2Yaml {
	
	private static final List<CloudDomain> SPRING_CLOUD_DOMAINS = Arrays.asList(
			new CloudDomain(null, "springsource.org", null), new CloudDomain(null, "spring.io", null),
			new CloudDomain(null, "spring.framework", null));

	private static void testDeploymentProperties(CloudApplicationDeploymentProperties props, String expectedYamlFilePath) throws Exception {
		Map<Object, Object> map = ApplicationManifestHandler.toYaml(props, SPRING_CLOUD_DOMAINS);
		
		DumperOptions options = new DumperOptions();
		options.setExplicitStart(true);
		options.setCanonical(false);
		options.setPrettyFlow(true);
		options.setDefaultFlowStyle(FlowStyle.BLOCK);
		options.setLineBreak(LineBreak.getPlatformLineBreak());

		String generatedManifest = new Yaml(options).dump(map);
		
		File yamlFile = ManifestCompareMergeTests.getTestFile(expectedYamlFilePath);
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(yamlFile);
			assertEquals(IOUtil.toString(inputStream).trim(), generatedManifest.trim());
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}	
	}
	
	@Test
	public void test_no_route_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(512);
		props.setUris(null);
		testDeploymentProperties(props, "manifest-generate-data/no-route-1.yml");
	}

	@Test
	public void test_uri_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(512);
		props.setUris(Arrays.asList("app.springsource.org"));
		testDeploymentProperties(props, "manifest-generate-data/uri-1.yml");
	}

	@Test
	public void test_uri_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(512);
		props.setUris(Arrays.asList("app-1.springsource.org", "app-2.springsource.org"));
		testDeploymentProperties(props, "manifest-generate-data/uri-2.yml");
	}

	@Test
	public void test_uri_3() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(512);
		props.setUris(Arrays.asList("app-1.springsource.org"));
		testDeploymentProperties(props, "manifest-generate-data/uri-3.yml");
	}

	@Test
	public void test_uri_4() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(512);
		props.setUris(Arrays.asList("app.springsource.org", "app.spring.io"));
		testDeploymentProperties(props, "manifest-generate-data/uri-4.yml");
	}

	@Test
	public void test_uri_5() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(512);
		props.setUris(Arrays.asList("app-1.springsource.org", "app-1.spring.io"));
		testDeploymentProperties(props, "manifest-generate-data/uri-5.yml");
	}

	@Test
	public void test_uri_6() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(512);
		props.setUris(Arrays.asList("app-1.springsource.org", "app-1.spring.io", "app-2.springsource.org", "app-2.spring.io"));
		testDeploymentProperties(props, "manifest-generate-data/uri-6.yml");
	}

	@Test
	public void test_no_hostname_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(512);
		props.setUris(Arrays.asList("springsource.org"));
		testDeploymentProperties(props, "manifest-generate-data/no-hostname-1.yml");
	}

	@Test
	public void test_no_hostname_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(512);
		props.setUris(Arrays.asList("springsource.org", "spring.io"));
		testDeploymentProperties(props, "manifest-generate-data/no-hostname-2.yml");
	}
}
