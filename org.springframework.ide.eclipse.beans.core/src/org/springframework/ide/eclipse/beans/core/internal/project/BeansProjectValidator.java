/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.project;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigValidator;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.core.project.IProjectBuilder;

/**
 * @author Torsten Juergeleit
 */
public class BeansProjectValidator implements IProjectBuilder {

	public void build(IResource resource, int kind, IProgressMonitor monitor) {
		if (BeansCoreUtils.isBeansConfig(resource)) {
			validate((IFile) resource, monitor);
		}
	}

	public static void validate(IFile configFile, IProgressMonitor monitor) {
		monitor.subTask("Validating Spring Beans config file ["
				+ configFile.getFullPath().toString() + "]");
		IWorkspaceRunnable validator = new BeansConfigValidator(configFile);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		ISchedulingRule rule = workspace.getRuleFactory()
				.markerRule(configFile);
		try {
			workspace.run(validator, rule, IWorkspace.AVOID_UPDATE, monitor);
		}
		catch (CoreException e) {
			BeansCorePlugin.log("Error while running Spring Beans " +
					"config validator", e);
		}
		finally {
			monitor.done();
		}
	}

	public void cleanup(IResource resource, IProgressMonitor monitor) {
		try {
			monitor.subTask("Deleting Spring Beans problem markers ["
					+ resource.getFullPath().toString() + "]");
			BeansModelUtils.deleteProblemMarkers(BeansModelUtils
					.getResourceModelElement(resource));
		}
		finally {
			monitor.done();
		}
	}
}
