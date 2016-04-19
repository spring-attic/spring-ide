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

import org.cloudfoundry.client.lib.ApplicationLogListener;
import org.cloudfoundry.client.lib.StreamingLogToken;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.CFPushArguments;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens.CancelationToken;
import org.springsource.ide.eclipse.commons.cloudfoundry.client.diego.SshClientSupport;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ClientRequests {

	/**
	 * Returns null if the application does not exist. Throws some kind of Exception if there's any other kind of problem.
	 */
	CFApplicationDetail getApplication(String appName) throws Exception;

	//TODO: consider removing the getXXXSupport method and directly adding the apis that these support
	// objects provide.
	SshClientSupport getSshClientSupport() throws Exception;


	void deleteApplication(String name) throws Exception;
	Version getApiVersion();
	void logout();

	List<CFApplication> getApplicationsWithBasicInfo() throws Exception;
	List<CFBuildpack> getBuildpacks() throws Exception;
	List<CFCloudDomain> getDomains() throws Exception;
	List<CFServiceInstance> getServices() throws Exception;
	List<CFSpace> getSpaces() throws Exception;
	List<CFStack> getStacks() throws Exception;
	void restartApplication(String appName, CancelationToken token) throws Exception;
	void stopApplication(String appName) throws Exception;
	Mono<StreamingLogToken> streamLogs(String appName, ApplicationLogListener logConsole) throws Exception;
	Flux<CFApplicationDetail> getApplicationDetails(List<CFApplication> appsToLookUp) throws Exception;
	String getHealthCheck(UUID appGuid) throws Exception;
	void setHealthCheck(UUID guid, String hcType) throws Exception;
	boolean applicationExists(String appName) throws Exception;

	//Removed in V2
	//void createApplication(CloudApplicationDeploymentProperties deploymentProperties) throws Exception;

	//Added since v2:
	void push(CFPushArguments args, CancelationToken cancelationToken) throws Exception;
	Map<String, String> getApplicationEnvironment(String appName) throws Exception;
}
