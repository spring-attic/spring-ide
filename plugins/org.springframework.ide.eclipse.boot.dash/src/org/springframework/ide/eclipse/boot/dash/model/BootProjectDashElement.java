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

/**
 * Concrete BootDashElement that wraps an IProject
 */
public class BootProjectDashElement extends WrappingBootDashElement<IProject> {

	public BootProjectDashElement(IProject project) {
		super(project);
	}

	public IProject getProject() {
		return delegate;
	}

	@Override
	public IJavaProject getJavaProject() {
		return JavaCore.create(getProject());
	}

}
