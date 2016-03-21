package org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipFile;

import org.cloudfoundry.client.lib.ApplicationLogListener;
import org.cloudfoundry.client.lib.StreamingLogToken;
import org.cloudfoundry.client.lib.domain.ApplicationStats;
import org.cloudfoundry.client.lib.domain.Staging;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.CloudFoundryOperationsBuilder;
import org.cloudfoundry.spring.client.SpringCloudFoundryClient;
import org.eclipse.core.runtime.Assert;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplicationArchive;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFService;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFSpace;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFStack;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CloudFoundryClientFactory;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.SshClientSupport;

import com.google.common.collect.ImmutableList;

public class DefaultCloudFoundryClientFactoryV2 extends CloudFoundryClientFactory {

	@Override
	public ClientRequests getClient(CFClientParams params) throws Exception {

		SpringCloudFoundryClient _client = SpringCloudFoundryClient.builder()
				.username(params.getUsername())
				.password(params.getPassword())
				.host(params.getHost())
				.build();

		final CloudFoundryOperations client = new CloudFoundryOperationsBuilder()
			.cloudFoundryClient(_client)
			.target(params.getOrgName(), params.getSpaceName())
			.build();

		return new ClientRequests() {

			@Override
			public List<CFApplication> getApplicationsWithBasicInfo() throws Exception {
				return ImmutableList.copyOf(
					client.applications()
						.list()
						.map(CFWrappingV2::wrap)
						.toIterable()
				);
			}

			@Override
			public Map<CFApplication, ApplicationStats> waitForApplicationStats(List<CFApplication> appsToLookUp,
					long timeToWait) throws Exception {
				Assert.isLegal(false, "Not implemented");
				return null;
			}

			@Override
			public void uploadApplication(String appName, ZipFile archive) throws Exception {
				Assert.isLegal(false, "Not implemented");

			}

			@Override
			public void updateApplicationUris(String appName, List<String> urls) throws Exception {
				Assert.isLegal(false, "Not implemented");

			}

			@Override
			public void updateApplicationStaging(String appName, Staging staging) throws Exception {
				Assert.isLegal(false, "Not implemented");

			}

			@Override
			public void updateApplicationServices(String appName, List<String> services) throws Exception {
				Assert.isLegal(false, "Not implemented");

			}

			@Override
			public void updateApplicationMemory(String appName, int memory) throws Exception {
				Assert.isLegal(false, "Not implemented");

			}

			@Override
			public void updateApplicationInstances(String appName, int instances) throws Exception {
				Assert.isLegal(false, "Not implemented");

			}

			@Override
			public void updateApplicationEnvironment(String appName, Map<String, String> environmentVariables)
					throws Exception {
				Assert.isLegal(false, "Not implemented");

			}

			@Override
			public void updateApplicationDiskQuota(String appName, int diskQuota) throws Exception {
				Assert.isLegal(false, "Not implemented");

			}

			@Override
			public StreamingLogToken streamLogs(String appName, ApplicationLogListener logConsole) {
				Assert.isLegal(false, "Not implemented");
				return null;
			}

			@Override
			public void stopApplication(String appName) throws Exception {
				Assert.isLegal(false, "Not implemented");

			}

			@Override
			public void setHealthCheck(UUID guid, String hcType) throws Exception {
				Assert.isLegal(false, "Not implemented");

			}

			@Override
			public void restartApplication(String appName) throws Exception {
				Assert.isLegal(false, "Not implemented");

			}

			@Override
			public void logout() {
				Assert.isLegal(false, "Not implemented");

			}

			@Override
			public List<CFStack> getStacks() throws Exception {
				Assert.isLegal(false, "Not implemented");
				return null;
			}

			@Override
			public SshClientSupport getSshClientSupport() throws Exception {
				Assert.isLegal(false, "Not implemented");
				return null;
			}

			@Override
			public List<CFSpace> getSpaces() throws Exception {
				Assert.isLegal(false, "Not implemented");
				return null;
			}

			@Override
			public List<CFService> getServices() throws Exception {
				Assert.isLegal(false, "Not implemented");
				return null;
			}

			@Override
			public String getHealthCheck(UUID appGuid) throws Exception {
				Assert.isLegal(false, "Not implemented");
				return null;
			}

			@Override
			public CloudAppInstances getExistingAppInstances(UUID guid) throws Exception {
				Assert.isLegal(false, "Not implemented");
				return null;
			}

			@Override
			public CloudAppInstances getExistingAppInstances(String appName) throws Exception {
				Assert.isLegal(false, "Not implemented");
				return null;
			}

			@Override
			public List<CFCloudDomain> getDomains() throws Exception {
				Assert.isLegal(false, "Not implemented");
				return null;
			}

			@Override
			public List<CFBuildpack> getBuildpacks() throws Exception {
				Assert.isLegal(false, "Not implemented");
				return null;
			}

			@Override
			public CFApplication getApplication(String appName) throws Exception {
				Assert.isLegal(false, "Not implemented");
				return null;
			}

			@Override
			public Version getApiVersion() {
				Assert.isLegal(false, "Not implemented");
				return null;
			}

			@Override
			public void deleteApplication(String name) throws Exception {
				Assert.isLegal(false, "Not implemented");

			}

			@Override
			public void createApplication(CloudApplicationDeploymentProperties deploymentProperties) throws Exception {
				Assert.isLegal(false, "Not implemented");

			}
		};
	}

	private String getHost(String apiUrl) {
		// TODO Auto-generated method stub
		return null;
	}

}
