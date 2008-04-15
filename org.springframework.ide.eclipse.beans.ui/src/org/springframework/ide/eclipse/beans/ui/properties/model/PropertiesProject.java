/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.properties.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent.Type;

/**
 * This class holds information for a Spring Beans project. The information is retrieved from a
 * given Spring Beans project instead being read from the corresponding project description XML file
 * defined in {@link IBeansProject#DESCRIPTION_FILE}. The information can be persisted by calling
 * the method {@link #saveDescription()}.
 * 
 * @author Torsten Juergeleit
 */
public class PropertiesProject extends BeansProject {

	/**
	 * Create a deep copy of given beans project.
	 */
	public PropertiesProject(PropertiesModel model, IBeansProject project) {
		super(model, project.getProject());

		// At first mark this project as already populated - otherwise the super
		// class accidently tries to populate the project from the config file
		modelPopulated = true;

		configSuffixes = new LinkedHashSet<String>(project.getConfigSuffixes());

		isImportsEnabled = project.isImportsEnabled();

		configs = new LinkedHashMap<String, IBeansConfig>();
		autoDetectedConfigs = new LinkedHashMap<String, IBeansConfig>();
		for (IBeansConfig config : project.getConfigs()) {
			if (config.getType() == IBeansConfig.Type.MANUAL) {
				configs.put(config.getElementName(), new PropertiesConfig(this, config
						.getElementName(), config.getType()));
			}
			else {
				autoDetectedConfigs.put(config.getElementName(), new PropertiesConfig(this, config
						.getElementName(), config.getType()));
			}
		}

		configSets = new LinkedHashMap<String, IBeansConfigSet>();
		for (IBeansConfigSet configSet : project.getConfigSets()) {
			configSets.put(configSet.getElementName(), new PropertiesConfigSet(this, configSet));
		}

	}

	@Override
	public void setConfigSuffixes(Set<String> suffixes) {
		super.setConfigSuffixes(suffixes);
	}

	@Override
	public boolean addConfigSuffix(String suffix) {
		if (super.addConfigSuffix(suffix)) {
			notifyListeners();
			return true;
		}
		return false;
	}

	@Override
	public void setConfigs(Set<String> configNames) {
		super.setConfigs(configNames);
		notifyListeners();
	}

	@Override
	public boolean addConfig(String configName, IBeansConfig.Type type) {
		if (super.addConfig(configName, type)) {
			notifyListeners();
			return true;
		}
		return false;
	}

	@Override
	public boolean removeConfig(String configName) {
		if (super.removeConfig(configName)) {
			notifyListeners();
			return true;
		}
		return false;
	}

	@Override
	public void setConfigSets(Set<IBeansConfigSet> configSets) {
		super.setConfigSets(configSets);
		notifyListeners();
	}

	@Override
	public boolean addConfigSet(IBeansConfigSet configSet) {
		if (super.addConfigSet(configSet)) {
			notifyListeners();
			return true;
		}
		return false;
	}

	@Override
	public void removeConfigSet(String configSetName) {
		super.removeConfigSet(configSetName);
		notifyListeners();
	}

	protected final void notifyListeners() {
		PropertiesModel model = (PropertiesModel) getElementParent();
		model.notifyListeners(this, Type.CHANGED);
	}
}
