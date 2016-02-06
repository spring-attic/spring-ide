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
import java.util.HashMap;
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

	@Override
	public String getName() {
		return name;
	}

	public CFApplicationData(String name, UUID guid, int instances, int runningInstances, AppState state) {
		super();
		this.name = name;
		this.guid = guid;
		this.instances = instances;
		this.runningInstances = runningInstances;
		this.state = state;
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
