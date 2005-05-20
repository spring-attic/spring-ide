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

package org.springframework.ide.eclipse.web.flow.core.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.springframework.ide.eclipse.web.flow.core.WebFlowCorePlugin;
import org.springframework.ide.eclipse.web.flow.core.internal.project.WebFlowProjectDescription;
import org.springframework.ide.eclipse.web.flow.core.internal.project.WebFlowProjectDescriptionReader;
import org.springframework.ide.eclipse.web.flow.core.internal.project.WebFlowProjectDescriptionWriter;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfig;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfigSet;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowProject;

public class WebFlowProject extends WebFlowModelElement implements
        IWebFlowProject {

    private IProject project;

    private WebFlowProjectDescription description;

    public WebFlowProject(IProject project) {
        super(WebFlowCorePlugin.getModel(), project.getName());
        this.project = project;
    }

    public int getElementType() {
        return PROJECT;
    }

    public IResource getElementResource() {
        return project;
    }

    public IProject getProject() {
        return project;
    }

    /**
     * Sets internal <code>BeansProjectDescription</code> to <code>null</code>.
     * Any further access to the data of this instance of
     * <code>BeansProject</code> leads to reloading of this beans project's
     * config description file.
     */
    public void reset() {
        this.description = null;
    }

    public void addConfig(IFile file) {
        getDescription().addConfig(file);
    }

    public void removeConfig(IFile file) {
        getDescription().removeConfig(file);
    }

    public Collection getConfigNames() {
        return getDescription().getConfigNames();
    }

    /**
     * Returns true if given file belongs to the list of Spring bean config
     * files which are stored in the project description.
     */
    public boolean hasConfig(IFile file) {
        return getDescription().hasConfig(file);
    }

    /**
     * Returns true if given config name belongs to the list of Spring bean
     * config files which are stored in the project description.
     */
    public boolean hasConfig(String configName) {
        return getDescription().hasConfig(configName);
    }

    /**
     * Returns <code>IBeansConfig</code> for given config file.
     */
    public IWebFlowConfig getConfig(IFile configFile) {
        return getDescription().getConfig(configFile);
    }

    /**
     * Returns <code>IBeansConfig</code> of given name.
     */
    public IWebFlowConfig getConfig(String configName) {
        return getDescription().getConfig(configName);
    }

    /**
     * Returns a collection of all BeansConfig defined in this project.
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfig
     */
    public Collection getConfigs() {
        return getDescription().getConfigs();
    }

    public boolean hasConfigSet(String configSetName) {
        IWebFlowConfigSet configSet = getDescription().getConfigSet(
                configSetName);
        return configSet != null;
    }

    /**
     * Returns a list of <code>IBeansConfigSet</code> s known defined within
     * this project.
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfigSet
     */
    public Collection getConfigSets() {
        return getDescription().getConfigSets();
    }

    public IWebFlowConfigSet getConfigSet(String name) {
        return getDescription().getConfigSet(name);
    }

    /**
     * Updates the list of configs (by name) belonging to this project. After
     * deleting all problem markers from configs the modified project
     * description is saved to disk.
     * 
     * @param configs
     *            list of config names
     */
    public void setConfigs(Collection configs) {
        WebFlowProjectDescription description = getDescription();

        // Look for removed config files and
        // 1. delete all problem markers from them
        // 2. remove config from any config set
        ArrayList toBeRemoved = new ArrayList();

        Iterator iter = description.getConfigNames().iterator();
        while (iter.hasNext()) {
            String config = (String) iter.next();
            if (!configs.contains(config)) {
                IFile file = getConfigFile(config);
                //WebFlowCoreUtils.deleteProblemMarkers(file);
                toBeRemoved.add(config);
            }
        }

        for (int i = 0; i < toBeRemoved.size(); i++) {
            String config = (String) toBeRemoved.get(i);
            description.removeConfig(config);
        }

        description.setConfigNames(configs);
        WebFlowProjectDescriptionWriter.write(project, description);
    }

    /**
     * Updates the <code>BeansConfigSet</code> s defined within this project.
     * The modified project description is saved to disk.
     * 
     * @param configSets
     *            list of BeansConfigSet instances
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfigSet
     */
    public void setConfigSets(List configSets) {
        WebFlowProjectDescription description = getDescription();
        description.setConfigSets(configSets);
        WebFlowProjectDescriptionWriter.write(project, description);
    }

    /**
     * Deletes all problem markers from config files.
     */
    public void deleteProblemMarkers() {
        WebFlowProjectDescription description = getDescription();
        Iterator iter = description.getConfigNames().iterator();
        while (iter.hasNext()) {
            IFile file = getConfigFile((String) iter.next());
            //WebFlowCoreUtils.deleteProblemMarkers(file);
        }
    }

    public String toString() {
        return getElementName();
    }

    /**
     * Returns lazily loaded project description. <b>This nature's project has
     * to be set first!!! </b>
     */
    private WebFlowProjectDescription getDescription() {
        if (description == null) {
            description = WebFlowProjectDescriptionReader.read(this);
        }
        return description;
    }

    private IFile getConfigFile(String configName) {
        if (configName.charAt(0) == '/') {
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            return (IFile) root.findMember(configName);
        }
        return (IFile) project.findMember(configName);
    }

    public void accept(
            org.springframework.ide.eclipse.web.flow.core.model.IModelElementVisitor visitor) {

        // First visit this project
        if (visitor.visit(this)) {

            // Now ask this project's configs
            Iterator iter = description.getConfigs().iterator();
            while (iter.hasNext()) {
                IWebFlowModelElement element = (IWebFlowModelElement) iter
                        .next();
                element.accept(visitor);
            }

            // Finally ask this project's config sets
            iter = description.getConfigSets().iterator();
            while (iter.hasNext()) {
                IWebFlowModelElement element = (IWebFlowModelElement) iter
                        .next();
                element.accept(visitor);
            }
        }
    }

}