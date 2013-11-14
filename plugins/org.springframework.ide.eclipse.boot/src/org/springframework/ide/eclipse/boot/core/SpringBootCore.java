/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.boot.core.internal.MavenSpringBootProject;
import org.springsource.ide.eclipse.commons.core.util.ExceptionUtil;

public class SpringBootCore {

	private static final String M2E_NATURE = "org.eclipse.m2e.core.maven2Nature";

	/**
	 * @return a SpringBoot centric view on a eclipse project.
	 */
	public static ISpringBootProject create(IProject project) throws CoreException {
		if (project.hasNature(M2E_NATURE)) {
			return new MavenSpringBootProject(project);
		} else {
			throw ExceptionUtil.coreException("This feature is only implemented for m2e enabled maven projects");
		}
	}

	public static ISpringBootProject create(IJavaProject project) throws CoreException {
		return create(project.getProject());
	}
	
}
