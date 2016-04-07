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
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.zip.ZipFile;

import org.cloudfoundry.client.lib.ApplicationLogListener;
import org.cloudfoundry.client.lib.StreamingLogToken;
import org.cloudfoundry.client.lib.domain.Staging;
import org.cloudfoundry.client.v2.applications.ApplicationEntity;
import org.cloudfoundry.client.v2.applications.ApplicationResource;
import org.cloudfoundry.client.v2.applications.ListApplicationsRequest;
import org.cloudfoundry.client.v2.applications.ListApplicationsResponse;
import org.cloudfoundry.client.v2.applications.UpdateApplicationRequest;
import org.cloudfoundry.client.v2.buildpacks.ListBuildpacksRequest;
import org.cloudfoundry.client.v2.domains.DomainResource;
import org.cloudfoundry.client.v2.domains.ListDomainsRequest;
import org.cloudfoundry.client.v2.serviceinstances.DeleteServiceInstanceRequest;
import org.cloudfoundry.client.v2.serviceinstances.ListServiceInstancesRequest;
import org.cloudfoundry.client.v2.serviceinstances.ListServiceInstancesResponse;
import org.cloudfoundry.client.v2.serviceinstances.ServiceInstanceResource;
import org.cloudfoundry.client.v2.services.GetServiceRequest;
import org.cloudfoundry.client.v2.stacks.GetStackRequest;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.CloudFoundryOperationsBuilder;
import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.cloudfoundry.operations.applications.ApplicationSummary;
import org.cloudfoundry.operations.applications.DeleteApplicationRequest;
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
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Version;
import org.reactivestreams.Publisher;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationRunningStateTracker;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplicationDetail;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFService;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFSpace;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFStack;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v1.DefaultClientRequestsV1;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.SshClientSupport;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SchedulerGroup;

/**
 * @author Kris De Volder
 * @author Nieraj Singh
 */
public class DefaultClientRequestsV2 implements ClientRequests {

	private static final Duration APP_START_TIMEOUT = Duration.ofMillis(ApplicationRunningStateTracker.APP_START_TIMEOUT);

	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");

	private static final Callable<? extends Consumer<Runnable>> SCHEDULER_GROUP = SchedulerGroup.async();

// TODO: it would be good not to create another 'threadpool' and use something like the below code
//  instead so that eclipse job scheduler is used for reactor 'tasks'. However... the code below
//  may not be 100% correct.
//	private static final Callable<? extends Consumer<Runnable>> SCHEDULER_GROUP = () -> {
//		return (Runnable task) -> {
//			Job job = new Job("CF Client background task") {
//				@Override
//				protected IStatus run(IProgressMonitor monitor) {
//					if (task!=null) {
//						task.run();
//					}
//					return Status.OK_STATUS;
//				}
//			};
//			job.setRule(JobUtil.lightRule("reactor-job-rule"));
//			job.setSystem(true);
//			job.schedule();
//		};
//	};

	private static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}

	private CFClientParams params;
	private SpringCloudFoundryClient client ;
	private CloudFoundryOperations operations;
	private Mono<String> orgId;

	@Deprecated
	private DefaultClientRequestsV1 v1;

	public DefaultClientRequestsV2(CFClientParams params) throws Exception {
		this.params = params;
		this.v1 = new DefaultClientRequestsV1(params);
		this.client = SpringCloudFoundryClient.builder()
				.username(params.getUsername())
				.password(params.getPassword())
				.host(params.getHost())
				.build();
		this.operations = new CloudFoundryOperationsBuilder()
			.cloudFoundryClient(client)
			.target(params.getOrgName(), params.getSpaceName())
			.build();
		this.orgId = getOrgId();
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
		return ReactorUtils.get(operations.applications()
		.list()
		.map((appSummary) ->
			CFWrappingV2.wrap(appSummary, getApplicationExtras(appSummary.getName()))
		)
		.toList()
		.map(ImmutableList::copyOf)
		);
	}

	private ApplicationExtras getApplicationExtras(String appName) {
		//Stuff used in computing the 'extras'...
		Mono<String> appIdMono = getApplicationId(appName);
		Mono<ApplicationEntity> entity = appIdMono
			.then((appId) ->
				client.applicationsV2().get(org.cloudfoundry.client.v2.applications.GetApplicationRequest.builder()
					.applicationId(appId)
					.build()
				)
			)
			.map((appResource) -> appResource.getEntity())
			.cache();

		//The stuff returned from the getters of 'extras'...
		Mono<List<String>> services = prefetch(getBoundServicesList(appName));
		Mono<Map<String, String>> env = prefetch(DefaultClientRequestsV2.this.getEnv(appName));
		Mono<String> buildpack = prefetch(entity.map(ApplicationEntity::getBuildpack));
		Mono<String> stack = prefetch(
			entity.map(ApplicationEntity::getStackId)
			.then((stackId) -> {
				return client.stacks().get(GetStackRequest.builder()
						.stackId(stackId)
						.build()
				);
			}).map((response) -> {
				return response.getEntity().getName();
			})
		);
		Mono<Integer> timeout = prefetch(
				entity
				.map(ApplicationEntity::getHealthCheckTimeout)
		);

		return new ApplicationExtras() {
			@Override
			public Mono<List<String>> getServices() {
				return services;
			}

			@Override
			public Mono<Map<String, String>> getEnv() {
				return env;
			}

			@Override
			public Mono<String> getBuildpack() {
				return buildpack;
			}

			@Override
			public Mono<String> getStack() {
				return stack;
			}

			public Mono<Integer> getTimeout() {
				return timeout;
			}

			@Override
			public Mono<String> getCommand() {
				Mono<String> command = prefetch(
						entity
						.map(ApplicationEntity::getCommand)
				);
				return command;
			}
		};
	}

	private <T> Mono<T> prefetch(Mono<T> toFetch) {
		return toFetch
		.otherwise((error) -> {
			BootActivator.log(new IOException("Failed prefectch", error));
			return Mono.empty();
		})
		.subscribe();
	}

//	private <T> Mono<T> prefetch(Mono<T> toFetch) {
//		Mono<T> result = toFetch
//		.cache(); // It should only be fetched once.
//
//		//We must ensure the 'result' is being consumed by something to force its execution:
//		result
//		.publishOn(SCHEDULER_GROUP) //Ensure the consume is truly async or it may block here.
//		.consume((dont_care) -> {});
//
//		return result;
//	}

	@Override
	public List<CFService> getServices() throws Exception {
		return ReactorUtils.get(
			operations
			.services()
			.listInstances()
			.map(CFWrappingV2::wrap)
			.toList()
			.map(ImmutableList::copyOf)
		);
	}

	@Override
	public List<CFApplicationDetail> waitForApplicationDetails(List<CFApplication> appsToLookUp, long timeToWait) throws Exception {
		return Flux.fromIterable(appsToLookUp)
		.flatMap((CFApplication appSummary) -> {
			return operations.applications().get(GetApplicationRequest.builder()
					.name(appSummary.getName())
					.build()
			)
			.map((ApplicationDetail appDetails) -> CFWrappingV2.wrap((CFApplicationSummaryData)appSummary, appDetails))
			.otherwise((error) -> {
				return Mono.just(CFWrappingV2.wrap((CFApplicationSummaryData)appSummary, null));
			});
		})
		.toList()
		.get(timeToWait);
	}

	@Override
	public void uploadApplication(String appName, ZipFile archive) throws Exception {
		v1.uploadApplication(appName, archive);
	}

	@Override
	public void updateApplicationUris(String appName, List<String> urls) throws Exception {
		v1.updateApplicationUris(appName, urls);
	}

	@Override
	public void updateApplicationStaging(String appName, Staging staging) throws Exception {
		v1.updateApplicationStaging(appName, staging);
	}

	@Override
	public void updateApplicationServices(String appName, List<String> services) throws Exception {
		v1.updateApplicationServices(appName, services);
	}

	@Override
	public void updateApplicationMemory(String appName, int memory) throws Exception {
		v1.updateApplicationMemory(appName, memory);
	}

	@Override
	public void updateApplicationInstances(String appName, int instances) throws Exception {
		v1.updateApplicationInstances(appName, instances);
	}

	@Override
	public void updateApplicationEnvironment(String appName, Map<String, String> environment) throws Exception {
		ReactorUtils.get(
			setEnvVars(appName, environment)
		);
	}

	@Override
	public void updateApplicationDiskQuota(String appName, int diskQuota) throws Exception {
		v1.updateApplicationDiskQuota(appName, diskQuota);
	}

	@Override
	public Mono<StreamingLogToken> streamLogs(String appName, ApplicationLogListener logConsole) throws Exception{
		Mono<StreamingLogToken> result = Mono.defer(() -> {
			return Mono.just(v1.streamLogs(appName, logConsole));
		})
		.retry()
		.cache();
		result.publishOn(SCHEDULER_GROUP).consume((token) -> {});

		return result;

//		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
//		try {
//
//			// TODO: Retain this from old code. Not sure what bug it addresses
//			Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
//
//		} catch (Exception e) {
//			BootDashActivator.log(e);
//		} finally {
//			Thread.currentThread().setContextClassLoader(contextClassLoader);
//		}
//		return null;
	}

	@Override
	public void stopApplication(String appName) throws Exception {
		v1.stopApplication(appName);
	}

	@Override
	public void restartApplication(String appName) throws Exception {
		ReactorUtils.get(APP_START_TIMEOUT,
			operations.applications().restart(RestartApplicationRequest.builder()
					.name(appName)
					.build())
		);
	}

	@Override
	public void logout() {
		operations = null;
		client = null;
		if (v1!=null) {
			v1.logout();
			v1 = null;
		}
	}

	@Override
	public List<CFStack> getStacks() throws Exception {
		return ReactorUtils.get(
			operations.stacks()
			.list()
			.map(CFWrappingV2::wrap)
			.toList()
		);
	}

	@Override
	public SshClientSupport getSshClientSupport() throws Exception {
		return v1.getSshClientSupport();
	}

	private CloudFoundryOperations operatationsFor(OrganizationSummary org) {
		return new CloudFoundryOperationsBuilder()
				.cloudFoundryClient(client)
				.target(org.getName())
				.build();
	}

	@Override
	public List<CFSpace> getSpaces() throws Exception {
		return ReactorUtils.get(
			operations.organizations()
			.list()
			.flatMap((OrganizationSummary org) -> {
				return operatationsFor(org)
						.spaces()
						.list()
						.map((space) -> CFWrappingV2.wrap(org, space));
			})
			.toList()
		);
	}

	@Override
	public String getHealthCheck(UUID appGuid) throws Exception {
		//XXX CF V2: getHealthcheck (via operations API)
		// See: https://www.pivotaltracker.com/story/show/116462215
		return ReactorUtils.get(
			client.applicationsV2()
			.get(org.cloudfoundry.client.v2.applications.GetApplicationRequest.builder()
					.applicationId(appGuid.toString())
					.build()
					)
			.map((response) -> response.getEntity().getHealthCheckType())
		);
	}

	@Override
	public void setHealthCheck(UUID guid, String hcType) throws Exception {
		//XXX CF V2: setHealthCheck (via operations API)
		// See: https://www.pivotaltracker.com/story/show/116462369
		ReactorUtils.get(
			client.applicationsV2()
			.update(UpdateApplicationRequest.builder()
					.applicationId(guid.toString())
					.healthCheckType(hcType)
					.build()
			)
		);
	}

	@Override
	public List<CFCloudDomain> getDomains() throws Exception {
		//XXX CF V2: list domains using 'operations' api.
		return ReactorUtils.get(
			orgId.flatMap(this::requestDomains)
			.map(CFWrappingV2::wrap)
			.toList()
		);
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
		//XXX CF V2: getBuilpacks using 'operations' API.
		return PaginationUtils.requestResources((page) -> {
			return client.buildpacks()
			.list(ListBuildpacksRequest.builder()
				.page(page)
				.build()
			);
		})
		.map(CFWrappingV2::wrap)
		.toList()
		.get();
	}

	@Override
	public CFApplicationDetail getApplication(String appName) throws Exception {
		return ReactorUtils.get(
				getApplicationMono(appName)
				//		.log("getApplication("+appName+")")
		);
	}

	private Mono<CFApplicationDetail> getApplicationMono(String appName) {
		return operations.applications().get(GetApplicationRequest.builder()
				.name(appName)
				.build()
		)
		.map((appDetail) -> {
			return CFWrappingV2.wrap(appDetail, getApplicationExtras(appName));
		})
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
			.map((appSummary) -> CFWrappingV2.wrap(appSummary, getApplicationExtras(appName)))
			.map((CFApplication summary) -> CFWrappingV2.wrap((CFApplicationSummaryData)summary, (ApplicationDetail)null));
		});
	}

	@Override
	public Version getApiVersion() {
		return v1.getApiVersion();
	}

	@Override
	public void deleteApplication(String appName) throws Exception {
		ReactorUtils.get(operations.applications().delete(DeleteApplicationRequest
				.builder()
				.name(appName)
				.build()
		));
	}

	@Override
	public boolean applicationExists(String appName) throws Exception {
		return ReactorUtils.get(
			client.applicationsV2().list(ListApplicationsRequest.builder()
			.name(appName)
			.build()
			)
			.map((ListApplicationsResponse response) -> {
				List<ApplicationResource> resource = response.getResources();
				return resource != null && !resource.isEmpty();
			})
		);
	}

	@Override
	public void push(CFPushArguments params) throws Exception {
		debug("Pushing app starting: "+params.getAppName());
		//XXX CF V2: push should use 'manifest' in a future version of V2
		ReactorUtils.get(APP_START_TIMEOUT,
			operations.applications()
			.push(CFWrappingV2.toPushRequest(params)
					.noStart(true)
					.build()
			)
			.after(() -> setEnvVars(params.getAppName(), params.getEnv()))
			.after(() -> bindAndUnbindServices(params.getAppName(), params.getServices()))
			.after(() -> {
				if (!params.isNoStart()) {
					return startApp(params.getAppName());
				} else {
					return Mono.empty();
				}
			})
	//		.log("pushing")
		);
		debug("Pushing app succeeded: "+params.getAppName());
	}

	public Mono<Void> bindAndUnbindServices(String appName, List<String> _services) {
		debug("bindAndUnbindServices "+_services);
		Set<String> services = ImmutableSet.copyOf(_services);
		try {
			return getBoundServicesSet(appName)
			.flatMap((boundServices) -> {
				debug("boundServices = "+boundServices);
				Set<String> toUnbind = Sets.difference(boundServices, services);
				Set<String> toBind = Sets.difference(services, boundServices);
				debug("toBind = "+toBind);
				debug("toUnbind = "+toUnbind);
				return Flux.merge(
						bindServices(appName, toBind),
						unbindServices(appName, toUnbind)
				);
			})
			.after();
		} catch (Exception e) {
			return Mono.error(e);
		}
	}

	public Flux<String> getBoundServices(String appName) {
		return operations.services()
		.listInstances()
		.filter((service) -> isBoundTo(service, appName))
		.map(ServiceInstance::getName);
	}

	public Mono<Set<String>> getBoundServicesSet(String appName) {
		return getBoundServices(appName)
		.toList()
		.map(ImmutableSet::copyOf);
	}

	public Mono<List<String>> getBoundServicesList(String appName) {
		return getBoundServices(appName)
		.toList()
		.map(ImmutableList::copyOf);
	}

	private boolean isBoundTo(ServiceInstance service, String appName) {
		return service.getApplications().stream()
				.anyMatch((boundAppName) -> boundAppName.equals(appName));
	}

	private Flux<Void> bindServices(String appName, Set<String> services) {
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
		.map((app) -> app.getGuid().toString())
		.cache();
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
		return getServiceId(serviceName)
		.then((serviceId) -> {
			 return client.serviceInstances().delete(DeleteServiceInstanceRequest.builder()
			.serviceInstanceId(serviceId)
			.build());
		})
		.after();
	}

	protected Mono<String> getServiceId(String serviceName) {
		return client.serviceInstances().list(ListServiceInstancesRequest.builder()
				.name(serviceName)
				.build()
		).then((ListServiceInstancesResponse response) -> {
			List<ServiceInstanceResource> resources = response.getResources();
			if (resources.isEmpty()) {
				return Mono.error(new IOException("Service instance not found: "+serviceName));
			} else {
				return Mono.just(resources.get(0).getMetadata().getId());
			}
		});
	}

	public Mono<Map<String,String>> getEnv(String appName) {
		return operations.applications().getEnvironments(GetApplicationEnvironmentsRequest.builder()
				.name(appName)
				.build()
		)
		.map((envs) -> envs.getUserProvided())
		.map(this::dropObjectsFromMap);
	}

	@Override
	public Map<String, String> getApplicationEnvironment(String appName) {
		return getEnv(appName).get();
	}

	private Map<String, String> dropObjectsFromMap(Map<String, Object> map) {
		Builder<String, String> builder = ImmutableMap.builder();
		for (Entry<String, Object> entry : map.entrySet()) {
			try {
				builder.put(entry.getKey(), (String) entry.getValue());
			} catch (ClassCastException e) {
				BootActivator.log(e);
			}
		}
		return builder.build();
	}
}
