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
package org.springframework.ide.eclipse.webflow.core.internal.model.project;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.springframework.ide.eclipse.core.project.IProjectBuilder;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowValidator;

/**
 * {@link IProjectBuilder} that validates Spring Web Flow configuration files.
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowProjectValidator implements IProjectBuilder {

	public void build(IFile file, int kind, IProgressMonitor monitor) {
		if (WebflowModelUtils.isWebflowConfig(file)) {
			validate(file, monitor);
		}
	}

	protected void validate(IFile file, IProgressMonitor monitor) {
		monitor.subTask("Validating Spring Web Flow file ["
				+ file.getFullPath().toString() + "]");
		IWorkspaceRunnable validator = new WebflowValidator(file);
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		ISchedulingRule rule = workspace.getRuleFactory().markerRule(file);
		try {
			workspace.run(validator, rule, IWorkspace.AVOID_UPDATE, monitor);
		}
		catch (CoreException e) {
			Activator.log(e);
		}
		finally {
			monitor.done();
		}
	}

	public void cleanup(IResource resource, IProgressMonitor monitor) {
		try {
			if (resource instanceof IFile
					&& WebflowModelUtils.isWebflowConfig(((IFile) resource))) {
				monitor.subTask("Deleting Spring Web Flow problem markers ["
						+ resource.getFullPath().toString().substring(1) + "]");
				WebflowModelUtils.deleteProblemMarkers(resource);
			}
		}
		finally {
			monitor.done();
		}
	}
}
