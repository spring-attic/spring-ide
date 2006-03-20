/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.views.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.ui.IPropertyListener;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

/**
 * Representation of an Spring project.
 *
 * @author Torsten Juergeleit
 */
public class ProjectNode extends AbstractNode {

	public static final int CONFIGS = 1;
	public static final int CONFIG_SETS = 2;

	private Set configExtensions;
	private Map configs;
	private Map configSets;
	private ListenerList listeners;

	/**
	 * Creates a new project node with an empty Spring bean config list.
	 * 
	 * @param name the project's name
	 */
	public ProjectNode(INode parent, String name) {
		super(parent, name);
		setElement(BeansCorePlugin.getModel().getProject(name));

		configExtensions = new HashSet();
		configs = new HashMap();
		configSets = new HashMap();
		listeners = new ListenerList();
	}

	public IBeansProject getProject() {
		return (IBeansProject) getElement();
	}

	public void addPropertyListener(IPropertyListener listener) {
		listeners.add(listener);
	}

	public void propertyChanged(INode node, int propertyId) {
		AbstractNode parent = (AbstractNode) getParent();
		if (parent == null) {
			Object[] array = listeners.getListeners();
			for (int i = 0; i < array.length; i++) {
				IPropertyListener listener = (IPropertyListener) array[i];
				listener.propertyChanged(node, propertyId);
			}
		} else {
			parent.propertyChanged(node, propertyId);
		}
	}

	public void removePropertyListener(IPropertyListener listener) {
		listeners.remove(listener);
	}

	public void setConfigExtensions(Collection extensions) {
		configExtensions.clear();
		Iterator iter = extensions.iterator();
		while (iter.hasNext()) {
			String extension = (String) iter.next();
			if (!configExtensions.contains(extension)) {
				configExtensions.add(extension);
			}
		}
		propertyChanged(this, CONFIGS);
	}

	public boolean hasConfigExtension(String extension) {
		return configExtensions.contains(extension);
	}

	public Set getConfigExtensions() {
		return Collections.unmodifiableSet(configExtensions);
	}

	public void setConfigs(Collection configs) {
		this.configs.clear();
		Iterator iter = configs.iterator();
		while (iter.hasNext()) {
			IBeansConfig config = (IBeansConfig) iter.next();
			addConfig(config.getElementName());
		}
		propertyChanged(this, CONFIGS);
	}

	public void addConfig(String name) {
		ConfigNode node = getConfig(name);
		if (node == null) {
			node = new ConfigNode(this, name);
			configs.put(name, node);
			propertyChanged(this, CONFIGS);
		}
	}

	public boolean hasConfig(String name) {
		return configs.containsKey(name);
	}

	public ConfigNode getConfig(IFile file) {
		return getConfig(file.getProjectRelativePath().toString());
	}

	public ConfigNode getConfig(String name) {
		return (configs.containsKey(name) ?
										 (ConfigNode) configs.get(name) : null);
	}

	/**
	 * Returns all Spring bean configs from this project.
	 */
	public List getConfigs() {
		return new ArrayList(configs.values());
	}

	public List getConfigNames() {
		return new ArrayList(configs.keySet());
	}

	/**
	 * Returns whether this project node contains any Spring bean configs.
	 */
	public boolean hasConfigs() {
		return !configs.isEmpty();
	}

	/**
	 * Removes given Spring bean config from this project.
	 */
	public void removeConfig(String name) {
		ConfigNode config = (ConfigNode) configs.remove(name);
		if (config != null) {
			Iterator iter = configSets.values().iterator();
			while (iter.hasNext()) {
				ConfigSetNode configSet = (ConfigSetNode) iter.next();
				configSet.removeConfig(name);
			}
			propertyChanged(this, CONFIGS);
		}
	}

	public void setConfigSets(Collection configSets) {
		this.configSets.clear();
		Iterator iter = configSets.iterator();
		while (iter.hasNext()) {
			addConfigSet((IBeansConfigSet) iter.next());
		}
		propertyChanged(this, CONFIG_SETS);
	}

	public void addConfigSet(IBeansConfigSet configSet) {
		String name = configSet.getElementName();
		ConfigSetNode node = getConfigSet(name);
		if (node == null) {
			node = new ConfigSetNode(this, configSet);
			addConfigSet(node);
			propertyChanged(this, CONFIG_SETS);
		}
	}

	public void addConfigSet(ConfigSetNode configSet) {
		configSets.put(configSet.getName(), configSet);
		propertyChanged(this, CONFIG_SETS);
	}

	public boolean hasConfigSet(String name) {
		return configSets.containsKey(name);
	}

	public ConfigSetNode getConfigSet(String name) {
		return (configSets.containsKey(name) ?
									(ConfigSetNode)configSets.get(name) : null);
	}

	/**
	 * Returns all Spring bean config sets from this project.
	 */
	public List getConfigSets() {
		return new ArrayList(configSets.values());
	}

	/**
	 * Removes given Spring bean config set from this project.
	 */
	public void removeConfigSet(String name) {
		ConfigSetNode configSet = (ConfigSetNode) configSets.remove(name);
		if (configSet != null) {
			propertyChanged(this, CONFIG_SETS);
		}
	}

	public void remove(INode node) {
		if (node instanceof ConfigNode) {
			configs.remove(node.getName());

			// Remove config from all config sets
			Iterator iter = configSets.values().iterator();
			while (iter.hasNext()) {
				ConfigSetNode configSet = (ConfigSetNode) iter.next();
				configSet.remove(node);
			}
		} else {
			configSets.remove(node.getName());
		}
	}

	public String toString() {
		StringBuffer text = new StringBuffer();
		text.append(getName());
		text.append(": configs=");
		text.append(configs);
		text.append(": configSets=");
		text.append(configSets);
		return text.toString();
	}
}
