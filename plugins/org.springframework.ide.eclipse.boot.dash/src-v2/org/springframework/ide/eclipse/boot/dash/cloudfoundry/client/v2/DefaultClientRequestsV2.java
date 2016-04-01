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
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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
import org.cloudfoundry.client.v2.serviceinstances.DeleteServiceInstanceRequest;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.CloudFoundryOperationsBuilder;
import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.cloudfoundry.operations.applications.ApplicationSummary;
import org.cloudfoundry.operations.applications.GetApplicationEnvironmentsRequest;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.cloudfoundry.operations.applications.RestartApplicationRequest;
import org.cloudfoundry.operations.applications.SetEnvironmentVariableApplicationRequest;
import org.cloudfoundry.operations.applications.StartApplicationRequest;
import org.cloudfoundry.operations.organizations.OrganizationDetail;
import org.cloudfoundry.operations.organizations.OrganizationInfoRequest;
import org.cloudfoundry.operations.organizations.OrganizationSummary;
import org.cloudfoundry.operations.services.BindServiceInstanceRequest;
import org.cloudfoundry.operations.services.CreateServiceInstanceRequest;
import org.cloudfoundry.operations.services.GetServiceInstanceRequest;
import org.cloudfoundry.operations.services.ServiceInstance;
import org.cloudfoundry.operations.services.UnbindServiceInstanceRequest;
import org.cloudfoundry.spring.client.SpringCloudFoundryClient;
import org.cloudfoundry.util.PaginationUtils;
import org.eclipse.core.runtime.Assert;
import org.osgi.framework.Version;
import org.reactivestreams.Publisher;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplicationDetail;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFService;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFSpace;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFStack;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.SshClientSupport;

import com.google.common.collect.ImmutableList;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Kris De Volder
 * @author Nieraj Singh
 */
public class DefaultClientRequestsV2 implements ClientRequests {

	private CFClientParams params;
	SpringCloudFoundryClient client ;
	CloudFoundryOperations operations;
	Mono<String> orgId;

	public DefaultClientRequestsV2(CFClientParams params) {
		this.params = params;
		client = SpringCloudFoundryClient.builder()
				.username(params.getUsername())
				.password(params.getPassword())
				.host(params.getHost())
				.build();
		operations = new CloudFoundryOperationsBuilder()
			.cloudFoundryClient(client)
			.target(params.getOrgName(), params.getSpaceName())
			.build();
		orgId = getOrgId();
	}

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
	public void updateApplicationEnvironment(String appName, Map<String, String> environmentVariables) throws Exception {
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
				//				.as(debugMono("domains"))
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
		return getApplicationMono(appName)
//		.log("getApplication("+appName+")")
		.get();
	}

	private Mono<CFApplicationDetail> getApplicationMono(String appName) {
		return operations.applications().get(GetApplicationRequest.builder()
				.name(appName)
				.build()
				)
		.map(CFWrappingV2::wrap)
		.otherwise((error) -> {
			//XXX CF V2: remove workaround for bug in CF V2 operations (no details for stopped app)
			// See https://www.pivotaltracker.com/story/show/116463023
			//Because there's presently a bug preventing us from retrieving details for stopped apps...
			// we shall have to make do with a summary pulled from the list of app summaries instead.
			return operations.applications().list()
			.filter((ApplicationSummary app) -> app.getName().equals(appName))
			.elementAt(0)
			.otherwise((outOfBounds) -> {
				if (outOfBounds instanceof IndexOutOfBoundsException) {
					return Mono.empty();
				}
				return Mono.error(outOfBounds);
			})
			.map(CFWrappingV2::wrap)
			.map((CFApplication summary) -> CFWrappingV2.wrap(summary, null));
		});
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

		// What the old client did:
		//				new BasicRequest(this.client, deploymentProperties.getAppName(), "Creating application") {
		//				@Override
		//				protected void runRequest(CloudFoundryOperations client) throws Exception {
		//					client.createApplication(deploymentProperties.getAppName(),
		//							new Staging(deploymentProperties.getCommand(), deploymentProperties.getBuildpack(),
		//									deploymentProperties.getStack(), deploymentProperties.getTimeout()),
		//							deploymentProperties.getDiskQuota(),
		//							deploymentProperties.getMemory(),
		//							new ArrayList<>(deploymentProperties.getUris()),
		//							deploymentProperties.getServices()
		//					);
		//				}
		//			}.call();

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

	@Override
	public void push(CFPushArguments params) throws Exception {
		//XXX CF V2: push should use 'manifest' in a future version of V2
		operations.applications()
		.push(CFWrappingV2.toPushRequest(params)
				.noStart(true)
				.build()
		)
		.after(() -> setEnvVars(params.getAppName(), params.getEnv()))
		.after(() -> bindAndUnbindServices(params.getAppName(), params.getServices()))
		.after(() ->  startApp(params.getAppName()))
		.log("pushing")
		.get(Duration.ofMinutes(5)); //TODO XXX CF V2: real value for push timeout
	}

	public Mono<Void> bindAndUnbindServices(String appName, List<String> services) {
		try {
			Set<String> toUnbind = getServices().stream()
					.map(CFService::getName)
					.filter((boundService) -> services.contains(boundService))
					.collect(Collectors.toSet());

			return Flux.merge(
					bindServices(appName, services),
					unbindServices(appName, toUnbind)
					)
					.after();
		} catch (Exception e) {
			return Mono.error(e);
		}
	}

	private Flux<Void> bindServices(String appName, List<String> services) {
		return Flux.fromIterable(services)
				.flatMap((service) -> {
					return operations.services().bind(BindServiceInstanceRequest.builder()
							.applicationName(appName)
							.serviceInstanceName(service)
							.build()
							);
				});
	}

	private Flux<Void> unbindServices(String appName, Set<String> toUnbind) {
		return Flux.fromIterable(toUnbind)
		.flatMap((service) -> {
			return operations.services().unbind(UnbindServiceInstanceRequest.builder()
					.applicationName(appName)
					.serviceInstanceName(service)
					.build()
					);
		});
	}

	protected Mono<Void> startApp(String appName) {
		return operations.applications()
		.start(StartApplicationRequest.builder()
			.name(appName)
			.build()
		);
	}

	private Mono<String> getApplicationId(String appName) {
		return getApplicationMono(appName)
		.map((app) -> app.getGuid().toString());
	}

	public Mono<Void> setEnvVars(String appName, Map<String, String> environment) {
		return getApplicationId(appName)
		.then(applicationId -> {
			return client.applicationsV2()
			.update(UpdateApplicationRequest.builder()
					.applicationId(applicationId)
					.environmentJsons(environment)
					.build())
			.after();
		});
	}

	protected Publisher<? extends Object> setEnvVar(String appName, String var, String value) {
		System.out.println("Set var starting: "+var +" = "+value);
		return operations.applications()
				.setEnvironmentVariable(SetEnvironmentVariableApplicationRequest.builder()
						.name(appName)
						.variableName(var)
						.variableValue(value)
						.build()
						)
				.after(() -> {
					System.out.println("Set var complete: "+var +" = "+value);
					return Mono.empty();
				});
	}

	public Mono<Void> createService(String name, String service, String plan) {
		return operations.services().createInstance(CreateServiceInstanceRequest.builder()
				.serviceInstanceName(name)
				.serviceName(service)
				.planName(plan)
				.build()
		);
	}

	public Mono<Void> deleteService(String serviceName) {
		return operations.services().get(GetServiceInstanceRequest.builder()
				.name(serviceName)
				.build())
		.map(ServiceInstance::getId)
		.then((serviceId) -> {
			 return client.serviceInstances().delete(DeleteServiceInstanceRequest.builder()
					.serviceInstanceId(serviceId)
					.build());
		})
		.after();
	}

	public Mono<Map<String,Object>> getEnv(String appName) {
		return operations.applications().getEnvironments(GetApplicationEnvironmentsRequest.builder()
				.name(appName)
				.build()
		).map((envs) -> envs.getUserProvided());
	}
}
