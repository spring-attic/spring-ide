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
package org.springframework.ide.eclipse.core.project;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

/**
 * @author Torsten Juergeleit
 */
public abstract class AbstractProjectBuilder implements IProjectBuilder {

	public final void build(IProject project, int kind, IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {

		// First retrieve a list of affected resources
		Set<IResource> affectedResources;
		if (delta == null || kind == IncrementalProjectBuilder.FULL_BUILD) {
			ResourceTreeVisitor visitor = new ResourceTreeVisitor();
			project.accept(visitor);
			affectedResources = visitor.getResources();
		}
		else {
			ResourceDeltaVisitor visitor = new ResourceDeltaVisitor();
			delta.accept(visitor);
			affectedResources = visitor.getResources();
		}

		// Finally build the affected resources
		if (!affectedResources.isEmpty()) {
			build(affectedResources, kind, monitor);
		}
	}

	/**
	 * Returns a list of resources which are affected if the given resource is
	 * modified, e.g. for a modified bean class file all beans config files
	 * which are referencing this bean class should be validated.
	 * @param resource  the resource the corresponding affected resources should
	 * 			be evaluated
	 * @param kind  the kind of modification (<code>0</code>,
	 * 			{@link IResourceDelta#accept(IResourceDeltaVisitor) or
	 * 			{@link IResourceDelta#REMOVED})
	 * @param monitor  a progress monitor, or <code>null</code> if progress
	 * 			reporting and cancellation are not desired
	 */
	protected abstract Set<IResource> getAffectedResources(IResource resource,
			int kind);

	/**
	 * Builds all the given affected resources.
	 * <p>
	 * This implementation does nothing.
	 * </p>
	 * @param affectedResources  the resource affected by this build
	 * @param kind  the kind of modification (<code>0</code>,
	 * 			{@link IResourceDelta#accept(IResourceDeltaVisitor) or
	 * 			{@link IResourceDelta#REMOVED})
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 * 			reporting and cancellation are not desired
	 */
	protected void build(Set<IResource> affectedResources, int kind,
			IProgressMonitor monitor) throws CoreException {
		// Do nothing
	}

	/**
	 * Create a list of affected resources from a resource tree.
	 */
	private class ResourceTreeVisitor implements IResourceVisitor {

		private Set<IResource> resources = new LinkedHashSet<IResource>();

		public Set<IResource> getResources() {
			return resources;
		}

		public boolean visit(IResource resource) throws CoreException {
			if (resource instanceof IFile) {
				resources.addAll(getAffectedResources(resource, 0));
			}
			return true;
		}
	}

	/**
	 * Create a list of affected resources from a resource delat.
	 */
	private class ResourceDeltaVisitor implements IResourceDeltaVisitor {

		private Set<IResource> resources = new LinkedHashSet<IResource>();

		public Set<IResource> getResources() {
			return resources;
		}

		public boolean visit(IResourceDelta aDelta) throws CoreException {
			boolean visitChildren = false;

			IResource resource = aDelta.getResource();
			if (resource instanceof IProject) {

				// Only check projects with Spring beans nature
				visitChildren = SpringCoreUtils.isSpringProject(resource);
			}
			else if (resource instanceof IFolder) {
				resources.addAll(getAffectedResources(resource,
						aDelta.getKind()));
				visitChildren = true;
			}
			else if (resource instanceof IFile) {
				switch (aDelta.getKind()) {
				case IResourceDelta.ADDED:
				case IResourceDelta.CHANGED:
					resources.addAll(getAffectedResources(resource,
							aDelta.getKind()));
					visitChildren = true;
					break;

				case IResourceDelta.REMOVED:
					resources.addAll(getAffectedResources(resource,
							aDelta.getKind()));
					break;
				}
			}
			return visitChildren;
		}
	}
}
