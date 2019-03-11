/*******************************************************************************
 * Copyright (c) 2007, 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.properties.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent;

/**
 * This class defines a Spring beans config set (a list of beans config names).
 * 
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @author Martin Lippert
 */
public class PropertiesConfigSet extends BeansConfigSet {

	public PropertiesConfigSet(PropertiesProject project, String name, Type type) {
		super(project, name, new HashSet<String>(), type);
	}

	/**
	 * Creates a deep copy of given config set associated with the specified
	 * project.
	 */
	public PropertiesConfigSet(PropertiesProject project,
			IBeansConfigSet configSet, Type type) {
		super(project, configSet.getElementName(), configSet.getConfigNames(), type);
		super.setAllowAliasOverriding(configSet.isAllowAliasOverriding());
		super.setAllowBeanDefinitionOverriding(configSet
				.isAllowBeanDefinitionOverriding());
		super.setIncomplete(configSet.isIncomplete());
		super.setProfiles(configSet.getProfiles());
	}

	@Override
	public void setElementName(String name) {
		super.setElementName(name);
		notifyListeners();
	}
	
	@Override
	public void setProfiles(Set<String> profiles) {
		super.setProfiles(profiles);
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
			IBeansConfig config = PropertiesConfigFactory.create(this, configName, IBeansConfig.Type.MANUAL);
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
		model.notifyListeners(this, ModelChangeEvent.Type.CHANGED);
	}
}
