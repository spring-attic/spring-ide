/*
 * Copyright 2011 SpringSource, a division of VMware, Inc
 * 
 * andrew - Initial API and implementation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
