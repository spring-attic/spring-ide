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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.zip.ZipFile;

import org.cloudfoundry.client.lib.ApplicationLogListener;
import org.cloudfoundry.client.lib.StreamingLogToken;
import org.cloudfoundry.client.lib.domain.Staging;
import org.cloudfoundry.client.v2.applications.ApplicationResource;
import org.cloudfoundry.client.v2.applications.ListApplicationsRequest;
import org.cloudfoundry.client.v2.applications.ListApplicationsResponse;
import org.cloudfoundry.client.v2.applications.UpdateApplicationRequest;
import org.cloudfoundry.client.v2.domains.DomainResource;
import org.cloudfoundry.client.v2.domains.ListDomainsRequest;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.CloudFoundryOperationsBuilder;
import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.cloudfoundry.operations.applications.RestartApplicationRequest;
import org.cloudfoundry.operations.organizations.OrganizationDetail;
import org.cloudfoundry.operations.organizations.OrganizationInfoRequest;
import org.cloudfoundry.operations.organizations.OrganizationSummary;
import org.cloudfoundry.spring.client.SpringCloudFoundryClient;
import org.cloudfoundry.util.PaginationUtils;
import org.eclipse.core.runtime.Assert;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplicationDetail;
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
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import com.google.common.collect.ImmutableList;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DefaultCloudFoundryClientFactoryV2 extends CloudFoundryClientFactory {

	private static <T> Function<Mono<T>, Mono<T>> debugMono(String msg) {
		return (mono) -> {
			return mono
			.then((value) -> {
				System.out.println(msg+" => "+value);
				return Mono.just(value);
			})
			.otherwise((error) -> {
				System.out.println(msg+" ERROR => "+ ExceptionUtil.getMessage(error));
				return Mono.error(error);
			});
		};
	}

	@Override
	public ClientRequests getClient(CFClientParams params) throws Exception {

		return new ClientRequests() {
			SpringCloudFoundryClient client = SpringCloudFoundryClient.builder()
					.username(params.getUsername())
					.password(params.getPassword())
					.host(params.getHost())
					.build();

			 CloudFoundryOperations operations = new CloudFoundryOperationsBuilder()
				.cloudFoundryClient(client)
				.target(params.getOrgName(), params.getSpaceName())
				.build();

			Mono<String> orgId = getOrgId();

			private Mono<String> getOrgId() {
				String orgName = params.getOrgName();
				if (orgName==null) {
					return Mono.error(new IOException("No organization targetted"));
				} else {
					return operations.organizations().get(OrganizationInfoRequest.builder()
							.name(params.getOrgName())
							.build()
					)
					.map(OrganizationDetail::getId)
					.cache();
				}
			}

			@Override
			public List<CFApplication> getApplicationsWithBasicInfo() throws Exception {
				return operations.applications()
				.list()
				.map(CFWrappingV2::wrap)
				.toList()
				.map(ImmutableList::copyOf)
				.get();
			}

			@Override
			public List<CFService> getServices() throws Exception {
				return operations
				.services()
				.listInstances()
				.map(CFWrappingV2::wrap)
				.toList()
				.map(ImmutableList::copyOf)
				.get();
			}

			@Override
			public List<CFApplicationDetail> waitForApplicationDetails(List<CFApplication> appsToLookUp, long timeToWait) throws Exception {
				return Flux.fromIterable(appsToLookUp)
				.flatMap((CFApplication appSummary) -> {
					return operations.applications().get(GetApplicationRequest.builder()
							.name(appSummary.getName())
							.build()
					)
					.map((ApplicationDetail appDetails) -> CFWrappingV2.wrap(appSummary, appDetails))
					.otherwise((error) -> {

						return Mono.just(CFWrappingV2.wrap(appSummary, null));
					});
				})
				.toList()
				.get(timeToWait);
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
			public void restartApplication(String appName) throws Exception {
				operations.applications().restart(RestartApplicationRequest.builder()
						.name(appName)
						.build())
				.get();

			}

			@Override
			public void logout() {
				operations = null;
				client = null;
			}

			@Override
			public List<CFStack> getStacks() throws Exception {
				return operations.stacks()
				.list()
				.map(CFWrappingV2::wrap)
				.toList()
				.get();
			}

			@Override
			public SshClientSupport getSshClientSupport() throws Exception {
				Assert.isLegal(false, "Not implemented");
				return null;
			}

			private CloudFoundryOperations operatationsFor(OrganizationSummary org) {
				return new CloudFoundryOperationsBuilder()
				.cloudFoundryClient(client)
				.target(org.getName())
				.build();
			}

			@Override
			public List<CFSpace> getSpaces() throws Exception {
				return operations.organizations()
				.list()
				.flatMap((OrganizationSummary org) -> {
					return operatationsFor(org)
					.spaces()
					.list()
					.map((space) -> CFWrappingV2.wrap(org, space));
				})
				.toList()
				.get();
			}

			@Override
			public String getHealthCheck(UUID appGuid) throws Exception {
				//XXX CF V2: getHealthcheck (via operations API)
				// See: https://www.pivotaltracker.com/story/show/116462215
				return client.applicationsV2()
				.get(org.cloudfoundry.client.v2.applications.GetApplicationRequest.builder()
					.applicationId(appGuid.toString())
					.build()
				)
				.map((response) -> response.getEntity().getHealthCheckType())
				.get();
			}

			@Override
			public void setHealthCheck(UUID guid, String hcType) throws Exception {
				//XXX CF V2: setHealthCheck (via operations API)
				// See: https://www.pivotaltracker.com/story/show/116462369
				client.applicationsV2()
				.update(UpdateApplicationRequest.builder()
					.applicationId(guid.toString())
					.healthCheckType(hcType)
					.build()
				)
				.get(); //To force synchronous execution
			}

			@Override
			public List<CFCloudDomain> getDomains() throws Exception {
				//XXX CF V2: list domains using 'operations' api.
				return orgId.flatMap(this::requestDomains)
				.map(CFWrappingV2::wrap)
				.toList()
				.as(debugMono("domains"))
				.get();
			}

			private Flux<DomainResource> requestDomains(String orgId) {
				return PaginationUtils.requestResources((page) ->
					client.domains().list(ListDomainsRequest.builder()
						.page(page)
//						.owningOrganizationId(orgId)
						.build()
					)
				);
			}

			@Override
			public List<CFBuildpack> getBuildpacks() throws Exception {
				//XXX CF V2 getBuildpacks (not yet implemented in V2 client or operations)
				return ImmutableList.of(
						CFWrappingV2.buildpack("java_buildpack"),
						CFWrappingV2.buildpack("staticfile_buildpack"),
						CFWrappingV2.buildpack("ruby_buildpack")
				);
			}

			@Override
			public CFApplicationDetail getApplication(String appName) throws Exception {
				return operations.applications().get(GetApplicationRequest.builder()
					.name(appName)
					.build()
				)
				.map(CFWrappingV2::wrap)
				.get();
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

			@Override
			public boolean applicationExists(String appName) {
				return client.applicationsV2().list(ListApplicationsRequest.builder()
					.name(appName)
					.build()
				)
				.map((ListApplicationsResponse response) -> {
					List<ApplicationResource> resource = response.getResources();
					return resource != null && !resource.isEmpty();
				})
				.get();
			}
		};
	}


}
