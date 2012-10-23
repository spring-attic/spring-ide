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
package org.springframework.ide.eclipse.roo.ui.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.roo.core.RooCoreActivator;
import org.springframework.ide.eclipse.roo.core.internal.model.DefaultRooInstall;
import org.springframework.ide.eclipse.roo.core.model.IRooInstall;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;

/**
 * Roo utility methods.
 * 
 * @author Steffen Pingel
 * @author Leo Dos Santos
 */
public class RooUiUtil {

	public static List<IProject> getAllRooProjects() {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		List<IProject> projects = new ArrayList<IProject>();
		for (IProject project : root.getProjects()) {
			if (project.isAccessible() && project.isOpen()
					&& SpringCoreUtils.hasNature(project, RooCoreActivator.NATURE_ID)) {
				projects.add(project);
			}
		}
		return projects;
	}
	
	public static boolean isRoo120OrGreater(IRooInstall install) {
		if (install != null) {
			String versionStr = install.getVersion();
			if (versionStr != null && !DefaultRooInstall.UNKNOWN_VERSION.equals(versionStr)) {
				Version version;
				if (versionStr.contains(" ")) {
					int index = versionStr.indexOf(" ");
					version = Version.parseVersion(versionStr.substring(0, index));
				}
				else {
					version = Version.parseVersion(versionStr);
				}
				return version.compareTo(Version.parseVersion("1.2.0")) >= 0;
			}
		}
		return false;
	}
	
}
