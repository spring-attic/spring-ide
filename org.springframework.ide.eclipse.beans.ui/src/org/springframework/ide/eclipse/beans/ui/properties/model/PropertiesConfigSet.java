/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.properties.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent.Type;

/**
 * This class defines a Spring beans config set (a list of beans config names).
 * 
 * @author Torsten Juergeleit
 */
public class PropertiesConfigSet extends BeansConfigSet {

	public PropertiesConfigSet(IBeansProject project, String name) {
		super(project, name, new HashSet<String>());
	}

	/**
	 * Creates a deep copy of given config set associated with the specified
	 * project.
	 */
	public PropertiesConfigSet(IBeansProject project,
			IBeansConfigSet configSet) {
		super(project, configSet.getElementName(), configSet.getConfigNames());
		super.setAllowAliasOverriding(configSet.isAllowAliasOverriding());
		super.setAllowBeanDefinitionOverriding(configSet
				.isAllowBeanDefinitionOverriding());
		super.setIncomplete(configSet.isIncomplete());
	}

	@Override
	public void setElementName(String name) {
		super.setElementName(name);
		notifyListeners();
	}

	@Override
	public void setAllowBeanDefinitionOverriding(
			boolean allowBeanDefinitionOverriding) {
		super.setAllowBeanDefinitionOverriding(allowBeanDefinitionOverriding);
		notifyListeners();
	}

	@Override
	public void setIncomplete(boolean isIncomplete) {
		super.setIncomplete(isIncomplete);
		notifyListeners();
	}

	@Override
	public void addConfig(String configName) {
		super.addConfig(configName);
		notifyListeners();
	}

	@Override
	public void removeConfig(String configName) {
		super.removeConfig(configName);
		notifyListeners();
	}

	@Override
	public void removeAllConfigs() {
		super.removeAllConfigs();
		notifyListeners();
	}

	@Override
	public Set<IBeansConfig> getConfigs() {
		Set<IBeansConfig> configs = new LinkedHashSet<IBeansConfig>();
		for (String configName : configNames) {
			IBeansConfig config = new PropertiesConfig(this, configName);
			if (config != null) {
				configs.add(config);
			}
		}
		return configs;
	}

	public void moveConfigUp(IBeansConfig config) {
		String configName = config.getElementName();
		if (configNames.contains(configName)) {
			ArrayList<String> newConfigNames = new ArrayList<String>(
					configNames);
			int index = newConfigNames.indexOf(configName);
			if (index > 0) {
				newConfigNames.remove(configName);
				newConfigNames.add(index - 1, configName);
				configNames.clear();
				configNames.addAll(newConfigNames);
				notifyListeners();
			}
		}
	}

	public void moveConfigDown(IBeansConfig config) {
		String configName = config.getElementName();
		if (configNames.contains(configName)) {
			ArrayList<String> newConfigNames = new ArrayList<String>(
					configNames);
			int index = newConfigNames.indexOf(configName);
			if (index < (configNames.size() - 1)) {
				newConfigNames.remove(configName);
				newConfigNames.add(index + 1, configName);
				configNames.clear();
				configNames.addAll(newConfigNames);
				notifyListeners();
			}
		}
	}

	protected final void notifyListeners() {
		PropertiesModel model = (PropertiesModel) getElementParent()
				.getElementParent();
		model.notifyListeners(this, Type.CHANGED);
	}
}
