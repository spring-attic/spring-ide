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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.cloudfoundry.client.lib.domain.ApplicationStats;
import org.cloudfoundry.client.lib.domain.CloudApplication.AppState;
import org.cloudfoundry.client.lib.domain.InstanceState;
import org.cloudfoundry.client.lib.domain.InstanceStats;
import org.eclipse.core.runtime.Assert;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.HealthCheckSupport;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;

public class MockCFApplication {

	private final String name;
	private final UUID guid;
	private final int instances;

	private Map<String, String> env = new HashMap<>();
	private int memory = 1024;
	private List<String> services = new ArrayList<>();
	private String detectedBuildpack = null;
	private String buildpackUrl = null;
	private List<String> uris = new ArrayList<>();
	private AppState state = AppState.STOPPED;
	private int diskQuota = 1024;
	private Integer timeout = null;
	private String command = null;
	private String stack = null;

	public MockCFApplication(String name, UUID guid, int instances, AppState state) {
		this.name = name;
		this.guid = guid;
		this.instances = instances;
		this.state = state;
	}

	private String healthCheck=HealthCheckSupport.HC_PORT;
	private List<InstanceStats> stats = new ArrayList<>();

	public MockCFApplication(String name) {
		this(name,
				UUID.randomUUID(),
				1,
				AppState.STOPPED
		);
	}

	public String getName() {
		return name;
	}

	public ApplicationStats getStats() {
		return new ApplicationStats(ImmutableList.copyOf(stats));
	}

	public void start() {
		Assert.isLegal(AppState.STOPPED==state);
		Assert.isLegal(stats.isEmpty());
		Builder<InstanceStats> builder = ImmutableList.builder();
		for (int i = 0; i < instances; i++) {
			Map<String, Object> values = new HashMap<>();
			values.put("state", InstanceState.RUNNING.toString());
			InstanceStats stat = new InstanceStats(UUID.randomUUID().toString(), values);
			builder.add(stat);
		}
		this.stats = builder.build();
		this.state = AppState.STARTED;
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
		for (InstanceStats instance : stats) {
			if (instance.getState()==InstanceState.RUNNING) {
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

	public void setTimeout(Integer timeout) {
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
		return new CFApplicationData(name, guid, instances, getRunningInstances(),
				state, memory, diskQuota, detectedBuildpack, buildpackUrl,
				env, services, uris, timeout, command, stack);
	}

	@Override
	public String toString() {
		return "MockCFApp("+name+")";
	}

	public void stop() {
		//TODO: in real CF this would not happen instantly so to be a 'good' simulation the stuff
		// we do here should probably be done asynchly, but that opens up a whole new can of worms,
		// such as ... who or what should controll the timing?
		//If we do something smart with this, perhaps we could have some kind of harness that forces sequencings
		// to expose race conditions in code.
		this.stats = ImmutableList.of();
		this.state = AppState.STOPPED;
	}

	public void setEnv(Map<String, String> newEnv) {
		env = ImmutableMap.copyOf(newEnv);
	}

	public void restart() {
		//TODO: same comment as stop.
		stop();
		start();
	}

}
