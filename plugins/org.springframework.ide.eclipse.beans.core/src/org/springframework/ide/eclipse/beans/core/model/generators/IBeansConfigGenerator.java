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
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

/**
 * Generates {@link IBeansConfig}s for a given kind
 * @author Andrew Eisenberg
 * @since 3.4.0
 */
public interface IBeansConfigGenerator<B extends IBeansConfig> {
    /**
     * @param project the project to provide context for bean generation
     * @param id the unique id of the config.  always <CONFIG_KIND>:<NAME>
     * @param type
     * @return
     * @throws CoreException
     */
    B generateConfig(IBeansProject project, BeansConfigId id, IBeansConfig.Type type) throws CoreException;
    
    /**
     * @param configSet the configSet to provide context for bean generation
     * @param id the unique id of the config.  always <CONFIG_KIND>:<NAME>
     * @param type
     * @return
     * @throws CoreException
     */
    B generateConfig(IBeansConfigSet configSet, BeansConfigId id, IBeansConfig.Type type) throws CoreException;
    
    String getConfigKind();
    
    // Maybe remove
    String getConfigName(IFile file, IProject project);
    
    boolean handlesConfig(IFile file);
    
    boolean handlesConfig(BeansConfigId id);
}
