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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.CFCloudDomain;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.client.v2.CFPushArguments;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.routes.RouteBinding;

/**
 * Interface for Cloud Foundry application deployment properties
 *
 * @author Alex Boyko
 *
 */
public interface DeploymentProperties {

	int DEFAULT_MEMORY = 1024;
	int DEFAULT_INSTANCES = 1;
	String DEFAULT_HEALTH_CHECK_TYPE = "port";

	String getAppName();

	int getMemory();

	int getDiskQuota();

	Integer getTimeout();

	String getHealthCheckType();

	String getHealthCheckHttpEndpoint();

	String getBuildpack();

	String getCommand();

	String getStack();

	Map<String, String> getEnvironmentVariables();

	int getInstances();

	List<String> getServices();

	Set<RouteBinding> getUris();

	CFPushArguments toPushArguments(List<CFCloudDomain> cloudDomains) throws Exception;

	void setArchive(File archive);

	IProject getProject();

	IFile getManifestFile();

}
