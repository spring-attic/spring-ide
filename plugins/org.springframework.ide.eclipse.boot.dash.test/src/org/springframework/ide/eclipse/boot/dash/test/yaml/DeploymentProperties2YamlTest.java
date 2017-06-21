/*******************************************************************************
 * Copyright (c) 2016, 2017 Pivotal, Inc.
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
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.junit.Test;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationManifestHandler;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudData;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.CFPushArguments;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.DeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.routes.RouteAttributes;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.routes.RouteBinding;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.routes.RouteBuilder;
import org.springframework.ide.eclipse.boot.dash.test.yaml.DeploymentProperties2YamlTest.TestDeploymentPropertiesBuilder;
import org.springsource.ide.eclipse.commons.frameworks.core.util.IOUtil;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.LineBreak;
import org.yaml.snakeyaml.Yaml;

import com.google.common.collect.ImmutableList;

/**
 * Tests for generating YAML files from {@link CloudApplicationDeploymentProperties}
 *
 * @author Alex Boyko
 *
 */
public class DeploymentProperties2YamlTest {

	public static class TestDeploymentPropertiesBuilder {

		private CloudData cloudData = ManifestCompareMergeTests.createCloudData();
		private RouteBuilder routeBuilder = new RouteBuilder(cloudData.getDomains());

		private String appName;
		private Integer memory;

		public void setAppName(String appName) {
			this.appName = appName;
		}
		public void setMemory(Integer memory) {
			this.memory = memory;
		}
		public DeploymentProperties build() {
			return new TestDeploymentProperties() {

				@Override
				public CFPushArguments toPushArguments(List<CFCloudDomain> cloudDomains) throws Exception {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public void setArchive(File archive) {
					// TODO Auto-generated method stub

				}

				@Override
				public Set<RouteBinding> getUris() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Integer getTimeout() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getStack() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public List<String> getServices() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public IProject getProject() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public int getMemory() {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public IFile getManifestFile() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public int getInstances() {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public String getHealthCheckType() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getHealthCheckHttpEndpoint() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public Map<String, String> getEnvironmentVariables() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public int getDiskQuota() {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public String getCommand() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getBuildpack() {
					// TODO Auto-generated method stub
					return null;
				}

				@Override
				public String getAppName() {
					// TODO Auto-generated method stub
					return null;
				}
			};
		}
		public void setUris(Object object) {
			props.setUris(routeBuilder.buildRoutes(new RouteAttributes(appName)
					.set;
		}
	}

	private static void doYamlGenerationTest(TestDeploymentPropertiesBuilder props, String expectedYamlFilePath) throws Exception {
		Map<Object, Object> map = ApplicationManifestHandler.toYaml(props.build(), ManifestCompareMergeTests.createCloudData());

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
		TestDeploymentPropertiesBuilder props = new TestDeploymentPropertiesBuilder();
		props.setAppName("app");
		props.setMemory(512);
		props.setUris(null);
		doYamlGenerationTest(props, "manifest-generate-data/no-route-1.yml");
	}

	@Test
	public void test_uri_1() throws Exception {
		TestDeploymentPropertiesBuilder props = new TestDeploymentPropertiesBuilder();
		props.setAppName("app");
		props.setMemory(512);
		props.setUris("app.springsource.org");
		doYamlGenerationTest(props, "manifest-generate-data/uri-1.yml");
	}

	@Test
	public void test_uri_2() throws Exception {
		TestDeploymentPropertiesBuilder props = new TestDeploymentPropertiesBuilder();
		props.setAppName("app");
		props.setMemory(512);
		props.setUris(Arrays.asList("app-1.springsource.org", "app-2.spring.io"));
		doYamlGenerationTest(props, "manifest-generate-data/uri-2.yml");
	}

	@Test
	public void test_uri_3() throws Exception {
		TestDeploymentPropertiesBuilder props = new TestDeploymentPropertiesBuilder();
		props.setAppName("app");
		props.setMemory(512);
		props.setUris(Arrays.asList("app-1.springsource.org"));
		doYamlGenerationTest(props, "manifest-generate-data/uri-3.yml");
	}

	@Test
	public void test_no_hostname_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(512);
		props.setUris(Arrays.asList("springsource.org"));
		doYamlGenerationTest(props, "manifest-generate-data/no-hostname-1.yml");
	}

	@Test
	public void test_no_hostname_2() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(512);
		props.setUris(Arrays.asList("springsource.org", "spring.io"));
		doYamlGenerationTest(props, "manifest-generate-data/no-hostname-2.yml");
	}

	@Test
	public void test_command_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(512);
		props.setCommand("my-command");
		props.setUris(Arrays.asList("app.springsource.org"));
		doYamlGenerationTest(props, "manifest-generate-data/command-1.yml");
	}

	@Test
	public void test_stack_1() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(512);
		props.setStack("my-stack");
		props.setUris(Arrays.asList("app.springsource.org"));
		doYamlGenerationTest(props, "manifest-generate-data/stack-1.yml");
	}


	@Test
	public void test_health_check_http() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(512);
		props.setHealthCheckType("http");
		props.setHealthCheckHttpEndpoint("/testhealth");
		props.setUris(Arrays.asList("app.springsource.org"));

		doYamlGenerationTest(props, "manifest-generate-data/health-check-http.yml");
	}


	@Test
	public void test_health_check_process() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(512);
		props.setHealthCheckType("process");
		props.setUris(Arrays.asList("app.springsource.org"));

		doYamlGenerationTest(props, "manifest-generate-data/health-check-process.yml");
	}

	@Test
	public void test_health_check_port() throws Exception {
		CloudApplicationDeploymentProperties props = new CloudApplicationDeploymentProperties();
		props.setAppName("app");
		props.setMemory(512);
		props.setHealthCheckType("port");
		props.setUris(Arrays.asList("app.springsource.org"));

		doYamlGenerationTest(props, "manifest-generate-data/health-check-port.yml");
	}

}
