/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.livegraph.model;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jst.server.core.IWebModule;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.ServerUtil;
import org.springframework.ide.eclipse.beans.ui.live.model.TypeLookupImpl;

/**
 * Live Beans Session implementation
 * 
 * @author Alex Boyko
 *
 */
public class LiveBeansSession extends TypeLookupImpl {

	private final String serviceUrl;

	private final String username;

	private final String password;

	public LiveBeansSession(String serviceUrl, String username, String password, String appName, IProject project) {
		super(appName, project);
		this.serviceUrl = serviceUrl;
		this.username = username;
		this.password = password;
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

	@Override
	public IProject[] relatedProjects() {
		ArrayList<IProject> projects = new ArrayList<>();
		projects.addAll(Arrays.asList(super.relatedProjects()));
		String appName = getApplicationName();
		if (appName != null && !"".equals(appName)) {
			IModule[] modules = ServerUtil.getModules("jst.web");
			for (IModule module : modules) {
				Object obj = module.loadAdapter(IWebModule.class, new NullProgressMonitor());
				if (obj instanceof IWebModule) {
					IWebModule webModule = (IWebModule) obj;
					if (appName.equals(webModule.getContextRoot())) {
						projects.add(module.getProject());
					}
				}
			}
		}
		return projects.toArray(new IProject[projects.size()]);
	}

}
