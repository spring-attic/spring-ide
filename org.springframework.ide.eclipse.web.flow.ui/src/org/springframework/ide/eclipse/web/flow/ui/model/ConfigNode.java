/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.ide.eclipse.web.flow.ui.model;

import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.web.flow.core.WebFlowCorePlugin;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfig;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowProject;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;

/**
 * Representation of a Spring web flow configuration.
 */
public class ConfigNode extends AbstractNode {

    private IWebFlowConfig config;

    /**
     * Creates a new Spring bean factory node with given name.
     * 
     * @param project the new node's parent
     * @param name the new node's config file name (full path)
     */
    public ConfigNode(ProjectNode project, String name) {
        super(project, name);
        setWebFlowConfig(project);
    }

    public IWebFlowState getState() {
        return this.config.getState();
    }

    /**
     * Creates a new Spring bean factory node with given name.
     * 
     * @param configSet the new node's parent
     * @param name the new node's config file name (full path)
     */
    public ConfigNode(ConfigSetNode configSet, String name) {
        super(configSet, name);
        setWebFlowConfig(configSet.getProjectNode());
    }

    public ProjectNode getProjectNode() {
        Object parent = getParent();
        if (parent instanceof ConfigSetNode) {
            return ((ConfigSetNode) parent).getProjectNode();
        }
        return (ProjectNode) parent;
    }

    public IFile getConfigFile() {
        return (config != null ? config.getConfigFile() : null);
    }

    public Object getAdapter(Class adapter) {
        //if (adapter == IPropertySource.class) {
        //	return BeansUIUtils.getPropertySource(config);
        //}
        return super.getAdapter(adapter);
    }

    public String toString() {
        StringBuffer text = new StringBuffer(getName());
        if (config != null) {
            text.append(": ");
            text.append(config.toString());
        }
        return text.toString();
    }

    private void setWebFlowConfig(ProjectNode project) {
        String configName = getName();
        if (configName.charAt(0) == '/') {
            int configNamePos = configName.indexOf('/', 1);
            String projectName = configName.substring(1, configNamePos);
            IWebFlowProject proj = WebFlowCorePlugin.getModel().getProject(
                    projectName);
            configName = configName.substring(configNamePos + 1);
            if (proj != null) {
                config = proj.getConfig(configName);
            }
        }
        else {
            config = project.getWebFlowProject().getConfig(configName);
        }
    }
}
