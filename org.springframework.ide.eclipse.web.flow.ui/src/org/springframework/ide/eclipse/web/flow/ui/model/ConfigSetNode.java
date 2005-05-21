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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfigSet;

/**
 * Representation of a Spring web flow configuration set.
 */
public class ConfigSetNode extends AbstractNode {

    public static final int NAME = 1;

    public static final int CONFIGS = 2;

    private IWebFlowConfigSet configSet;

    private List configs = new ArrayList();

    private List states = null; // lazy initialized in getBeans() or getBean()

    private Map statesMap; // lazy initialized in getBean()
    
    private IBeansConfigSet beansConfigSet;

    /**
     * Creates an empty node.
     */
    public ConfigSetNode(ProjectNode project) {
        super(project, null);
    }

    /**
     * Creates a new node from given config set.
     * 
     * @param project
     *            the new node's parent
     * @param configSet
     *            the new node's config set
     */
    public ConfigSetNode(ProjectNode project, IWebFlowConfigSet configSet) {
        super(project, configSet.getElementName());
        this.configSet = configSet;

        // Add configs from given config set to internal list
        Iterator iter = configSet.getConfigs().iterator();
        while (iter.hasNext()) {
            String configName = (String) iter.next();
            ConfigNode config = new ConfigNode(this, configName);
            configs.add(config);
        }
    }

    public ProjectNode getProjectNode() {
        return (ProjectNode) getParent();
    }

    public void setName(String name) {
        super.setName(name);
        propertyChanged(this, NAME);
    }

    /**
     * Adds the given config to this config set.
     * 
     * @param config
     *            the config to add
     */
    public void addConfig(ConfigNode config) {
        configs.add(config);
        propertyChanged(this, CONFIGS);
    }

    public int getConfigCount() {
        return configs.size();
    }

    /**
     * Returns the bean nodes of this bean factory.
     * 
     * @return bean nodes of this bean factory
     */
    public List getConfigs() {
        return new ArrayList(configs);
    }

    public List getConfigNames() {
        List names = new ArrayList();
        Iterator iter = configs.iterator();
        while (iter.hasNext()) {
            ConfigNode config = (ConfigNode) iter.next();
            names.add(config.getName());
        }
        return names;
    }

    public boolean hasConfig(String name) {
        Iterator iter = configs.iterator();
        while (iter.hasNext()) {
            ConfigNode config = (ConfigNode) iter.next();
            if (name.equals(config.getName())) {
                return true;
            }
        }
        return false;
    }

    public void removeConfig(String name) {
        ConfigNode removeConfig = null;
        Iterator iter = configs.iterator();
        while (iter.hasNext()) {
            ConfigNode config = (ConfigNode) iter.next();
            if (name.equals(config.getName())) {
                removeConfig = config;
                break;
            }
        }
        if (removeConfig != null) {
            configs.remove(removeConfig);
            propertyChanged(this, CONFIGS);
        }
    }

    public void moveConfigUp(ConfigNode config) {
        int index = configs.indexOf(config);
        if (index > 0) {
            configs.remove(config);
            configs.add(index - 1, config);
            propertyChanged(this, CONFIGS);
        }
    }

    public void moveConfigDown(ConfigNode config) {
        int index = configs.indexOf(config);
        if (index != -1 && index < (configs.size() - 1)) {
            configs.remove(config);
            configs.add(index + 1, config);
            propertyChanged(this, CONFIGS);
        }
    }

    /**
     * Clear's this node's internally stored data
     */
    public void clear() {
        configs.clear();
        if (states != null) {
            states.clear();
        }
    }

    public void remove(INode node) {
        configs.remove(node);
    }

    public Object getAdapter(Class adapter) {
        //if (adapter == IPropertySource.class) {
        //	return BeansUIUtils.getPropertySource(configSet);
        //}
        return super.getAdapter(adapter);
    }

    public String toString() {
        StringBuffer text = new StringBuffer();
        text.append(getName());
        text.append(": configs=");
        text.append(configs);
        return text.toString();
    }

    public IBeansConfigSet getBeansConfigSet() {
        if (this.configSet != null)
            return this.configSet.getBeansConfigSet();
        else
            return this.beansConfigSet;
    }

    public void setBeansConfigSet(IBeansConfigSet beans) {
        if (this.configSet != null) {
            this.configSet.setBeansConfigSet(beans);
            if (beans == null) {
                this.configSet.setBeansConfigSetElementName(null);
            }
        }
        this.beansConfigSet = beans;
    }

    public IWebFlowConfigSet getWebFlowConfigSet() {
        return this.configSet;
    }
}