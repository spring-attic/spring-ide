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

package org.springframework.ide.eclipse.beans.core.internal.project;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

/**
 * This class holds the configuration for a Spring Beans project.
 * @author Torsten Juergeleit
 */
public class BeansProjectDescription {

	private IBeansProject project;
	private Set<String> configExtensions;
	private Map<String, IBeansConfig> configs;
	private Map<String, IBeansConfigSet> configSets;

	public BeansProjectDescription(IBeansProject project) {
		this.project = project;
		configExtensions = new LinkedHashSet<String>();
		configs = new LinkedHashMap<String, IBeansConfig>();
		configSets = new LinkedHashMap<String, IBeansConfigSet>();
	}

	public Set<String> getConfigExtensions() {
		return Collections.unmodifiableSet(configExtensions);
	}

	public void setConfigExtensions(Set<String> configExtensions) {
		this.configExtensions = configExtensions;
	}

	public void addConfigExtension(String extension) {
		Assert.isNotNull(extension);
		if (extension != null && extension.length() > 0
				&& !configExtensions.contains(extension)) {
			configExtensions.add(extension);
		}
	}

	public void setConfigNames(Set<String> configNames) {
		configs.clear();
		for (String configName : configNames){
			configs.put(configName, new BeansConfig(project, configName));
		}
	}

	public boolean addConfig(IFile file) {
		return addConfig(getConfigName(file));
	}

	private String getConfigName(IFile file) {
		String configName;
		if (file.getProject().equals(project.getProject())) {
			configName = file.getProjectRelativePath().toString();
		} else {
			configName = file.getFullPath().toString();
		}
		return configName;
	}

	public boolean addConfig(String configName) {
		if (configName.length() > 0 && !configs.containsKey(configName)) {
			configs.put(configName, new BeansConfig(project, configName));
			return true;
		}
		return false;
	}

	/**
	 * Returns true if given file belongs to the list of Spring bean config
	 * files which are stored in the project description.
	 */
	public boolean hasConfig(IFile file) {
		return hasConfig(getConfigName(file));
	}

	/**
	 * Returns true if given config (project-relative file name) belongs to the
	 * list of Spring bean config files which are stored in the project
	 * description.
	 */
	public boolean hasConfig(String configName) {
		return configs.containsKey(configName);
	}

	public IBeansConfig getConfig(IFile file) {
		return configs.get(getConfigName(file));
	}

	public IBeansConfig getConfig(String name) {
		return configs.get(name);
	}

	public Set<IBeansConfig> getConfigs() {
		return new LinkedHashSet<IBeansConfig>(configs.values());
	}

	public Set<String> getConfigNames() {
		return new LinkedHashSet<String>(configs.keySet());
	}

	public boolean removeConfig(IFile file) {
		if (file.getProject().equals(project)) {
			return removeConfig(file.getProjectRelativePath().toString());
		}

		// External configs only remove from all config sets 
		return removeConfigFromConfigSets(file.getFullPath().toString());
	}

	public boolean removeConfig(String name) {
		if (hasConfig(name)) {
			configs.remove(name);
			removeConfigFromConfigSets(name);
			return true;
		}
		return false;
	}

	private boolean removeConfigFromConfigSets(String name) {
		for (IBeansConfigSet configSet : configSets.values()) {
			if (configSet.hasConfig(name)) {
				((BeansConfigSet) configSet).removeConfig(name);
				return true;
			}
		}
		return false;
	}

	public void addConfigSet(IBeansConfigSet configSet) {
		configSets.put(configSet.getElementName(), configSet);
	}

	public void setConfigSets(Set<IBeansConfigSet> configSets) {
		this.configSets.clear();
		for (IBeansConfigSet configSet : configSets) {
			this.configSets.put(configSet.getElementName(), configSet);
		}
	}

	public IBeansConfigSet getConfigSet(String name) {
		return configSets.get(name);
	}

	public Set<IBeansConfigSet> getConfigSets() {
		return new LinkedHashSet<IBeansConfigSet>(configSets.values());
	}

	public String toString() {
		return "ConfigExtensions=" + configExtensions + ", Configs="
				+ configs.values() + ", ConfigsSets=" + configSets;
	}
}
