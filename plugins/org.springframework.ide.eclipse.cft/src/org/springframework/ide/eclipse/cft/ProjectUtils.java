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
package org.springframework.ide.eclipse.cft;

import org.eclipse.cft.server.core.internal.CloudFoundryProjectUtil;
import org.eclipse.cft.server.core.internal.client.CloudFoundryApplicationModule;
import org.eclipse.core.resources.IProject;
import org.eclipse.wst.server.core.IModule;
import org.springframework.ide.eclipse.boot.core.BootPropertyTester;

public class ProjectUtils {

	public static boolean isSpringBootProject(IModule module) {
		IProject project = null;
		if (module instanceof CloudFoundryApplicationModule) {
			project = CloudFoundryProjectUtil.getProject((CloudFoundryApplicationModule) module);
		} else if (module != null) {
			project = module.getProject();
		}
		return project != null && BootPropertyTester.isBootProject(project);
	}
	
	public static boolean isJavaProject(IModule module) {
		IProject project = null;
		if (module instanceof CloudFoundryApplicationModule) {
			project = CloudFoundryProjectUtil.getProject((CloudFoundryApplicationModule) module);
		} else if (module != null) {
			project = module.getProject();
		}
		return project != null && CloudFoundryProjectUtil.isJavaProject(project);
	}
}
