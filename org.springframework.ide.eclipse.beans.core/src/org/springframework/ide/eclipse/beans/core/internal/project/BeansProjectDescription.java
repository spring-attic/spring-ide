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

package org.springframework.ide.eclipse.beans.core.internal.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

public class BeansProjectDescription {

	private IBeansProject project;
	private List configNames;
	private Map configs;
	private Map configSets;

	public BeansProjectDescription(IBeansProject project) {
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
			IBeansConfig config = new BeansConfig(project, name);
			configs.put(name, config);
		}
	}

	/**
	 * Returns true if given file belongs to the list of Spring bean config
	 * files which are stored in the project description. 
	 */
	public boolean hasConfig(IFile file) {
		return configNames.contains(file.getProjectRelativePath().toString());
	}

	/**
	 * Returns true if given config (project-relative file name) belongs to the
	 * list of Spring bean config files which are stored in the project
	 * description. 
	 */
	public boolean hasConfig(String name) {
		return configNames.contains(name);
	}

	public IBeansConfig getConfig(IFile file) {
		String name = file.getProjectRelativePath().toString();
		if (configNames.contains(name)) {
			return (IBeansConfig) configs.get(name);
		}
		return null;
	}

	public IBeansConfig getConfig(String name) {
		if (configNames.contains(name)) {
			return (IBeansConfig) configs.get(name);
		}
		return null;
	}

	public Collection getConfigs() {
		return configs.values();
	}

	public IFile getConfigFile(String configName) {
		if (configName.charAt(0) == '/') {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			return (IFile) root.findMember(configName);
		}
		return (IFile) ((IProject)
						   project.getElementResource()).findMember(configName);
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
			BeansConfigSet configSet = (BeansConfigSet) iter.next();
			configSet.removeConfig(name);
		}
	}

	public void addConfigSet(IBeansConfigSet configSet) {
		configSets.put(configSet.getElementName(), configSet);
	}

	public void setConfigSets(List configSets) {
		this.configSets.clear();
		Iterator iter = configSets.iterator();
		while (iter.hasNext()) {
			IBeansConfigSet configSet = (IBeansConfigSet) iter.next();
			this.configSets.put(configSet.getElementName(), configSet);
		}
	}

	public int getNumberOfConfigSets() {
		return configSets.size();
	}

	public Collection getConfigSetNames() {
		return configSets.keySet();
	}

	public IBeansConfigSet getConfigSet(String name) {
		return (IBeansConfigSet) configSets.get(name);
	}

	public Collection getConfigSets() {
		return configSets.values();
	}

	public String toString() {
		return "Configs=" + configNames + ", ConfigsSets=" + configSets.toString();
	}

}
