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

import org.eclipse.core.resources.IResource;
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
 */
public interface IProjectBuilder {

	/**
	 * For every modified file within a Spring project this method is called.
	 * @param file  the modified {@link IResource}
	 * @param kind  the kind of build being requested. Valid values are
	 * <ul>
	 * <li><code>FULL_BUILD</code>- indicates a full build.</li>
	 * <li><code>INCREMENTAL_BUILD</code>- indicates an incremental build.</li>
	 * <li><code>AUTO_BUILD</code>- indicates an automatically triggered
	 * incremental build (autobuilding on).</li>
	 * </ul>
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 * reporting and cancellation are not desired
	 */
	void build(IResource resource, int kind, IProgressMonitor monitor);

	/**
	 * Clean up the created UI contributions.
	 * @param resource the resource
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 * reporting and cancellation are not desired
	 */
	void cleanup(IResource resource, IProgressMonitor monitor);
}
