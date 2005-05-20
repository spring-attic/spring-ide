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
import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.web.flow.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfig;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfigSet;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowProject;

/**
 * This class defines a Spring web flow config set (a list of beans config names).
 */
public class WebFlowConfigSet extends WebFlowModelElement implements
        IWebFlowConfigSet {

    private List configNames;

    private IBeansConfigSet beansConfigSet;

    private String beansConfigSetName;

    public WebFlowConfigSet(IWebFlowProject project, String name) {
        this(project, name, new ArrayList());
    }

    public WebFlowConfigSet(IWebFlowProject project, String name,
            List configNames) {
        super(project, name);
        this.configNames = new ArrayList(configNames);
    }

    /**
     * Sets internal maps with <code>IBean</code> s and bean classes to
     * <code>null</code>.
     */
    public void reset() {
    }

    public int getElementType() {
        return CONFIG_SET;
    }

    public IResource getElementResource() {
        return getElementParent().getElementResource();
    }

    public void addConfig(String configName) {
        if (configName.length() > 0 && !configNames.contains(configName)) {
            configNames.add(configName);
            reset();
        }
    }

    public boolean hasConfig(String configName) {
        return configNames.contains(configName);
    }

    public boolean hasConfig(IFile file) {
        return configNames.contains(file.getProjectRelativePath().toString());
    }

    public IWebFlowConfig getConfig(IFile file) {
        int i = configNames.indexOf(file.getProjectRelativePath().toString());
        if (i >= 0)
            return (IWebFlowConfig) configNames.get(i);
        else
            return null;
    }

    public void removeConfig(String configName) {
        configNames.remove(configName);
        reset();
    }

    public Collection getConfigs() {
        return configNames;
    }

    public void replaceConfig(String origFileName, String newFileName) {
        removeConfig(origFileName);
        addConfig(newFileName);
        reset();
    }

    public String toString() {
        return getElementName() + ": " + configNames.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfigSet#setBeanConfigSet(org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet)
     */
    public void setBeansConfigSet(IBeansConfigSet beansConfigSet) {
        this.beansConfigSet = beansConfigSet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfigSet#getBeanConfigSet()
     */
    public IBeansConfigSet getBeansConfigSet() {
        if (this.beansConfigSet == null && this.beansConfigSetName != null) {
            IBeansProject beansProject = BeansCorePlugin.getModel().getProject(
                    this.getElementResource().getProject());
            Collection configSets = beansProject.getConfigSets();
            Iterator iterator = configSets.iterator();
            while (iterator.hasNext()) {
                IBeansConfigSet configSet = (IBeansConfigSet) iterator.next();
                if (this.beansConfigSetName.equals(configSet.getElementName())) {
                    this.beansConfigSet = configSet;
                }
            }
        }
        return this.beansConfigSet;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfigSet#hasBeansConfigSet()
     */
    public boolean hasBeansConfigSet() {
        return this.beansConfigSet != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfigSet#setBeansConfigSetElementName(java.lang.String)
     */
    public void setBeansConfigSetElementName(String name) {
        this.beansConfigSetName = name;
    }

    public void accept(IModelElementVisitor visitor) {
        visitor.visit(this);
    }
}