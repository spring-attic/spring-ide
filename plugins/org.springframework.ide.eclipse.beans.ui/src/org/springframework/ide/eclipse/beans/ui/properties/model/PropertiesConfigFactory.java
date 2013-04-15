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
package org.springframework.ide.eclipse.beans.ui.properties.model;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
public class PropertiesConfigFactory {
	
	public static final String JAVA_CONFIG_TYPE = "java:";

	public static IBeansConfig create(PropertiesProject project, String name, IBeansConfig.Type type) {
		if (name != null && name.startsWith(JAVA_CONFIG_TYPE)) {
			String className = name.substring(JAVA_CONFIG_TYPE.length());
			IJavaProject javaProject = JdtUtils.getJavaProject(project.getProject());

			try {
				IType configClass = javaProject.findType(className);
				if (configClass != null) {
					return new PropertiesJavaConfig(project, configClass, IBeansConfig.Type.MANUAL);
				}
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
				
			return new PropertiesConfig(project, name, IBeansConfig.Type.MANUAL);
		}
		
		return null;
	}

	public static IBeansConfig create(PropertiesConfigSet set, String name, IBeansConfig.Type type) {
		if (name != null && name.startsWith(JAVA_CONFIG_TYPE)) {
			String className = name.substring(JAVA_CONFIG_TYPE.length());
			IBeansProject beansProject = (IBeansProject) set.getElementParent();
			IJavaProject javaProject = JdtUtils.getJavaProject(beansProject.getProject());

			try {
				IType configClass = javaProject.findType(className);
				if (configClass != null) {
					return new PropertiesJavaConfig(set, configClass, IBeansConfig.Type.MANUAL);
				}
			} catch (JavaModelException e) {
				e.printStackTrace();
			}

		}
		else {
//			if (name.length() > 0 && name.charAt(0) == '/') {
//				String projectPath = '/' + project.getElementName() + '/';
//				if (name.startsWith(projectPath)) {
//					name = name.substring(projectPath.length());
//				}
//			}
				
			return new PropertiesConfig(set, name, IBeansConfig.Type.MANUAL);
		}
		
		return null;
	}

}
