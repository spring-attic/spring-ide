/*******************************************************************************
 * Copyright (c) 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.io.ExternalFile;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class BeansConfigFactory {
	
	public static final String JAVA_CONFIG_TYPE = "java:";

	public static IBeansConfig create(IBeansProject project, String name, IBeansConfig.Type type) {
		if (name != null && name.startsWith(JAVA_CONFIG_TYPE)) {
			String className = name.substring(JAVA_CONFIG_TYPE.length());
			IJavaProject javaProject = JdtUtils.getJavaProject(project.getProject());

			try {
				IType configClass = javaProject.findType(className);
				return new BeansJavaConfig(project, configClass, className, IBeansConfig.Type.MANUAL);
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
		else {
			if (name.length() > 0 && name.charAt(0) == '/') {
				String projectPath = '/' + project.getElementName() + '/';
				if (name.startsWith(projectPath)) {
					name = name.substring(projectPath.length());
				}
			}
			return new BeansConfig(project, name, IBeansConfig.Type.MANUAL);
		}
		return null;
	}
	
	public static String getConfigName(IFile file, IProject project) {
		String configName;
	
		if (!"xml".equals(file.getFileExtension())) {
			IJavaProject javaProject = JdtUtils.getJavaProject(project.getProject());
			if (javaProject != null) {
				IJavaElement element = JavaCore.create(file, javaProject);
				if (element != null && element.getPrimaryElement() instanceof ICompilationUnit) {
					IType[] types;
					try {
						types = ((ICompilationUnit) element.getPrimaryElement()).getTypes();
						if (types != null && types.length > 0) {
							return JAVA_CONFIG_TYPE + types[0].getFullyQualifiedName();
						}
					} catch (JavaModelException e) {
					}
				}
			}
		}

		if (file.getProject().equals(project.getProject()) && !(file instanceof ExternalFile)) {
			configName = file.getProjectRelativePath().toString();
		}
		else {
			configName = file.getFullPath().toString();
		}
		return configName;
	}

}
