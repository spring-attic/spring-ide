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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JarEntryFile;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig.Type;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * @author Martin Lippert
 * @author Andrew Eisenberg
 * @since 3.4.0
 */
public class BeansConfigFactory {
    
    private static final String EXTENSION_POINT = BeansCorePlugin.PLUGIN_ID + ".beansconfigkind";
    private static final String CONFIG_GENERATOR = "configGenerator";
    
    private static Set<IBeansConfigGenerator<?>> configGenerators;
	
	private static Set<IBeansConfigGenerator<?>> initGenerators() {
        Set<IBeansConfigGenerator<?>> set = new HashSet<IBeansConfigGenerator<?>>();
        // THIS IS PROBABLY WRONG
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_POINT);
        IExtension[] extensions = extensionPoint.getExtensions();
        for (IExtension ext : extensions) {
            IConfigurationElement[] configElts = ext.getConfigurationElements();
            for (IConfigurationElement elt : configElts) {
                try {
                    IBeansConfigGenerator<?> configGenerator = (IBeansConfigGenerator<?>) elt.createExecutableExtension(CONFIG_GENERATOR);
                    set.add(configGenerator);
                } catch (Exception e) {
                    BeansCorePlugin.log("Error creating bean generator " + elt.getAttribute(CONFIG_GENERATOR) + " ignoring extension point", e);
                }
            }
        }
        return set;
    }

    public static IBeansConfig create(IBeansProject project, BeansConfigId id, IBeansConfig.Type type) {
	    if (configGenerators == null) {
	        configGenerators = initGenerators();
	    }
	    if (project == null ) {
	        project = BeansCorePlugin.getModel().getProject(id.project);
	    }
	    
	    for (IBeansConfigGenerator<?> generator : configGenerators) {
            if (generator.getConfigKind().equals(id.kind)) {
                try {
                    return generator.generateConfig(project, id, type);
                } catch (CoreException e) {
                    BeansCorePlugin.log("Exception generating beans config: " + id, e);
                }
            }
        }
	    return null;
	}
	
    public static IBeansConfig create(IBeansConfigSet configSet, BeansConfigId id, IBeansConfig.Type type) {
        if (configGenerators == null) {
            configGenerators = initGenerators();
        }
        Assert.isNotNull(configSet);
        
        for (IBeansConfigGenerator<?> generator : configGenerators) {
            if (generator.getConfigKind().equals(id.kind)) {
                try {
                    return generator.generateConfig(configSet, id, type);
                } catch (CoreException e) {
                    BeansCorePlugin.log("Exception generating beans config: " + id, e);
                }
            }
        }
        return null;
    }
    
    /**
     * @param configId
     * @param autoDetected
     * @return
     */
    public static IBeansConfig create(BeansConfigId id, Type type) {
        return create((IBeansProject) null, id, type);
    }

    public static String getConfigKind(IFile file) {
	    return file.getFileExtension();
	}
	
    public static BeansConfigId getConfigId(IFile file) {
        return getConfigId(file, null);
    }
	public static BeansConfigId getConfigId(Object obj, IProject project) {
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
	        // TODO assumption is XML config is this valid?
	        return new BeansConfigId(((String) obj).endsWith(XMLConfigGenerator.XML_CONFIG_KIND) ? XMLConfigGenerator.XML_CONFIG_KIND : JavaConfigGenerator.JAVA_CONFIG_KIND, project.getName(), (String) obj);
	    } else if (obj instanceof IFile) {
	        IFile file = (IFile) obj;
	        if (project == null) {
	            project = file.getProject();
	        }
	    
	        if (!XMLConfigGenerator.XML_CONFIG_KIND.equals(file.getFileExtension())) {
	            IJavaProject javaProject = JdtUtils.getJavaProject(project.getProject());
	            if (javaProject != null) {
	                IJavaElement element = JavaCore.create(file, javaProject);
	                if (element != null && element.getPrimaryElement() instanceof ICompilationUnit) {
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
	                }
	            }
	        }
	        return new BeansConfigId(file.getFileExtension(), project.getName(), file.getFullPath().toString());
	    }
	    return null;
	}
}
