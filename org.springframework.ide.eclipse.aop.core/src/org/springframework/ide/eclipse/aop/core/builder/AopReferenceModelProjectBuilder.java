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
package org.springframework.ide.eclipse.aop.core.builder;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.model.builder.AopReferenceModelBuilder;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelMarkerUtils;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelUtils;
import org.springframework.ide.eclipse.core.project.IProjectBuilder;

/**
 * {@link IProjectBuilder} that triggers creation of Spring IDE's internal AOP
 * reference model
 * @author Christian Dupuis
 * @since 2.0
 */
public class AopReferenceModelProjectBuilder implements IProjectBuilder {

	public void build(IResource resource, int kind, IProgressMonitor monitor) {
		try {
			if (resource instanceof IFile) {
				Set<IFile> filesToBuild = AopReferenceModelUtils
						.getAffectedFiles(kind, (IFile) resource);
				if (filesToBuild != null && filesToBuild.size() > 0) {
					monitor.subTask(Activator.getFormattedMessage(
							"AopReferenceModelProjectBuilder.buildingAopReferenceModel",
							resource.getFullPath()));
					IWorkspaceRunnable validator = new AopReferenceModelBuilder(
							filesToBuild);
					IWorkspace workspace = ResourcesPlugin.getWorkspace();
					ISchedulingRule rule = workspace.getRuleFactory()
							.markerRule(workspace.getRoot());
					try {
						workspace.run(validator, rule, IWorkspace.AVOID_UPDATE,
								monitor);
					}
					catch (CoreException e) {
						Activator.log(e);
					}
				}
			}
		}
		finally {
			monitor.done();
		}
	}

	public void cleanup(IResource resource, IProgressMonitor monitor) {
		try {
			monitor.subTask(Activator.getFormattedMessage(
					"AopReferenceModelProjectBuilder.deletedProblemMarkers",
					resource.getFullPath()));
			AopReferenceModelMarkerUtils.deleteProblemMarkers(resource);
		}
		finally {
			monitor.done();
		}
	}
}
