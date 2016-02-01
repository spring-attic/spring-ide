/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.client;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.cloudfoundry.client.lib.StreamingLogToken;
import org.cloudfoundry.client.lib.archive.ApplicationArchive;
import org.cloudfoundry.client.lib.domain.ApplicationStats;
import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.cloudfoundry.client.lib.domain.Staging;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.console.ApplicationLogConsole;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.BuildpackSupport.Buildpack;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.SshClientSupport;

public interface ClientRequests {

	//TODO: consider removing the getXXXSupport method and directly adding the apis that these support
	// objects provide.
	SshClientSupport getSshClientSupport() throws Exception;

	void createApplication(CloudApplicationDeploymentProperties deploymentProperties) throws Exception;
	void deleteApplication(String name) throws Exception;
	Version getApiVersion();
	void logout();
	CFApplication getApplication(String appName) throws Exception;
	List<CFApplication> getApplicationsWithBasicInfo() throws Exception;
	List<Buildpack> getBuildpacks() throws Exception;
	List<CloudDomain> getDomains() throws Exception;
	CloudAppInstances getExistingAppInstances(String appName) throws Exception;
	CloudAppInstances getExistingAppInstances(UUID guid) throws Exception;
	List<CFService> getServices() throws Exception;
	List<CFSpace> getSpaces() throws Exception;
	void restartApplication(String appName) throws Exception;
	void stopApplication(String appName) throws Exception;
	StreamingLogToken streamLogs(String appName, ApplicationLogConsole logConsole);
	void updateApplicationEnvironment(String appName, Map<String, String> environmentVariables) throws Exception;
	void updateApplicationInstances(String appName, int instances) throws Exception;
	void updateApplicationMemory(String appName, int memory) throws Exception;
	void updateApplicationServices(String appName, List<String> services) throws Exception;
	void updateApplicationStaging(String appName, Staging staging) throws Exception;
	void updateApplicationUris(String appName, List<String> urls) throws Exception;
	Map<CFApplication, ApplicationStats> waitForApplicationStats(List<CFApplication> appsToLookUp,
			long timeToWait) throws Exception;
	void uploadApplication(String appName, ApplicationArchive archive) throws Exception;
	String getHealthCheck(UUID appGuid);
	void setHealthCheck(UUID guid, String hcType);
	void updateApplicationDiskQuota(String appName, int diskQuota) throws Exception;
}
