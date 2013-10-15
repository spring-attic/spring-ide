/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.springsource.ide.eclipse.commons.internal.core.CorePlugin;

public class BootPropertyTester extends PropertyTester {
	
	private static final boolean DEBUG = (""+Platform.getLocation()).contains("kdvolder");

	private static void debug(String string) {
		if (DEBUG) {
			System.out.println(string);
		}
	}


	public BootPropertyTester() {
		// TODO Auto-generated constructor stub
	}

	//@Override
	public boolean test(Object rsrc, String property, Object[] args, Object expectedValue) {
		if (rsrc instanceof IProject && "isBootProject".equals(property)) {
			return expectedValue.equals(isBootProject((IProject)rsrc));
		}
		if (rsrc instanceof IResource && "isBootResource".equals(property)) {
			return expectedValue.equals(isBootResource((IResource) rsrc));
		}
		return false;
	}

	public static boolean isBootProject(IProject project) {
		if (project==null || ! project.isAccessible()) {
			return false;
		}
		try {
			if (project.hasNature(JavaCore.NATURE_ID)) {
				IJavaProject jp = JavaCore.create(project);
				IClasspathEntry[] classpath = jp.getResolvedClasspath(true);
				//Look for a 'spring-boot' jar entry
				for (IClasspathEntry e : classpath) {
					if (isBootJar(e)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			CorePlugin.log(e);
		}
		return false;
	}

	/**
	 * @return whether given resource is either Spring Boot IProject or nested inside one.
	 */
	public static boolean isBootResource(IResource rsrc) {
//		debug("isBootResource: "+rsrc.getName());
		if (rsrc==null || ! rsrc.isAccessible()) {
//			debug("isBootResource => false");
			return false;
		}
		boolean result = isBootProject(rsrc.getProject());
//		debug("isBootResource => "+result);
		return result;
	}


	private static boolean isBootJar(IClasspathEntry e) {
		if (e.getEntryKind()==IClasspathEntry.CPE_LIBRARY) {
			IPath path = e.getPath();
			String name = path.lastSegment();
			return name.endsWith(".jar") && name.startsWith("spring-boot");
		}
		return false;
	}

}
