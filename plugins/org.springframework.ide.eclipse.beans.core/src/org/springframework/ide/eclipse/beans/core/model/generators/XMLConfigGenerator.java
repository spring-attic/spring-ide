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
import org.springframework.ide.eclipse.beans.core.internal.model.XMLBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig.Type;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.io.ExternalFile;

/**
 * Generates XML-based configs
 * @author Andrew Eisenberg
 * @since 3.4.0
 */
public class XMLConfigGenerator implements IBeansConfigGenerator<XMLBeansConfig> {
    public static final String XML_CONFIG_KIND = "xml";

    public XMLBeansConfig generateConfig(IBeansProject project, BeansConfigId id,
            Type type) throws CoreException {
        return new XMLBeansConfig(project, id, IBeansConfig.Type.MANUAL);
    }

    public XMLBeansConfig generateConfig(IBeansConfigSet configSet, BeansConfigId id,
            Type type) throws CoreException {
        return new XMLBeansConfig(configSet, id, IBeansConfig.Type.MANUAL);
    }
    
    public String getConfigKind() {
        return XML_CONFIG_KIND;
    }

    public String getConfigName(IFile file, IProject project) {
        if (handlesConfig(file)) {
            return file.getFullPath().toString();
        }
        return null;
    }

    public boolean handlesConfig(IFile file) {
        return file.getFileExtension().equals(XML_CONFIG_KIND);
    }
    
    public boolean handlesConfig(BeansConfigId id) {
        return XML_CONFIG_KIND.equals(id.name);
    }

}
