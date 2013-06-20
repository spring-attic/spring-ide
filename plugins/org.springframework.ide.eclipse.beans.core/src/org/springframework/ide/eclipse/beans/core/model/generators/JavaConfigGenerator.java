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
package org.springframework.ide.eclipse.beans.core.model.generators;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansJavaConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig.Type;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * 
 * @author Andrew Eisenberg
 * @created Jun 17, 2013
 */
public class JavaConfigGenerator implements IBeansConfigGenerator<BeansJavaConfig> {

    public static final String JAVA_CONFIG_KIND = "java";

    public BeansJavaConfig generateConfig(IBeansProject project, BeansConfigId id, IBeansConfig.Type type) throws CoreException {
        IJavaProject javaProject = JdtUtils.getJavaProject(project.getProject());
        IType configClass = javaProject.findType(id.name);
        return new BeansJavaConfig(project, configClass, id, IBeansConfig.Type.MANUAL);
    }

    public String getConfigKind() {
        return JAVA_CONFIG_KIND;
    }

    public String getConfigName(IFile file, IProject project) {
        if (handlesConfig(file)) {
            try {
                // dangerous: first type may not be the bean
                return JavaCore.createCompilationUnitFrom(file).getTypes()[0].getFullyQualifiedName();
            } catch (JavaModelException e) {
                BeansCorePlugin.log("Error generating bean name for " + file.getFullPath(), e);
            }
        }
        return null;
    }

    public boolean handlesConfig(IFile file) {
        return file.getFileExtension().equals(JAVA_CONFIG_KIND);
    }

    public boolean handlesConfig(BeansConfigId id) {
        return JAVA_CONFIG_KIND.equals(id.name);
    }

    public BeansJavaConfig generateConfig(IBeansConfigSet configSet,
            BeansConfigId id, Type type) throws CoreException {
        IJavaProject javaProject = JdtUtils.getJavaProject(((IBeansProject) configSet.getElementParent()).getProject());
        IType configClass = javaProject.findType(id.name);
        return new BeansJavaConfig(configSet, configClass, id, IBeansConfig.Type.MANUAL);
    }

}
