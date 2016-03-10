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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.cloudfoundry.client.lib.domain.CloudApplication.AppState;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;

public class CFApplicationData implements CFApplication {

	private final String name;
	private final UUID guid;
	private final int instances;
	private final int runningInstances;
	private final AppState state;
	private final int memory;
	private final int diskQuota;
	private final String detectedBuildpack;
	private final String buildpackUrl;
	private final Map<String, String> env;
	private final List<String> services;
	private final List<String> uris;
	private final Integer timeout;
	private final String command;
	private final String stack;

	@Override
	public String getName() {
		return name;
	}

	public CFApplicationData(String name, UUID guid, int instances, int runningInstances, AppState state, int memory,
			int diskQuota, String detectedBuildpack, String buildpackUrl, Map<String, String> env,
			List<String> services, List<String> uris, Integer timeout, String command, String stack) {
		super();
		this.name = name;
		this.guid = guid;
		this.instances = instances;
		this.runningInstances = runningInstances;
		this.state = state;
		this.memory = memory;
		this.diskQuota = diskQuota;
		this.detectedBuildpack = detectedBuildpack;
		this.buildpackUrl = buildpackUrl;
		this.env = env;
		this.services = services;
		this.uris = uris;
		this.timeout = timeout;
		this.command = command;
		this.stack = stack;
	}

	@Override
	public int getInstances() {
		return instances;
	}

	@Override
	public int getRunningInstances() {
		return runningInstances;
	}

	@Override
	public Map<String, String> getEnvAsMap() {
		return env;
	}

	@Override
	public int getMemory() {
		return memory;
	}

	@Override
	public UUID getGuid() {
		return guid;
	}

	@Override
	public List<String> getServices() {
		return services;
	}

	@Override
	public String getDetectedBuildpack() {
		return detectedBuildpack;
	}

	@Override
	public String getBuildpackUrl() {
		return buildpackUrl;
	}

	@Override
	public List<String> getUris() {
		return uris;
	}

	@Override
	public AppState getState() {
		return state;
	}

	@Override
	public int getDiskQuota() {
		return diskQuota;
	}

	public Integer getTimeout() {
		return timeout;
	}

	public String getCommand() {
		return command;
	}

	public String getStack() {
		return stack;
	}

}
