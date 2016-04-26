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

import static org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.ReactorUtils.just;

import java.io.IOException;
import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.lib.ApplicationLogListener;
import org.cloudfoundry.client.lib.StreamingLogToken;
import org.cloudfoundry.client.v2.applications.ApplicationEntity;
import org.cloudfoundry.client.v2.applications.UpdateApplicationRequest;
import org.cloudfoundry.client.v2.buildpacks.ListBuildpacksRequest;
import org.cloudfoundry.client.v2.domains.DomainResource;
import org.cloudfoundry.client.v2.domains.ListDomainsRequest;
import org.cloudfoundry.client.v2.info.GetInfoRequest;
import org.cloudfoundry.client.v2.info.GetInfoResponse;
import org.cloudfoundry.client.v2.serviceinstances.DeleteServiceInstanceRequest;
import org.cloudfoundry.client.v2.stacks.GetStackRequest;
import org.cloudfoundry.client.v2.userprovidedserviceinstances.DeleteUserProvidedServiceInstanceRequest;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.CloudFoundryOperationsBuilder;
import org.cloudfoundry.operations.applications.ApplicationDetail;
import org.cloudfoundry.operations.applications.DeleteApplicationRequest;
import org.cloudfoundry.operations.applications.GetApplicationEnvironmentsRequest;
import org.cloudfoundry.operations.applications.GetApplicationRequest;
import org.cloudfoundry.operations.applications.PushApplicationRequest;
import org.cloudfoundry.operations.applications.PushApplicationRequest.PushApplicationRequestBuilder;
import org.cloudfoundry.operations.applications.RestartApplicationRequest;
import org.cloudfoundry.operations.applications.SetEnvironmentVariableApplicationRequest;
import org.cloudfoundry.operations.applications.StartApplicationRequest;
import org.cloudfoundry.operations.applications.StopApplicationRequest;
import org.cloudfoundry.operations.organizations.OrganizationDetail;
import org.cloudfoundry.operations.organizations.OrganizationInfoRequest;
import org.cloudfoundry.operations.organizations.OrganizationSummary;
import org.cloudfoundry.operations.routes.ListRoutesRequest;
import org.cloudfoundry.operations.routes.ListRoutesRequest.Level;
import org.cloudfoundry.operations.routes.MapRouteRequest;
import org.cloudfoundry.operations.routes.Route;
import org.cloudfoundry.operations.routes.Route.RouteBuilder;
import org.cloudfoundry.operations.routes.UnmapRouteRequest;
import org.cloudfoundry.operations.services.BindServiceInstanceRequest;
import org.cloudfoundry.operations.services.CreateServiceInstanceRequest;
import org.cloudfoundry.operations.services.CreateUserProvidedServiceInstanceRequest;
import org.cloudfoundry.operations.services.GetServiceInstanceRequest;
import org.cloudfoundry.operations.services.ServiceInstance;
import org.cloudfoundry.operations.services.UnbindServiceInstanceRequest;
import org.cloudfoundry.spring.client.SpringCloudFoundryClient;
import org.cloudfoundry.util.PaginationUtils;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Version;
import org.reactivestreams.Publisher;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationRunningStateTracker;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplicationDetail;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFBuildpack;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFClientParams;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFServiceInstance;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFSpace;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFStack;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.ClientRequests;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v1.DefaultClientRequestsV1;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens.CancelationToken;
import org.springframework.ide.eclipse.boot.util.Log;
import org.springframework.ide.eclipse.boot.util.StringUtil;
import org.springframework.util.StringUtils;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.SshClientSupport;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.SshHost;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import reactor.core.publisher.Computations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Kris De Volder
 * @author Nieraj Singh
 */
public class DefaultClientRequestsV2 implements ClientRequests {

	private static final Duration APP_START_TIMEOUT = Duration.ofMillis(ApplicationRunningStateTracker.APP_START_TIMEOUT);
	private static final Duration GET_SERVICES_TIMEOUT = Duration.ofSeconds(60);

	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");


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
	private CloudFoundryClient client ;
	private CloudFoundryOperations operations;
	private Mono<String> orgId;

	private Mono<GetInfoResponse> info;

	@Deprecated
	private DefaultClientRequestsV1 v1;

	public DefaultClientRequestsV2(CloudFoundryClientCache clientFactory, CFClientParams params) throws Exception {
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
		this.info = client.info().get(GetInfoRequest.builder().build()).cache();
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
		Mono<List<String>> services = prefetch("services", getBoundServicesList(appName));
		Mono<Map<String, String>> env = prefetch("env",
				DefaultClientRequestsV2.this.getEnv(appName)
		);
		Mono<String> buildpack = prefetch("buildpack",
				entity.then((e) -> just(e.getBuildpack()))
		);

		Mono<String> stack = prefetch("stack",
			entity.then((e) -> just(e.getStackId()))
			.then((stackId) -> {
				return client.stacks().get(GetStackRequest.builder()
						.stackId(stackId)
						.build()
				);
			}).map((response) -> {
				return response.getEntity().getName();
			})
		);
		Mono<Integer> timeout = prefetch("timeout",
				entity
				.then((v) -> just(v.getHealthCheckTimeout()))
		);

		Mono<String> command = prefetch("command",
				entity.then((e) -> just(e.getCommand()))
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
				return command;
			}
		};
	}

	private <T> Mono<T> prefetch(String id, Mono<T> toFetch) {
		return toFetch
//		.log(id + " before error handler")
		.otherwise((error) -> {
			Log.log(new IOException("Failed prefetch '"+id+"'", error));
			return Mono.empty();
		})
//		.log(id + " after error handler")
		.cache()
//		.log(id + "after cache")
		;
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
	public List<CFServiceInstance> getServices() throws Exception {
		return ReactorUtils.get(GET_SERVICES_TIMEOUT, CancelationTokens.NULL,
			operations
			.services()
			.listInstances()
			.doOnNext(System.out::println)
			.map(CFWrappingV2::wrap)
			.toList()
			.map(ImmutableList::copyOf)
		);
	}

	/**
	 * Get details for a given list of applications. This does a 'best' effort getting the details for
	 * as many apps as possible but it does not guarantee that it will return details for each app in the
	 * list. This is to avoid one 'bad apple' from spoiling the whole batch. (I.e if failing to fetch details for
	 * some apps we can still return details for the others rather than throw an exception).
	 */
	@Override
	public Flux<CFApplicationDetail> getApplicationDetails(List<CFApplication> appsToLookUp) throws Exception {
		return Flux.fromIterable(appsToLookUp)
		.flatMap((CFApplication appSummary) -> {
			return operations.applications().get(GetApplicationRequest.builder()
					.name(appSummary.getName())
					.build()
			)
			.otherwise((error) -> {
				BootDashActivator.log(ExceptionUtil.coreException("getting application details for '"+appSummary.getName()+"' failed", error));
				return Mono.empty();
			})
			.map((ApplicationDetail appDetails) -> CFWrappingV2.wrap((CFApplicationSummaryData)appSummary, appDetails));
		});
	}

	@Override
	public Mono<StreamingLogToken> streamLogs(String appName, ApplicationLogListener logConsole) throws Exception {
		Mono<StreamingLogToken> result = Mono.fromCallable(() -> {
			return v1.streamLogs(appName, logConsole);
		})
//		.log("streamLog before retry")
		.retry(falseAfter(Duration.ofMinutes(1)))
//		.log("streamLog after retry")
		.cache();

		result.subscribeOn(Computations.single()).consume((token) -> {});
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
	}

	private Predicate<Throwable> falseAfter(Duration timeToWait) {
		return new Predicate<Throwable>() {

			private Long firstCalledAt;

			@Override
			public boolean test(Throwable t) {
				if (firstCalledAt==null) {
					firstCalledAt = System.currentTimeMillis();
				}
				long waitedTime = System.currentTimeMillis() - firstCalledAt;
				debug("falseAfter: remaining = "+(timeToWait.toMillis() - waitedTime));
				return waitedTime < timeToWait.toMillis();
			}

		};
	}

	@Override
	public void stopApplication(String appName) throws Exception {
		ReactorUtils.get(
			operations.applications().stop(StopApplicationRequest.builder()
					.name(appName)
					.build()
					)
			);
	}

	@Override
	public void restartApplication(String appName, CancelationToken cancelationToken) throws Exception {
		ReactorUtils.get(APP_START_TIMEOUT, cancelationToken,
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

	public boolean isLoggedOut() {
		return client==null;
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
		return new SshClientSupport() {

			@Override
			public String getSshUser(UUID appGuid, int instance) {
				return "cf:"+appGuid+"/" + instance;
			}

			@Override
			public String getSshUser(String appName, int instance) throws Exception {
				return ReactorUtils.get(
						getApplicationId(appName)
						.map((guid) -> getSshUser(UUID.fromString(guid), instance))
				);
			}

			@Override
			public SshHost getSshHost() throws Exception {
				return ReactorUtils.get(
					info.then((i) -> {
						String fingerPrint = i.getApplicationSshHostKeyFingerprint();
						String host = i.getApplicationSshEndpoint();
						int port = 22; //Default ssh port
						if (host!=null) {
							if (host.contains(":")) {
								String[] pieces = host.split(":");
								host = pieces[0];
								port = Integer.parseInt(pieces[1]);
							}
						}
						if (host!=null) {
							return Mono.just(new SshHost(host, port, fingerPrint));
						}
						return Mono.empty();
					})
				);
			}

			@Override
			public String getSshCode() throws Exception {
				return v1.getSshClientSupport().getSshCode();
//				throw new OperationNotSupportedException("CF V2 client doesn't support SSH access yet");
			}
		};
	}

	private Mono<CloudFoundryOperations> operationsFor(OrganizationSummary org) {
		debug("Creating operations for "+org);
		return Mono.fromCallable(() -> new CloudFoundryOperationsBuilder()
				.cloudFoundryClient(client)
				.target(org.getName())
				.build()
		);
	}

	@Override
	public List<CFSpace> getSpaces() throws Exception {
		return ReactorUtils.get(
			operations.organizations()
			.list()
			.flatMap((OrganizationSummary org) -> {
				return operationsFor(org).flatMap((operations) ->
					operations
					.spaces()
					.list()
					.map((space) -> CFWrappingV2.wrap(org, space))
				);
			})
			.toList()
//			.log("getSpaces")
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
		return ReactorUtils.get(
			PaginationUtils.requestResources((page) -> {
				return client.buildpacks()
				.list(ListBuildpacksRequest.builder()
					.page(page)
					.build()
				);
			})
			.map(CFWrappingV2::wrap)
			.toList()
		);
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
			//TODO: we have 'real' appdetails now so we could get most of the 'application extras' info from that.
			return CFWrappingV2.wrap(appDetail, getApplicationExtras(appName));
		})
		.otherwise(ReactorUtils.suppressException(IllegalArgumentException.class));
	}

	@Override
	public Version getApiVersion() {
		return info
		.map((i) -> new Version(i.getApiVersion()))
		.get();
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
				getApplicationMono(appName)
				.map((app) -> true)
				.otherwiseIfEmpty(Mono.just(false))
		);
	}

	@Override
	public void push(CFPushArguments params, CancelationToken cancelationToken) throws Exception {
		debug("Pushing app starting: "+params.getAppName());
		//XXX CF V2: push should use 'manifest' in a future version of V2
		ReactorUtils.get(APP_START_TIMEOUT, cancelationToken,
			operations.applications()
			.push(toPushRequest(params)
					.noStart(true)
					.noRoute(true)
					.build()
			)
			.after(() ->
				Flux.merge(
					setRoutes(params.getAppName(), params.getRoutes()),
					setEnvVars(params.getAppName(), params.getEnv()),
					bindAndUnbindServices(params.getAppName(), params.getServices())
				)
				.after()
			)
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

	public Mono<Void> setRoutes(String appName, Collection<String> desiredUrls) {
		debug("setting routes for '"+appName+"': "+desiredUrls);

		//Carefull! It is not safe map/unnmap multiple routes in parallel. Doing so causes some of the
		// operations to fail, presumably because of some 'optimisitic locking' being used in the database
		// that keeps track of routes.
		//To avoid this problem we must execute all that map / unmap calls in sequence!
		return ReactorUtils.sequence(
				unmapUndesiredRoutes(appName, desiredUrls),
				mapDesiredRoutes(appName, desiredUrls)
		);
	}

	private Mono<Void> mapDesiredRoutes(String appName, Collection<String> desiredUrls) {
		Mono<Set<String>> currentUrlsMono = getUrls(appName).cache();
		Mono<Set<String>> domains = getDomainNames().cache();

		return currentUrlsMono.then((currentUrls) -> {
			debug("currentUrls = "+currentUrls);
			return Flux.fromIterable(desiredUrls)
			.flatMap((url) -> {
				if (currentUrls.contains(url)) {
					debug("skipping: "+url);
					return Mono.empty();
				} else {
					debug("mapping: "+url);
					return mapRoute(domains, appName, url);
				}
			}, 1) //!!!IN SEQUENCE!!!
			.after();
		});
	}

	private Mono<Void> mapRoute(Mono<Set<String>> domains, String appName, String desiredUrl) {
		debug("mapRoute: "+appName+" -> "+desiredUrl);
		return toRoute(domains, desiredUrl)
		.then((Route route) -> mapRoute(appName, route));
	}

	private Mono<Void> mapRoute(String appName, Route route) {
		MapRouteRequest mapRouteReq = MapRouteRequest.builder()
				.applicationName(appName)
				.domain(route.getDomain())
				.host(route.getHost())
				.path(route.getPath())
				.build();
		debug("mapRoute: "+mapRouteReq);
		return operations.routes().map(
				mapRouteReq
		);
	}

	private Mono<Route> toRoute(Mono<Set<String>> domains, String desiredUrl) {
		return domains.then((ds) -> {
			for (String d : ds) {
				//TODO: we assume that there's no 'path' component for now, which simpiflies things. What if there is a path component?
				if (desiredUrl.endsWith(d)) {
					String host = desiredUrl.substring(0, desiredUrl.length()-d.length());
					while (host.endsWith(".")) {
						host = host.substring(0, host.length()-1);
					}
					RouteBuilder route = Route.builder();
					route.domain(d);
					if (StringUtils.hasText(host)) {
						route.host(host);
					}
					return Mono.just(route.build());
				}
			}
			return Mono.error(new IOException("Couldn't find a domain matching "+desiredUrl));
		});
	}

	private Mono<Set<String>> getDomainNames() {
		return orgId.flatMap(this::requestDomains)
		.map((r) -> r.getEntity().getName())
		.toList()
		.map(ImmutableSet::copyOf);
	}

	private Mono<Set<String>> getUrls(String appName) {
		return operations.applications().get(GetApplicationRequest.builder()
				.name(appName)
				.build()
		)
		.map((app) -> ImmutableSet.copyOf(app.getUrls()));
	}

	private Mono<Void> unmapUndesiredRoutes(String appName, Collection<String> desiredUrls) {
		return getExistingRoutes(appName)
		.flatMap((route) -> {
			debug("unmap? "+route);
			if (desiredUrls.contains(getUrl(route))) {
				debug("unmap? "+route+" SKIP");
				return Mono.empty();
			} else {
				debug("unmap? "+route+" UNMAP");
				return unmapRoute(appName, route);
			}
		}, 1) //!!!IN SEQUENCE!!!
		.after();
	}

	private String getUrl(Route route) {
		String url = route.getDomain();
		if (route.getHost()!=null) {
			url = route.getHost() + "." + url;
		}
		String path = route.getPath();
		if (path!=null) {
			while (path.startsWith("/")) {
				path = path.substring(1);
			}
			if (StringUtils.hasText(path)) {
				url = url +"/" +path;
			}
		}
		return url;
	}

	private Mono<Void> unmapRoute(String appName, Route route) {
		String path = route.getPath();
		if (!StringUtil.hasText(path)) {
			//client doesn't like to get 'empty string' it will complain that route doesn't exist.
			path = null;
		}
		return operations.routes().unmap(UnmapRouteRequest.builder()
			.applicationName(appName)
			.domain(route.getDomain())
			.host(route.getHost())
			.path(path)
			.build()
		);
	}

	private Flux<Route> getExistingRoutes(String appName) {
		return operations.routes().list(ListRoutesRequest.builder()
				.level(Level.SPACE)
				.build()
		)
		.flatMap((route) -> {
			for (String app : route.getApplications()) {
				if (app.equals(appName)) {
					return Mono.just(route);
				}
			};
			return Mono.empty();
		});
	}

	private static PushApplicationRequestBuilder toPushRequest(CFPushArguments params) {
		return PushApplicationRequest.builder()
		.name(params.getAppName())
		.memory(params.getMemory())
		.diskQuota(params.getDiskQuota())
		.timeout(params.getTimeout())
		.buildpack(params.getBuildpack())
		.command(params.getCommand())
		.stack(params.getStack())
		.instances(params.getInstances())
		.application(params.getApplicationData());
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

	public Mono<Void> createUserProvidedService(String name, Map<String, Object> credentials) {
		return operations.services().createUserProvidedInstance(CreateUserProvidedServiceInstanceRequest.builder()
				.name(name)
				.credentials(credentials)
				.build()
		);
	}

	@Override
	public void deleteService(String serviceName) {
		deleteServiceMono(serviceName).get();
	}

	public Mono<Void> deleteServiceMono(String serviceName) {
		return getService(serviceName)
		.then(this::deleteServiceInstance);
	}

	protected Mono<Void> deleteServiceInstance(ServiceInstance s) {
		switch (s.getType()) {
		case MANAGED:
			return client.serviceInstances().delete(DeleteServiceInstanceRequest.builder()
					.serviceInstanceId(s.getId())
					.build()
			)
			.after();
		case USER_PROVIDED:
			return client.userProvidedServiceInstances().delete(DeleteUserProvidedServiceInstanceRequest.builder()
					.userProvidedServiceInstanceId(s.getId())
					.build()
			);
		default:
			return Mono.error(new IllegalStateException("Unknown service type: "+s.getType()));
		}
	}

	protected Mono<ServiceInstance> getService(String serviceName) {
		return operations.services().getInstance(GetServiceInstanceRequest.builder()
				.name(serviceName)
				.build()
		);
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
	public Map<String, String> getApplicationEnvironment(String appName) throws Exception {
		return ReactorUtils.get(getEnv(appName));
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
