/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.model.locate;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * {@link AbstractPathMatchingBeansConfigLocator} extension that works on {@link IJavaProject} and
 * threats every source folder as potential input to path matching.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public abstract class AbstractJavaProjectPathMatchingBeansConfigLocator extends
		AbstractPathMatchingBeansConfigLocator {
	
	/**
	 * Returns <code>true</code> only if the given project is a {@link IJavaProject}.
	 */
	public boolean supports(IProject project) {
		return JdtUtils.isJavaProject(project);
	}
	
	/**
	 * Returns <code>true</code> only if the given project is a {@link IJavaProject}.
	 */
	@Override
	protected boolean canLocateInProject(IProject project) {
		return supports(project);
	}
	
	/**
	 * Returns every source path of the {@link IJavaProject} as a potential root dir.
	 */
	@Override
	protected Set<IPath> getRootDirectories(IProject project) {
		Set<IPath> rootDirectories = new LinkedHashSet<IPath>();
		IJavaProject javaProject = JdtUtils.getJavaProject(project);
		if (javaProject != null) {
			try {
				for (IClasspathEntry entry : javaProject.getResolvedClasspath(true)) {
					if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
						rootDirectories.add(entry.getPath());
					}
				}
			}
			catch (JavaModelException e) {
				BeansCorePlugin.log(e);
			}
		}
		return rootDirectories;
	}

}
