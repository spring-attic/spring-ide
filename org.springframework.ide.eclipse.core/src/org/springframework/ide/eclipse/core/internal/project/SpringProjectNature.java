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
package org.springframework.ide.eclipse.core.internal.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

/**
 * This class defines the project nature with the corresponding incremental
 * builders for Spring projects.
 * @author Torsten Juergeleit
 */
public class SpringProjectNature implements IProjectNature {

	public static final String DEBUG_OPTION = SpringCore.PLUGIN_ID +
													   "/project/nature/debug";
	public static boolean DEBUG = SpringCore.isDebug(DEBUG_OPTION);

	private IProject project;

	/**
	 * Constructor needed for <code>IProject.getNature()</code> and
	 * <code>IProject.addNature()</code>.
	 *
	 * @see #setProject(IProject)
	 */
	public SpringProjectNature() {
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	public IProject getProject() {
		return project;
	}

	/**
	 * Adds Spring builder to project's list of external builders.
	 */
	public void configure() throws CoreException {
		if (DEBUG) {
			System.out.println("configuring Spring project nature");
		}
		IProject project = getProject();
		IProgressMonitor monitor = new NullProgressMonitor();
		SpringCoreUtils.addProjectBuilder(project, SpringCore.BUILDER_ID,
				monitor);
	}

	/**
	 * Removes Spring beans valdiator from project's list of external builders.
	 */
	public void deconfigure() throws CoreException {
		if (DEBUG) {
			System.out.println("deconfiguring Spring project nature");
		}
		IProject project = getProject();
		IProgressMonitor monitor = new NullProgressMonitor();
		SpringCoreUtils.removeProjectBuilder(project, SpringCore.BUILDER_ID,
				monitor);
	}
}
