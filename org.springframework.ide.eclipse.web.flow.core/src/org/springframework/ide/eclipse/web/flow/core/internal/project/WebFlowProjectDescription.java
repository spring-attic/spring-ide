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

package org.springframework.ide.eclipse.web.flow.core.internal.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.web.flow.core.internal.model.WebFlowConfig;
import org.springframework.ide.eclipse.web.flow.core.internal.model.WebFlowConfigSet;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfig;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfigSet;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowProject;

public class WebFlowProjectDescription {

    private IWebFlowProject project;

    private List configNames;

    private Map configs;

    private Map configSets;

    public WebFlowProjectDescription(IWebFlowProject project) {
        this.project = project;
        this.configs = new HashMap();
        this.configNames = new ArrayList();
        this.configSets = new HashMap();
    }

    public void setConfigNames(Collection configs) {
        this.configNames = new ArrayList(configs);
    }

    public Collection getConfigNames() {
        return configNames;
    }

    public void addConfig(IFile file) {
        addConfig(file.getProjectRelativePath().toString());
    }

    public void addConfig(String name) {
        if (name.length() > 0) {
            configNames.add(name);
            IWebFlowConfig config = new WebFlowConfig(project, name);
            configs.put(name, config);
        }
    }

    /**
     * Returns true if given file belongs to the list of Spring web flow config
     * files which are stored in the project description.
     */
    public boolean hasConfig(IFile file) {
        return configNames.contains(file.getProjectRelativePath().toString());
    }

    /**
     * Returns true if given config (project-relative file name) belongs to the
     * list of Spring web flow config files which are stored in the project
     * description.
     */
    public boolean hasConfig(String name) {
        return configNames.contains(name);
    }

    public IWebFlowConfig getConfig(IFile file) {
        String name = file.getProjectRelativePath().toString();
        if (configNames.contains(name)) {
            return (IWebFlowConfig) configs.get(name);
        }
        return null;
    }

    public IWebFlowConfig getConfig(String name) {
        if (configNames.contains(name)) {
            return (IWebFlowConfig) configs.get(name);
        }
        return null;
    }

    public Collection getConfigs() {
        return configs.values();
    }

    public void removeConfig(IFile file) {
        removeConfig(file.getProjectRelativePath().toString());
    }

    public void removeConfig(String name) {
        configNames.remove(name);
        configs.remove(name);

        // Remove given config name from any config set
        Iterator iter = configSets.values().iterator();
        while (iter.hasNext()) {
            WebFlowConfigSet configSet = (WebFlowConfigSet) iter.next();
            configSet.removeConfig(name);
        }
    }

    public void addConfigSet(IWebFlowConfigSet configSet) {
        configSets.put(configSet.getElementName(), configSet);
    }

    public void setConfigSets(List configSets) {
        this.configSets.clear();
        Iterator iter = configSets.iterator();
        while (iter.hasNext()) {
            IWebFlowConfigSet configSet = (IWebFlowConfigSet) iter.next();
            this.configSets.put(configSet.getElementName(), configSet);
        }
    }

    public int getNumberOfConfigSets() {
        return configSets.size();
    }

    public Collection getConfigSetNames() {
        return configSets.keySet();
    }

    public IWebFlowConfigSet getConfigSet(String name) {
        return (IWebFlowConfigSet) configSets.get(name);
    }

    public Collection getConfigSets() {
        return configSets.values();
    }

    public String toString() {
        return "Configs=" + configNames + ", ConfigsSets="
                + configSets.toString();
    }

}