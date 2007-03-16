/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.core.project;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This interface defines the contract for the
 * <code>org.springframework.ide.eclipse.core.builders</code>  extension point.
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
	 * @param file  modified file
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 * reporting and cancellation are not desired
	 */
	void build(IFile file, IProgressMonitor monitor);
	
	/**
	 * Clean up the created UI contributions
	 * @param resource the resource
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 * reporting and cancellation are not desired
	 */
	void cleanup(IResource resource, IProgressMonitor monitor);
}
