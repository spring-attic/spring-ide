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

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelMarkerUtils;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelUtils;
import org.springframework.ide.eclipse.core.project.AbstractProjectBuilder;
import org.springframework.ide.eclipse.core.project.IProjectBuilder;

/**
 * {@link IProjectBuilder} that triggers creation of Spring IDE's internal AOP
 * reference model
 * @author Christian Dupuis
 * @since 2.0
 */
public class AopReferenceModelProjectBuilder extends AbstractProjectBuilder {

	protected Set<IResource> getAffectedResources(IResource resource,
			int kind) {
		Set<IResource> resources = new LinkedHashSet<IResource>();
		if (resource instanceof IFile) {
			resources.addAll(AopReferenceModelUtils.getAffectedFiles(kind,
					resource));
		}
		return resources;
	}

	@Override
	protected void build(Set<IResource> affectedResources, int kind,
			IProgressMonitor monitor) throws CoreException {
		try {
			for (IResource resource : affectedResources) {
				if (resource instanceof IFile) {
// FIXME
//						monitor.subTask(Activator.getFormattedMessage(
//								"AopReferenceModelProjectBuilder.buildingAopReferenceModel",
//								resource.getFullPath()));
//						IWorkspaceRunnable validator = new AopReferenceModelBuilder(
//								filesToBuild);
//						IWorkspace workspace = ResourcesPlugin.getWorkspace();
//						ISchedulingRule rule = workspace.getRuleFactory()
//								.markerRule(workspace.getRoot());
//						try {
//							workspace.run(validator, rule, IWorkspace.AVOID_UPDATE,
//									monitor);
//						}
//						catch (CoreException e) {
//							Activator.log(e);
//						}
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
