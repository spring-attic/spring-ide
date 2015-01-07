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
package org.springframework.ide.eclipse.propertiesfileeditor.util;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.JavaRuntime;
import org.springframework.ide.eclipse.propertiesfileeditor.SpringPropertiesEditorPlugin;

public class JavaProjectUtil {

	public static List<File> getNonSystemJarDependencies(IJavaProject jp, boolean reverse) {
		try {
			String[] paths = JavaRuntime.computeDefaultRuntimeClassPath(jp);
			if (paths!=null && paths.length>0) {
				LinkedList<File> jars = new LinkedList<File>();
				for (String path : paths) {
					if (path!=null) {
						File jar = new File(path);
						if (FileUtil.isJarFile(jar)) {
							if (reverse) {
								jars.addFirst(jar);
							} else {
								jars.add(jar);
							}
						}
					}
				}
				return jars;
			}
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
		return Collections.emptyList();
	}

	/**
	 * Get IFile handle relative to project's default output folder.
	 */
	public static IFile getOutputFile(IJavaProject jp, String relativePath) {
		return getOutputFile(jp, new Path(relativePath));
	}

	public static IFile getOutputFile(IJavaProject jp, IPath relativePath) {
		try {
			IPath loc = jp.getOutputLocation().append(relativePath);
			String pname = loc.segment(0);
			return ResourcesPlugin.getWorkspace().getRoot().getProject(pname).getFile(loc.removeFirstSegments(1));
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
		return null;
	}

}
