/*******************************************************************************
 * Copyright (c) 2005, 2012 Spring IDE Developers
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
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

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
public interface IProjectBuilder extends IProjectContributor {

	/**
	 * Builds all the given affected resources.
	 * @param affectedResources  the resource affected by this build
	 * @param kind  the kind of build being requested. Valid values are
	 * <ul>
	 * <li>{@link IncrementalProjectBuilder.FULL_BUILD} - indicates a full
	 * build.</li>
	 * <li>{@link IncrementalProjectBuilder.INCREMENTAL_BUILD} - indicates an
	 * incremental build.</li>
	 * <li>{@link IncrementalProjectBuilder.AUTO_BUILD} - indicates an
	 * automatically triggered incremental build (autobuilding on).</li>
	 * </ul>
	 * @param monitor  a progress monitor, or <code>null</code> if progress
	 * 			reporting and cancellation are not desired
	 */
	void build(Set<IResource> affectedResources, int kind,
			IProgressMonitor monitor) throws CoreException;
}
