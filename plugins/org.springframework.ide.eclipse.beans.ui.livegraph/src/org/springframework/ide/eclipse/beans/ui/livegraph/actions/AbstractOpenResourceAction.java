/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.livegraph.actions;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jst.server.core.IWebModule;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.ServerUtil;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBeansSession;
import org.springsource.ide.eclipse.commons.core.JdtUtils;
import org.springsource.ide.eclipse.commons.ui.SpringUIUtils;

/**
 * @author Leo Dos Santos
 */
public abstract class AbstractOpenResourceAction extends BaseSelectionListenerAction {

	protected AbstractOpenResourceAction(String text) {
		super(text);
	}

	protected String cleanClassName(String className) {
		String cleanClassName = className;
		if (className != null) {
			int ix = className.indexOf('$');
			if (ix > 0) {
				cleanClassName = className.substring(0, ix);
			}
			else {
				ix = className.indexOf('#');
				if (ix > 0) {
					cleanClassName = className.substring(0, ix);
				}
			}
		}
		return cleanClassName;
	}

	protected String extractClassName(String resourcePath) {
		int index = resourcePath.lastIndexOf("/WEB-INF/classes/");
		int length = "/WEB-INF/classes/".length();
		if (index >= 0) {
			resourcePath = resourcePath.substring(index + length);
		}
		resourcePath = resourcePath.substring(0, resourcePath.lastIndexOf(".class"));
		resourcePath = resourcePath.replace(File.separator, ".");
		return resourcePath;
	}

	protected String extractResourcePath(String resourceStr) {
		// Extract the resource path out of the descriptive text
		int indexStart = resourceStr.indexOf("[");
		int indexEnd = resourceStr.indexOf("]");
		if (indexStart > -1 && indexEnd > -1 && indexStart < indexEnd) {
			resourceStr = resourceStr.substring(indexStart + 1, indexEnd);
		}
		return resourceStr;
	}

	protected IProject[] findProjects(LiveBeansSession session) {
		Set<IProject> projects = new HashSet<IProject>();
		
		IProject p = session.getProject();
		if (p!=null) {
			projects.add(p);
		}
		
		String appName = session.getApplicationName();
		if (appName!=null && !"".equals(appName)) {
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

	protected boolean hasTypeInProject(LiveBeansSession session, String className) {
		IProject[] projects = findProjects(session);
		for (IProject project : projects) {
			IType type = JdtUtils.getJavaType(project, cleanClassName(className));
			if (type != null) {
				return true;
			}
		}
		return false;
	}

	protected void openInEditor(LiveBeansSession session, String className) {
		IProject[] projects = findProjects(session);
		for (IProject project : projects) {
			IType type = JdtUtils.getJavaType(project, cleanClassName(className));
			if (type != null) {
				SpringUIUtils.openInEditor(type);
				break;
			}
		}
	}

}
