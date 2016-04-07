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
package org.springframework.ide.eclipse.boot.dash.test.mocks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.cloudfoundry.client.lib.domain.InstanceState;
import org.eclipse.core.runtime.Assert;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFAppState;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplicationDetail;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFInstanceState;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFInstanceStats;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.ApplicationExtras;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.CFApplicationDetailData;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.CFApplicationSummaryData;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens.CancelationToken;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.HealthCheckSupport;
import org.springsource.ide.eclipse.commons.frameworks.test.util.ACondition;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;

import reactor.core.publisher.Mono;

public class MockCFApplication {

	private static class MockCFInstanceStats implements CFInstanceStats {

		private CFInstanceState state;

		public MockCFInstanceStats(CFInstanceState state) {
			this.state = state;
		}

		@Override
		public CFInstanceState getState() {
			return state;
		}

		@Override
		public String toString() {
			return "CFInstanceState("+state+")";
		}
	}

	private final String name;
	private final UUID guid;
	private final int instances;

	private Map<String, String> env = new HashMap<>();
	private int memory = 1024;
	private List<String> services = new ArrayList<>();
	private String buildpackUrl = null;
	private List<String> uris = new ArrayList<>();
	private CFAppState state = CFAppState.STOPPED;
	private int diskQuota = 1024;
	private int timeout = (int)TimeUnit.MINUTES.toMillis(2);
	private String command = null;
	private String stack = null;
	private MockCloudFoundryClientFactory owner;

	public MockCFApplication(MockCloudFoundryClientFactory owner, String name, UUID guid, int instances, CFAppState state) {
		this.owner = owner;
		this.name = name;
		this.guid = guid;
		this.instances = instances;
		this.state = state;
	}

	private String healthCheck=HealthCheckSupport.HC_PORT;
	private ImmutableList<CFInstanceStats> stats = ImmutableList.of();

	private CancelationTokens cancelationTokens = new CancelationTokens();

	public MockCFApplication(MockCloudFoundryClientFactory owner, String name) {
		this(owner,
				name,
				UUID.randomUUID(),
				1,
				CFAppState.STOPPED
		);
	}

	public String getName() {
		return name;
	}

	public List<CFInstanceStats> getStats() {
		return stats;
	}

	public void start() throws Exception {
		Assert.isLegal(CFAppState.STOPPED==state);
		Assert.isLegal(stats.isEmpty());
		this.state = CFAppState.UNKNOWN;
		final long endTime = System.currentTimeMillis()+getStartDelay();
		final CancelationToken cancelToken = cancelationTokens.create();
		new ACondition("simulated app starting (waiting)", getStartDelay()+1000) {
			@Override
			public boolean test() throws Exception {
				if (!cancelToken.isCanceled() && System.currentTimeMillis()<endTime) {
					System.out.println("Starting "+getName()+"...");
					throw new IOException("App still starting");
				}
				return true;
			}
		};
		Builder<CFInstanceStats> builder = ImmutableList.builder();
		for (int i = 0; i < instances; i++) {
			Map<String, Object> values = new HashMap<>();
			values.put("state", InstanceState.RUNNING.toString());
			CFInstanceStats stat = new MockCFInstanceStats(CFInstanceState.RUNNING);
			builder.add(stat);
		}
		if (cancelToken.isCanceled()) {
			System.out.println("Starting "+getName()+" CANCELED");
			throw new IOException("Operation Canceled");
		}
		this.stats = builder.build();
		this.state = CFAppState.STARTED;
		System.out.println("Starting "+getName()+" SUCCESS");
	}

	private long getStartDelay() {
		return owner.getStartDelay();
	}

	public String getHealthCheck() {
		return healthCheck;
	}

	public void setHealthCheck(String healthCheck) {
		this.healthCheck = healthCheck;
	}

//	public int getInstances() {
//		return instances;
//	}

	public int getRunningInstances() {
		int runningInstances = 0;
		for (CFInstanceStats instance : getStats()) {
			if (instance.getState()==CFInstanceState.RUNNING) {
				runningInstances++;
			}
		}
		return runningInstances;
	}

	public UUID getGuid() {
		return guid;
	}

	public void setBuildpackUrl(String buildpackUrl) {
		this.buildpackUrl = buildpackUrl;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public void setStack(String stack) {
		this.stack = stack;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public void setDiskQuota(int diskQuota) {
		this.diskQuota = diskQuota;
	}

	public void setMemory(int memory) {
		this.memory = memory;
	}

	public void setUris(Collection<String> uris) {
		this.uris = uris==null?null:ImmutableList.copyOf(uris);
	}

	public void setServices(Collection<String> services) {
		this.services = services==null?null:ImmutableList.copyOf(services);
	}

	public CFApplication getBasicInfo() {
		return new CFApplicationSummaryData(
				name,
				instances,
				getRunningInstances(),
				memory,
				guid,
				uris,
				state,
				diskQuota,
				getExtras()
		);
	}

	private static <T> Mono<T> just(T it) {
		return it == null ? Mono.empty() : Mono.just(it);
	}

	private ApplicationExtras getExtras() {
		return new ApplicationExtras() {
			@Override
			public Mono<String> getStack() {
				return just(stack);
			}
			@Override
			public Mono<List<String>> getServices() {
				return just(services);
			}

			@Override
			public Mono<Map<String, String>> getEnv() {
				return just(env);
			}

			@Override
			public Mono<String> getBuildpack() {
				return just(buildpackUrl);
			}
			@Override
			public Mono<Integer> getTimeout() {
				return just(timeout);
			}
			@Override
			public Mono<String> getCommand() {
				return just(command);
			}
		};
	}

	public CFApplicationDetail getDetailedInfo() {
		return new CFApplicationDetailData(
				new CFApplicationSummaryData(
						name,
						instances,
						getRunningInstances(),
						memory,
						guid,
						uris,
						state,
						diskQuota,
						getExtras()
				),
				ImmutableList.copyOf(stats)
		);
//		return new CFApplicationDetailData(getBasicInfo(), ImmutableList.copyOf(stats));
	}

	@Override
	public String toString() {
		return "MockCFApp("+name+")";
	}

	public void stop() {
		cancelationTokens.cancelAll();
		this.stats = ImmutableList.of();
		this.state = CFAppState.STOPPED;
	}

	public Map<String, String> getEnv() {
		return env;
	}

	public void setEnv(Map<String, String> newEnv) {
		env = ImmutableMap.copyOf(newEnv);
	}

	public void restart() throws Exception {
		stop();
		start();
	}

	public void setBuildpackUrlMaybe(String buildpack) {
		if (buildpack!=null) {
			setBuildpackUrl(buildpack);
		}
	}

	public void setCommandMaybe(String command) {
		if (command!=null) {
			setCommand(command);
		}
	}

	public void setDiskQuotaMaybe(Integer diskQuota) {
		if (diskQuota!=null) {
			setDiskQuota(diskQuota);
		}
	}

	public void setEnvMaybe(Map<String, String> env) {
		if (env!=null) {
			setEnv(env);
		}
	}

	public void setMemoryMaybe(Integer memory) {
		if (memory!=null) {
			setMemory(memory);
		}
	}

	public void setServicesMaybe(List<String> services) {
		if (services!=null) {
			setServices(services);
		}
	}

	public void setStackMaybe(String stack) {
		if (stack!=null) {
			setStack(stack);
		}
	}

	public void setTimeoutMaybe(Integer timeout) {
		if (timeout!=null) {
			setTimeout(timeout);
		}
	}

}
