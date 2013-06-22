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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig.Type;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

/**
 * @author Martin Lippert
 * @author Andrew Eisenberg
 * @since 3.4.0
 */
public class BeansConfigFactory {
    
    private static final String EXTENSION_POINT = BeansCorePlugin.PLUGIN_ID + ".beansconfigkind";
//    private static final String CONFIG_GENERATOR = "configGenerator";
    private static final String GENERATOR = "generator";
    
    private static Set<IBeansConfigGenerator<?>> configGenerators;
	
	private static Set<IBeansConfigGenerator<?>> initGenerators() {
        Set<IBeansConfigGenerator<?>> set = new HashSet<IBeansConfigGenerator<?>>();
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_POINT);
        IExtension[] extensions = extensionPoint.getExtensions();
        for (IExtension ext : extensions) {
            IConfigurationElement[] configElts = ext.getConfigurationElements();
            for (IConfigurationElement elt : configElts) {
                try {
                    IBeansConfigGenerator<?> configGenerator = (IBeansConfigGenerator<?>) elt.createExecutableExtension(GENERATOR);
                    set.add(configGenerator);
                } catch (Exception e) {
                    BeansCorePlugin.log("Error creating bean generator " + elt.getAttribute(GENERATOR) + " ignoring extension point", e);
                }
            }
        }
        return set;
    }

    public static IBeansConfig create(IBeansProject project, BeansConfigId id, IBeansConfig.Type type) {
        if (id == null) {
            return null;
        }
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
    
    public static IBeansConfig create(BeansConfigId id, Type type) {
        return create((IBeansProject) null, id, type);
    }
}
