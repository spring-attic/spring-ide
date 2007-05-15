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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigValidator;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.project.AbstractProjectBuilder;

/**
 * @author Torsten Juergeleit
 */
public class BeansProjectValidator extends AbstractProjectBuilder {

	@Override
	protected Set<IResource> getAffectedResources(IResource resource,
			int kind) {
		Set<IResource> resources = new LinkedHashSet<IResource>();
		if (resource instanceof IFile) {

			// First check for a beans config file
			IBeansConfig config = BeansCorePlugin.getModel().getConfig(
					(IFile) resource);
			if (config != null) {
				resources.add(resource);
			}
			else {

				// Now check for a bean class or source file
				try {
					IJavaElement element = JavaCore.create(resource);
					if (element != null) {
						if (element instanceof IClassFile) {
							IType type = ((IClassFile) element).getType();
							resources.addAll(getBeanConfigResources(type));
						}
						else if (element instanceof ICompilationUnit) {
							for (IType type : ((ICompilationUnit) element)
									.getTypes()) {
								resources.addAll(getBeanConfigResources(type));
							}
						}
					}
				}
				catch (JavaModelException e) {
					BeansCorePlugin.log(e);
				}
			}
		}
		return resources;
	}

	private List<IResource> getBeanConfigResources(IType beanClass) {
		List<IResource> resources = new ArrayList<IResource>();
		String className = beanClass.getFullyQualifiedName();
		for (IBeansConfig config : BeansCorePlugin.getModel().getConfigs(
				className)) {
			resources.add(config.getElementResource());
		}
		return resources;
	}

	@Override
	protected void build(Set<IResource> affectedResources, int kind,
			IProgressMonitor monitor) throws CoreException {
		for (IResource resource : affectedResources) {
			if (resource instanceof IFile) {
				validate((IFile) resource, monitor);
			}
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

	protected void validate(IFile configFile, IProgressMonitor monitor) {
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
}
