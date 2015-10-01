/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;

public class BaseDeploymentProperties {
	/*
	 * URLs should never be null. If no URLs are needed, keep list empty
	 */
	private final List<String> urls = new ArrayList<String>();

	private String appName;

	private IProject project;

	public BaseDeploymentProperties(IProject project) {
		this.project = project;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	public IProject getProject() {
		return this.project;
	}

	/**
	 *
	 * @return never null
	 */
	public List<String> getUrls() {
		return urls;
	}

	public void setUrls(List<String> urls) {
		this.urls.clear();
		if (urls != null) {
			this.urls.addAll(urls);
		}
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public String getAppName() {
		return appName;
	}
}
