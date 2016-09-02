/*******************************************************************************
 * Copyright (c) 2013, 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrService;
import org.springframework.ide.eclipse.boot.core.internal.MavenSpringBootProject;
import org.springsource.ide.eclipse.commons.core.preferences.StsProperties;

public class SpringBootCore {

	public static final String M2E_NATURE = "org.eclipse.m2e.core.maven2Nature";
	private static final StsProperties stsProps = StsProperties.getInstance();
	private InitializrService initializr;

	public SpringBootCore(InitializrService initializr) {
		this.initializr = initializr;
	}

	/**
	 * Deprecated, use of this method hampers testability. Instead 'good' code
	 * should use a SpringBootCore instance from its context to create a project.
	 *
	 * @return a SpringBoot centric view on a eclipse project.
	 */
	@Deprecated
	public static ISpringBootProject create(IProject project) throws CoreException {
		return getDefault().project(project);
	}

	/**
	 * Deprecated, use of this method hampers testability. Instead 'good' code
	 * should use a SpringBootCore instance from its context to create a project.
	 *
	 * @return a SpringBoot centric view on a eclipse project.
	 */
	@Deprecated
	public static ISpringBootProject create(IJavaProject project) throws CoreException {
		return getDefault().project(project);
	}

	private static SpringBootCore instance;

	/**
	 * Gets the default instance. Callers should be careful calling this method as this
	 * makes it hard to test the calling code (i.e. dependency injection of mocks).
	 * <p>
	 * Generally callers should instead seek to obtain a reference to a {@link SpringBootCore}
	 * instance from their context.
	 */
	public static SpringBootCore getDefault() {
		if (instance==null) {
			instance = new SpringBootCore(InitializrService.CACHING);
		}
		return instance;
	}

	/**
	 * @return a SpringBoot centric view on a eclipse project.
	 */
	public ISpringBootProject project(IJavaProject project) throws CoreException {
		return project(project.getProject());
	}

	/**
	 * @return a SpringBoot centric view on a eclipse project.
	 */
	public ISpringBootProject project(IProject project) throws CoreException {
		if (project.hasNature(M2E_NATURE)) {
			return new MavenSpringBootProject(project, initializr);
		} else {
			return null;
		}
	}

	/**
	 * Normally we determine the version of spring boot used by a project based on its
	 * classpath. But spring-boot jar is not yet on the classpath we use the defaultVersion
	 */
	public static String getDefaultBootVersion() {
		return stsProps.get("spring.boot.default.version");
	}

}
