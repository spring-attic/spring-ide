/*******************************************************************************
 * Copyright (c) 2007, 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigFactory;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigId;
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
		super(project, name, new HashSet<BeansConfigId>(), type);
	}

	/**
	 * Creates a deep copy of given config set associated with the specified
	 * project.
	 */
	public PropertiesConfigSet(PropertiesProject project,
			IBeansConfigSet configSet, Type type) {
		super(project, configSet.getElementName(), configSet.getConfigIds(), type);
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
	public void addConfig(BeansConfigId configId) {
		super.addConfig(configId);
		notifyListeners();
	}

	@Override
	public void removeConfig(BeansConfigId configId) {
		super.removeConfig(configId);
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
		for (BeansConfigId configId : configIds) {
			IBeansConfig config = BeansConfigFactory.create(this, configId, IBeansConfig.Type.MANUAL);
			if (config != null) {
				configs.add(config);
			}
		}
		return configs;
	}

	public void moveConfigUp(IBeansConfig config) {
	    BeansConfigId configId = config.getId();
		if (configIds.contains(configId)) {
			ArrayList<BeansConfigId> newConfigIds = new ArrayList<BeansConfigId>(
					configIds);
			int index = newConfigIds.indexOf(configId);
			if (index > 0) {
				newConfigIds.remove(configId);
				newConfigIds.add(index - 1, configId);
				configIds.clear();
				configIds.addAll(newConfigIds);
				notifyListeners();
			}
		}
	}

	public void moveConfigDown(IBeansConfig config) {
	    BeansConfigId configId = config.getId();
		if (configIds.contains(configId)) {
			ArrayList<BeansConfigId> newConfigNames = new ArrayList<BeansConfigId>(
					configIds);
			int index = newConfigNames.indexOf(configId);
			if (index < (configIds.size() - 1)) {
				newConfigNames.remove(configId);
				newConfigNames.add(index + 1, configId);
				configIds.clear();
				configIds.addAll(newConfigNames);
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
