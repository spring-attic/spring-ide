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
import org.springframework.ide.eclipse.boot.core.internal.MavenSpringBootProject;

public class SpringBootCore {

	/**
	 * @return a SpringBoot centric view on a eclipse project.
	 */
	public static ISpringBootProject create(IProject project) throws CoreException {
		return new MavenSpringBootProject(project);
	}
	
}
