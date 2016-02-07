/*******************************************************************************
 * Copyright (c) 2013-2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.springsource.ide.eclipse.commons.internal.core.CorePlugin;

/**
 * @author Kris De Volder
 */
public class BootPropertyTester extends PropertyTester {

	private static final Pattern JAR_VERSION_REGEXP = Pattern.compile(".*\\-([^.]+\\.[^.]+\\.[^.]+\\.[^.]+)\\.jar");

	public BootPropertyTester() {
		// TODO Auto-generated constructor stub
	}

	//@Override
	public boolean test(Object rsrc, String property, Object[] args, Object expectedValue) {
		if (expectedValue==null) {
			expectedValue = true;
		}
//		System.out.println(">>> BootPropertyTester");
//		System.out.println(" rsrc = "+rsrc);
//		System.out.println(" property = "+property);
//		System.out.println(" expectedValue = "+expectedValue);
		if (rsrc instanceof IProject && "isBootProject".equals(property)) {
			return expectedValue.equals(isBootProject((IProject)rsrc));
		}
		if (rsrc instanceof IResource && "isBootResource".equals(property)) {
			return expectedValue.equals(isBootResource((IResource) rsrc));
		}
		return false;
	}

	public static boolean isDevtoolsJar(IClasspathEntry e) {
		if (e.getEntryKind()==IClasspathEntry.CPE_LIBRARY) {
			IPath path = e.getPath();
			String name = path.lastSegment();
			return name.endsWith(".jar") && name.startsWith("spring-boot-devtools");
		}
		return false;
	}


	public static boolean hasDevtools(IProject p) {
		try {
			if (p!=null) {
				IJavaProject jp = JavaCore.create(p);
				IClasspathEntry[] classpath = jp.getResolvedClasspath(true);
				if (classpath!=null) {
					for (IClasspathEntry e : classpath) {
						if (BootPropertyTester.isDevtoolsJar(e)) {
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			BootActivator.log(e);
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
				//Look for a 'spring-boot' jar or project entry
				if (!isExcludedProject(project)) {
					for (IClasspathEntry e : classpath) {
						if (isBootJar(e) || isBootProject(e)) {
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			CorePlugin.log(e);
		}
		return false;
	}

	private static boolean isExcludedProject(IProject project) {
		Pattern exclusion = BootPreferences.getInstance().getProjectExclusion();
		return exclusion.matcher(project.getName()).matches();
	}

	/**
	 * @return whether given resource is either Spring Boot IProject or nested inside one.
	 */
	public static boolean isBootResource(IResource rsrc) {
		if (rsrc==null || ! rsrc.isAccessible()) {
			return false;
		}
		boolean result = isBootProject(rsrc.getProject());
		return result;
	}

	public static boolean isBootProject(IClasspathEntry e) {
		if (e.getEntryKind()==IClasspathEntry.CPE_PROJECT) {
			IPath path = e.getPath();
			String name = path.lastSegment();
			return name.startsWith("spring-boot");
		}
		return false;
	}

	public static boolean isBootJar(IClasspathEntry e) {
		if (e.getEntryKind()==IClasspathEntry.CPE_LIBRARY) {
			IPath path = e.getPath();
			String name = path.lastSegment();
			return name.endsWith(".jar") && name.startsWith("spring-boot");
		}
		return false;
	}

	/**
	 * Attempt to determine spring-boot version from project's classpath, in a form that
	 * is easy to compare to version ranges. May return null if the version couldn't be
	 * determined.
	 */
	public static Version getBootVersion(IProject p) {
		try {
			if (p.isAccessible() && p.hasNature(JavaCore.NATURE_ID)) {
				IJavaProject jp = JavaCore.create(p);
				IClasspathEntry[] cp = jp.getResolvedClasspath(true);
				for (IClasspathEntry e : cp) {
					if (isBootJar(e)) {
						String version = getJarVersion(e);
						if (version!=null) {
							return new Version(version);
						}
					}
				}
			}
		} catch (Exception error) {
			BootActivator.log(error);
		}
		return null;
	}


	private static String getJarVersion(IClasspathEntry e) {
		String name = e.getPath().lastSegment();
		//Example: spring-boot-starter-web-1.2.3.RELEASE.jar

		//Pattern regexp = Pattern.compile(".*\\-([^.]+\\.[^.]+\\.[^.]+\\.[^.]+)\\.jar");
		Pattern regexp = JAR_VERSION_REGEXP;

		Matcher matcher = regexp.matcher(name);
		if (matcher.matches()) {
			String versionStr = matcher.group(1);
			return versionStr;
		}
		return null;
	}

	public static boolean supportsLifeCycleManagement(IProject project) {
		Version version = BootPropertyTester.getBootVersion(project);
		if (version!=null) {
			return new VersionRange("1.3.0").includes(version);
		}
		return false;
	}

}
