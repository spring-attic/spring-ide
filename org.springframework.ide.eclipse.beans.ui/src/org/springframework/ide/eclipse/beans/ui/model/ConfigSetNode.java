/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ui.views.properties.IPropertySource;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * Representation of a Spring bean configuration set.
 */
public class ConfigSetNode extends AbstractNode {

	public static final int NAME = 1;
	public static final int CONFIGS = 2;

	private IBeansConfigSet configSet;
	private List configs = new ArrayList();
	private List beans = null;  // lazy initialized in getBeans() or getBean()
	private Map beansMap;  // lazy initialized in getBean()
	private boolean isOverrideEnabled = false;
	private boolean isIncomplete = false;

	/**
	 * Creates an empty node.
	 */
	public ConfigSetNode(ProjectNode project) {
		super(project, null);
	}

	/**
	 * Creates a new node from given config set.
	 * 
	 * @param project the new node's parent
	 * @param configSet the new node's config set
	 */
	public ConfigSetNode(ProjectNode project, IBeansConfigSet configSet) {
		super(project, configSet.getElementName());
		this.configSet = configSet;

		isOverrideEnabled = configSet.isAllowBeanDefinitionOverriding();

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

	public void setOverrideEnabled(boolean isOverrideEnabled) {
		this.isOverrideEnabled = isOverrideEnabled;
	}

	public boolean isOverrideEnabled() {
		return isOverrideEnabled;
	}

	public void setIncomplete(boolean isIncomplete) {
		this.isIncomplete = isIncomplete;
	}

	public boolean isIncomplete() {
		return isIncomplete;
	}

	/**
	 * Adds the given config to this config set.
	 * 
	 * @param config the config to add
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

	public BeanNode getBean(String name) {
		if (beansMap == null) {
			if (beans == null) {
				// Lazily parse the config files to populate the bean list
				readConfigFiles();
				refreshViewer();
			}

			// Lazily initialize the bean map
			beansMap = new HashMap();
			Iterator iter = beans.iterator();
			while (iter.hasNext()) {
				BeanNode bean = (BeanNode) iter.next();
				beansMap.put(bean.getName(), bean);
			}
		}
		return (BeanNode) beansMap.get(name);
	}

	/**
	 * Returns the bean nodes of this bean factory.
	 * 
	 * @return bean nodes of this bean factory
	 */
	public BeanNode[] getBeans(boolean refreshViewer) {
		if (beans == null) {
			// Lazily parse the config files to populate the bean list
			readConfigFiles();
			if (refreshViewer) {
				refreshViewer();
			}
		}
		return (BeanNode[]) beans.toArray(new BeanNode[beans.size()]);
	}

	private void readConfigFiles() {
		this.beans = new ArrayList();
		Iterator iter = configs.iterator();
		while (iter.hasNext()) {
			ConfigNode config = (ConfigNode) iter.next();
			BeanNode[] beans = config.getBeans(false);
			for (int i = 0; i < beans.length; i++) {
				BeanNode bean = new BeanNode(this, beans[i]);
				markOverridingBean(bean);
				this.beans.add(bean);
			}
		}
	}

	private void markOverridingBean(BeanNode newBean) {
		String beanName = newBean.getName();
		Iterator iter = beans.iterator();
		while (iter.hasNext()) {
			BeanNode bean = (BeanNode) iter.next();
			if ((bean.getFlags() & INode.FLAG_HAS_ERRORS) == 0 &&
											  beanName.equals(bean.getName())) {
				newBean.setIsOverride(true);
				break;
			}
		}
	}

	/**
	 * Clear's this node's internally stored data
	 */
	public void clear() {
		configs.clear();
		if (beans != null) {
			beans.clear();
		}
	}

	public void remove(INode node) {
		configs.remove(node);
	}

	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			return BeansUIUtils.getPropertySource(configSet);
		} else if (adapter == IModelElement.class) {
			return configSet;
		}
		return super.getAdapter(adapter);
	}

	public String toString() {
		StringBuffer text = new StringBuffer();
		text.append(getName());
		text.append(": configs=");
		text.append(configs);
		return text.toString();
	}
}
