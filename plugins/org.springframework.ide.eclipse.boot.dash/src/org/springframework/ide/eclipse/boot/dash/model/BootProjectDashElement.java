/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.ide.eclipse.boot.dash.util.ProjectRunStateTracker;

/**
 * Concrete BootDashElement that wraps an IProject
 */
public class BootProjectDashElement extends WrappingBootDashElement<IProject> {

	private ProjectRunStateTracker runStateTracker;

	public BootProjectDashElement(IProject project, ProjectRunStateTracker runStateTracker) {
		super(project);
		this.runStateTracker = runStateTracker;
	}

	public IProject getProject() {
		return delegate;
	}

	@Override
	public IJavaProject getJavaProject() {
		return JavaCore.create(getProject());
	}

	@Override
	public RunState getRunState() {
		return runStateTracker.getState(getProject());
	}

}
