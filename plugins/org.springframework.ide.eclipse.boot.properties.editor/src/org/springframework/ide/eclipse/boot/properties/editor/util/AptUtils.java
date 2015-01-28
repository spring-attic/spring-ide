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
package org.springframework.ide.eclipse.boot.properties.editor.util;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.core.util.IFactoryPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.ide.eclipse.boot.properties.editor.SpringPropertiesEditorPlugin;
import org.springframework.ide.eclipse.boot.util.JavaProjectUtil;

/**
 * @author Kris De Volder
 */
public class AptUtils {

	/**
	 * Enable's JDT APT on a JavaProject. Note: if the project's classpath contains
	 * no APT services in jar-dependencies then this does nothing.
	 */
	public static void enableApt(IJavaProject jp) {
		boolean shouldEnable = false; //becomes true if we find at least one annotation processor.
		try {
			IFactoryPath factoryPath = AptConfig.getDefaultFactoryPath(jp);
			for (File jarFile : JavaProjectUtil.getNonSystemJarDependencies(jp, true)) {
				if (!AnnotationServiceLocator.getAptServiceEntries(jarFile).isEmpty()) {
					shouldEnable = true;
				}
				IPath absolutePath = new Path(jarFile.getAbsolutePath());
				IPath variablePath = useClasspathVariable(absolutePath);
				if (variablePath!=null) {
					factoryPath.addVarJar(variablePath);
				} else {
					factoryPath.addExternalJar(jarFile);
				}
			}
			if (shouldEnable) {
				AptConfig.setEnabled(jp, true);
				AptConfig.setFactoryPath(jp, factoryPath);
			}
		} catch (Exception e) {
			SpringPropertiesEditorPlugin.log(e);
		}
	}

	public static boolean isAptEnabled(IJavaProject jp) {
		return AptConfig.isEnabled(jp);
	}

	
	/**
	 * Attempt to use a classpath variable to make given absolutePath relative (this
	 * is nicer for users because the paths end up getting stored in project settings in
	 * the workspace and absolute paths are not 'portable' so is awkward to share
	 * with other users via SCM. 
	 * 
	 * @return An equivalent path using classpath variable name as its first segment or null if
	 *  no classpath variable is a prefix of the absolutePath.
	 */
	private static IPath useClasspathVariable(IPath absolutePath) {
		//Start by finding the 'best' classpath variable.
		// This variable should be a 'prefix' of given absolutePath
		// in case more than one such variable exists we prefer the 'longest' path.
		String bestVar = null;
		int bestLen = 0;

		for (String var : JavaCore.getClasspathVariableNames()) {
			IPath varPath = JavaCore.getClasspathVariable(var);
			if (varPath!=null && varPath.segmentCount()>bestLen && varPath.isPrefixOf(absolutePath)) {
				bestVar = var;
				bestLen = varPath.segmentCount();
			}
		}

		// Make path relative to 'bestVar'
		if (bestVar!=null) {
			IPath varPath = JavaCore.getClasspathVariable(bestVar);
			IPath relativePath = absolutePath.removeFirstSegments(varPath.segmentCount());
			return new Path(bestVar).append(relativePath);
		}
		return null;
	}

}
