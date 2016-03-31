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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.Assert;

/**
 * Arguments passed to push operation.
 *
 * @author Kris De Volder
 * @author Nieraj Singh
 */
public class CFPushArguments implements AutoCloseable {
	private String host;
	private String domain;
	private boolean noRoute;
	private boolean noHost;
	private String appName;
	private Integer memory;
	private Integer diskQuota;
	private Integer timeout;
	private String buildpack;
	private String command;
	private String stack;
	private Map<String, String> env;
	private int instances;
	private List<String> services;
	private InputStream applicationData;

	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public boolean isNoRoute() {
		return noRoute;
	}
	public void setNoRoute(boolean noRoute) {
		this.noRoute = noRoute;
	}
	public boolean isNoHost() {
		return noHost;
	}
	public void setNoHost(boolean noHost) {
		this.noHost = noHost;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public int getMemory() {
		return memory;
	}
	public void setMemory(int memory) {
		this.memory = memory;
	}
	public Integer getDiskQuota() {
		return diskQuota;
	}
	public void setDiskQuota(Integer diskQuota) {
		this.diskQuota = diskQuota;
	}
	public Integer getTimeout() {
		return timeout;
	}
	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}
	public String getBuildpack() {
		return buildpack;
	}
	public void setBuildpack(String buildpack) {
		this.buildpack = buildpack;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public String getStack() {
		return stack;
	}
	public void setStack(String stack) {
		this.stack = stack;
	}
	public Map<String, String> getEnv() {
		return env;
	}
	public void setEnv(Map<String, String> env) {
		this.env = env;
	}
	public int getInstances() {
		return instances;
	}
	public void setInstances(int instances) {
		this.instances = instances;
	}
	public List<String> getServices() {
		return services;
	}
	public void setServices(List<String> services) {
		this.services = services;
	}
	public InputStream getApplicationData() {
		return applicationData;
	}
	@Override
	public void close() throws Exception {
		if (applicationData!=null) {
			applicationData.close();
		}
	}
	public void setApplicationData(File archive) throws FileNotFoundException {
		Assert.isLegal(applicationData==null, "Can only set this once");
		this.applicationData = new FileInputStream(archive);
	}
}
