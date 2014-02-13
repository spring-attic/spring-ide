/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.livegraph.model;

import org.eclipse.core.resources.IProject;

/**
 * @author Leo Dos Santos
 */
public class LiveBeansSession {

	private final String serviceUrl;

	private final String username;

	private final String password;

	private final String appName;
	
	private final IProject project;

	public LiveBeansSession(String serviceUrl, String username, String password, String appName, IProject project) {
		this.serviceUrl = serviceUrl;
		this.username = username;
		this.password = password;
		this.appName = appName;
		this.project = project;
	}

	public String getApplicationName() {
		return appName;
	}

	public String getPassword() {
		return password;
	}

	public String getServiceUrl() {
		return serviceUrl;
	}

	public String getUsername() {
		return username;
	}

	public IProject getProject() {
		return project;
	}

}
