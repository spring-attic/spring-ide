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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jst.server.core.IWebModule;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.ServerUtil;
import org.springsource.ide.eclipse.commons.core.JdtUtils;
import org.springsource.ide.eclipse.commons.ui.SpringUIUtils;

/**
 * @author Leo Dos Santos
 */
public abstract class AbstractOpenResourceAction extends BaseSelectionListenerAction {

	protected AbstractOpenResourceAction(String text) {
		super(text);
	}

	protected String cleanClassName(final String className) {
		String cleanClassName = className;
		int ix = className.indexOf('$');
		if (ix > 0) {
			cleanClassName = className.substring(0, ix);
		}
		return cleanClassName;
	}

	protected IProject[] findProjects(final String contextRoot) {
		Set<IProject> projects = new HashSet<IProject>();
		IModule[] modules = ServerUtil.getModules("jst.web");
		for (IModule module : modules) {
			Object obj = module.loadAdapter(IWebModule.class, new NullProgressMonitor());
			if (obj instanceof IWebModule) {
				IWebModule webModule = (IWebModule) obj;
				if (contextRoot.equals(webModule.getContextRoot())) {
					projects.add(module.getProject());
				}
			}
		}
		return projects.toArray(new IProject[projects.size()]);
	}

	protected void openInEditor(String appName, String className) {
		IProject[] projects = findProjects(appName);
		for (IProject project : projects) {
			IType type = JdtUtils.getJavaType(project, cleanClassName(className));
			if (type != null) {
				SpringUIUtils.openInEditor(type);
				break;
			}
		}
	}

}
