/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.project;

import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This interface defines the contract for contributing artifacts, e.g. problem markers, to a Spring
 * project.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public interface IProjectContributor {

	/**
	 * Returns a list of resources which may be affected by contributions if the given resource is
	 * modified, e.g. for a modified bean class file all beans config files which are referencing
	 * this bean class should be validated.
	 * @param resource the resource the corresponding affected resources should be evaluated
	 * @param kind the kind of build (<code>0</code>,
	 * {@link IncrementalProjectBuilder#FULL_BUILD}, {@link IncrementalProjectBuilder#CLEAN_BUILD}
	 * or {@link IncrementalProjectBuilder#INCREMENTAL_BUILD})
	 * @param deltaKind the kind of modification (<code>0</code>, {@link IResourceDelta#ADDED},
	 * {@link IResourceDelta#CHANGED} or {@link IResourceDelta#REMOVED})
	 */
	Set<IResource> getAffectedResources(IResource resource, int kind, int deltaKind)
			throws CoreException;

	/**
	 * Cleanup the contributions (e.g. problem markers) created for the given resource.
	 * @param resource the resource the contributions should be removed from
	 * @param monitor a progress monitor, or <code>null</code> if progress reporting and
	 * cancellation are not desired
	 */
	void cleanup(IResource resource, IProgressMonitor monitor) throws CoreException;
}
