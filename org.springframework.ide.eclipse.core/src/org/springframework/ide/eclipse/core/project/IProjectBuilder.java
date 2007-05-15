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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.core.internal.project.SpringProjectBuilder;

/**
 * This interface defines the contract for the
 * <code>org.springframework.ide.eclipse.core.builders</code> extension point.
 * <p>
 * All builders must implement this interface according to the following
 * guidelines:
 * <ul>
 * <li>must supply a public, no-argument constructor</li>
 * <li>may implement other methods</li>
 * </ul>
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public interface IProjectBuilder {

	/**
	 * This method is the main entry point to the builder called by the
	 * {@link SpringProjectBuilder}.
	 * @param project the project this builder is started for
	 * @param kind the kind of build being requested. Valid values are
	 * <ul>
	 * <li>{@link IncrementalProjectBuilder.FULL_BUILD} - indicates a full
	 * build.</li>
	 * <li>{@link IncrementalProjectBuilder.INCREMENTAL_BUILD}Ê- indicates an
	 * incremental build.</li>
	 * <li>{@link IncrementalProjectBuilder.AUTO_BUILD}Ê- indicates an
	 * automatically triggered incremental build (autobuilding on).</li>
	 * </ul>
	 * @param delta the resource delta recording the changes since the last time
	 * 			this builder was run or <code>null</code> for a full build
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 * 			reporting and cancellation are not desired
	 */
	void build(IProject project, int kind, IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException;
	
	/**
	 * Cleanup the contributions (e.g. problem markers) created by this builder
	 * from a given resource.
	 * @param resource  the resource the contributions should be removed from
	 * @param monitor  a progress monitor, or <code>null</code> if progress
	 * 			reporting and cancellation are not desired
	 */
	void cleanup(IResource resource, IProgressMonitor monitor)
			throws CoreException;
}
