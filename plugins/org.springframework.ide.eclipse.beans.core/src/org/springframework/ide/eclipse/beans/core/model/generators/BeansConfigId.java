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

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JarEntryFile;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.util.Util;
import org.springframework.core.io.Resource;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * 
 * @author Andrew Eisenberg
 * @since 3.4.0
 */
public class BeansConfigId {
    
    public final String name;
    public final String kind;
    public final String project;
    
    private BeansConfigId(String kind, String project, String name) {
        super();
        Assert.isNotNull(kind, "Bean kind must not be null");
        Assert.isNotNull(name, "Bean name must not be null");
        this.kind = kind;
        this.name = name;
        if (project == null && kind.equals("xml")) {
            int configNamePos = name.indexOf('/', (name.charAt(0) == '/' ? 1 : 0));
            if (configNamePos > 0) {
                project = name.substring(1, configNamePos);
            }
        }
        Assert.isNotNull(project, "Bean project must not be null");
        this.project = project;
    }
    
    @Override
    public String toString() {
        if (kind.equals(XMLConfigGenerator.XML_CONFIG_KIND)) {
            if (name.startsWith("/" + project + "/")) {
                return name.substring(("/" + project + "/").length());
            }
            return name;
        }
        return kind + ":" + name;
    }
    
    public BeansConfigId newName(String newName) {
        return new BeansConfigId(this.kind, this.project, newName);
    }

    public BeansConfigId newProject(String newProject) {
        return new BeansConfigId(this.kind, newProject, this.name);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + kind.hashCode();
        result = prime * result + name.hashCode();
        result = prime * result + project.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BeansConfigId other = (BeansConfigId) obj;
        if (!kind.equals(other.kind)) {
            return false;
        }
        if (!name.equals(other.name)) {
            return false;
        }
        if (!project.equals(other.project)) {
            return false;
        }
        return true;
    }

    public static String getConfigKind(IFile file) {
        return file.getFileExtension();
    }

    public static BeansConfigId parse(String idString, IProject project) {
    	if (idString.startsWith(JavaConfigGenerator.JAVA_CONFIG_KIND + ":")) {
    		return new BeansConfigId(JavaConfigGenerator.JAVA_CONFIG_KIND, project.getName(), idString.substring((JavaConfigGenerator.JAVA_CONFIG_KIND + ":").length()));
    	}
    	String newName;
    	if (idString.startsWith(XMLConfigGenerator.XML_CONFIG_KIND + ":")) {
    		newName = idString.substring((XMLConfigGenerator.XML_CONFIG_KIND + ":").length());
    	} else {
    		newName = idString;
    	}
    	if (newName.charAt(0) != '/') {
    		newName = "/" + project.getName() + "/" + newName;
    	}
    	return new BeansConfigId(XMLConfigGenerator.XML_CONFIG_KIND, project.getName(), newName);
    }

    public static BeansConfigId create(IFile file) {
        return create(file, file.getProject());
    }

    public static BeansConfigId create(Object obj, IProject project) {
        if (obj instanceof IType) {
            IType type = (IType) obj;
            return new BeansConfigId(JavaConfigGenerator.JAVA_CONFIG_KIND, project.getName(), type.getFullyQualifiedName('$'));
    
        } else if (obj instanceof ZipEntryStorage) {
            ZipEntryStorage entry = (ZipEntryStorage) obj;
            return new BeansConfigId(XMLConfigGenerator.XML_CONFIG_KIND, project.getName(), entry.getFullName());
        } else if (obj instanceof JarEntryFile) {
            JarEntryFile jarEntry = (JarEntryFile) obj;
            IPath fullPath = ((JarPackageFragmentRoot) jarEntry.getPackageFragmentRoot())
                    .getPath();
            String entryName = jarEntry.getFullPath().toString();
            for (String name : JavaCore.getClasspathVariableNames()) {
                IPath variablePath = JavaCore.getClasspathVariable(name);
                if (variablePath != null && variablePath.isPrefixOf(fullPath)) {
                    fullPath = new Path(name).append(fullPath.removeFirstSegments(variablePath
                            .segmentCount()));
                    break;
                }
            }
            String path = IBeansConfig.EXTERNAL_FILE_NAME_PREFIX + fullPath.toString()
                    + ZipEntryStorage.DELIMITER + entryName;
            return new BeansConfigId(XMLConfigGenerator.XML_CONFIG_KIND, project.getName(), path);
        } else if (obj instanceof String) {
            return BeansConfigId.parse((String) obj, project);
        } else if (obj instanceof Resource) {
            try {
                return BeansConfigId.parse(((Resource) obj).getFile().getCanonicalPath(), project);
            } catch (IOException e) {
                throw new RuntimeException(obj.toString() + " does not exist");
            }
        } else if (obj instanceof IFile) {
            IFile file = (IFile) obj;
            if (project == null) {
                project = file.getProject();
            }
        
            if (!XMLConfigGenerator.XML_CONFIG_KIND.equals(file.getFileExtension()) && Util.isJavaLikeFileName(file.getName())) {
                IJavaProject javaProject = JdtUtils.getJavaProject(project);
                if (javaProject != null) {
                    IJavaElement element = JavaCore.create(file, javaProject);
                    if (element != null && element.getPrimaryElement() instanceof ICompilationUnit && element.exists()) {
                        String typeName = element.getElementName();
                        String fileExtension = file.getFileExtension();
                        if (fileExtension != null && fileExtension.length() > 0) {
                            typeName = typeName.substring(0, typeName.length() - (fileExtension.length() + 1));
                        }
                        
                        IJavaElement parent = element.getParent();
                        String packageName = "";
                        if (parent.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
                            IPackageFragment packageFragment = (IPackageFragment) parent;
                            if (!packageFragment.isDefaultPackage()) {
                                packageName = packageFragment.getElementName() + ".";
                            }
                            
                            return new BeansConfigId(file.getFileExtension(), project.getName(), packageName + typeName);
                        }
                    } else {
                        // element not on project's classpath
                        return null;
                    }
                }
            }
            return new BeansConfigId(file.getFileExtension(), project.getName(), file.getFullPath().toString());
        } else if (obj instanceof IResource) {
            return null;
        }
        
        if (obj == null) {
            return null;
        }
        // maybe best to just return null here.
        throw new IllegalArgumentException("Attempt to create a BeansConfigId from an unexpected type: " + obj.getClass() + " toString: "  + obj);
    }
}
