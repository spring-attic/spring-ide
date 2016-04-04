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
import java.util.zip.ZipFile;

import org.cloudfoundry.client.lib.ApplicationLogListener;
import org.cloudfoundry.client.lib.StreamingLogToken;
import org.cloudfoundry.client.lib.domain.Staging;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.CFPushArguments;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.SshClientSupport;

public interface ClientRequests {

	/**
	 * Returns null if the application does not exist. Throws some kind of Exception if there's any other kind of problem.
	 */
	CFApplicationDetail getApplication(String appName) throws Exception;

	//TODO: consider removing the getXXXSupport method and directly adding the apis that these support
	// objects provide.
	SshClientSupport getSshClientSupport() throws Exception;

	void createApplication(CloudApplicationDeploymentProperties deploymentProperties) throws Exception;
	void deleteApplication(String name) throws Exception;
	Version getApiVersion();
	void logout();

	List<CFApplication> getApplicationsWithBasicInfo() throws Exception;
	List<CFBuildpack> getBuildpacks() throws Exception;
	List<CFCloudDomain> getDomains() throws Exception;
	List<CFService> getServices() throws Exception;
	List<CFSpace> getSpaces() throws Exception;
	List<CFStack> getStacks() throws Exception;
	void restartApplication(String appName) throws Exception;
	void stopApplication(String appName) throws Exception;
	StreamingLogToken streamLogs(String appName, ApplicationLogListener logConsole);
	void updateApplicationEnvironment(String appName, Map<String, String> environmentVariables) throws Exception;
	void updateApplicationInstances(String appName, int instances) throws Exception;
	void updateApplicationMemory(String appName, int memory) throws Exception;
	void updateApplicationServices(String appName, List<String> services) throws Exception;
	void updateApplicationStaging(String appName, Staging staging) throws Exception;
	void updateApplicationUris(String appName, List<String> urls) throws Exception;
	List<CFApplicationDetail> waitForApplicationDetails(List<CFApplication> appsToLookUp, long timeToWait) throws Exception;
	void uploadApplication(String appName, ZipFile archive) throws Exception;
	String getHealthCheck(UUID appGuid) throws Exception;
	void setHealthCheck(UUID guid, String hcType) throws Exception;
	void updateApplicationDiskQuota(String appName, int diskQuota) throws Exception;
	boolean applicationExists(String appName) throws Exception;

	//Added since v2:

	void push(CFPushArguments properties) throws Exception;
	Map<String, String> getApplicationEnvironment(String appName) throws Exception;

}
