package org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFAppState;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFApplication;

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
public class CFApplicationSummaryData implements CFApplication {

	private String name;
	private int instances;
	private int runningInstances;
	private Map<String, String> env;
	private int memory;
	private UUID guid;
	private List<String> services;
	private String detectedBuildpack;
	private String buildpackUrl;
	private List<String> uris;
	private CFAppState state;
	private int diskQuota;
	private Integer timeout;
	private String command;
	private String stack;



	public CFApplicationSummaryData(String name, int instances, int runningInstances, Map<String, String> env,
			int memory, UUID guid, List<String> services, String detectedBuildpack, String buildpackUrl,
			List<String> uris, CFAppState state, int diskQuota, Integer timeout, String command, String stack) {
		super();
		this.name = name;
		this.instances = instances;
		this.runningInstances = runningInstances;
		this.env = env;
		this.memory = memory;
		this.guid = guid;
		this.services = services;
		this.detectedBuildpack = detectedBuildpack;
		this.buildpackUrl = buildpackUrl;
		this.uris = uris;
		this.state = state;
		this.diskQuota = diskQuota;
		this.timeout = timeout;
		this.command = command;
		this.stack = stack;
	}

	@Override
	public String getName() {
		return name;
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
	public CFAppState getState() {
		return state;
	}

	@Override
	public int getDiskQuota() {
		return diskQuota;
	}

	@Override
	public Integer getTimeout() {
		return timeout;
	}

	@Override
	public String getCommand() {
		return command;
	}

	@Override
	public String getStack() {
		return stack;
	}

}
