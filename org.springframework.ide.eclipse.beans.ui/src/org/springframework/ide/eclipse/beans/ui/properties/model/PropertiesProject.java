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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.ide.eclipse.beans.core.internal.model.BeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent.Type;

/**
 * This class holds information for a Spring Beans project. The information is
 * retrieved from a given Spring Beans project instead being read from the
 * corresponding project description XML file defined in
 * {@link IBeansProject#DESCRIPTION_FILE}. The information can be persisted by
 * calling the method {@link #saveDescription()}.
 * 
 * @author Torsten Juergeleit
 */
public class PropertiesProject extends BeansProject {

	/**
	 * Create a deep copy of given beans project.
	 */
	public PropertiesProject(IBeansProject project) {
		super(null, project.getProject());
		configExtensions = new LinkedHashSet<String>(project
				.getConfigExtensions());

		configs = new LinkedHashMap<String, IBeansConfig>();
		for (IBeansConfig config : project.getConfigs()) {
			super.addConfig(config.getElementName());
		}

		configSets = new LinkedHashMap<String, IBeansConfigSet>();
		for (IBeansConfigSet configSet : project.getConfigSets()) {
			super.addConfigSet(new PropertiesConfigSet(this, configSet));
		}
	}

	public void setConfigExtensions(Set<String> extensions) {
		super.setConfigExtensions(extensions);
	}

	public boolean addConfigExtension(String extension) {
		if (super.addConfigExtension(extension)) {
			notifyListeners();
			return true;
		}
		return false;
	}

	public void setConfigs(Set<String> configNames) {
		super.setConfigs(configNames);
		notifyListeners();
	}
		
	public boolean addConfig(String configName) {
		if (super.addConfig(configName)) {
			notifyListeners();
			return true;
		}
		return false;
	}

	public boolean removeConfig(String configName) {
		if (super.removeConfig(configName)) {
			notifyListeners();
			return true;
		}
		return false;
	}

	public void setConfigSets(Set<IBeansConfigSet> configSets) {
		super.setConfigSets(configSets);
		notifyListeners();
	}

	public boolean addConfigSet(IBeansConfigSet configSet) {
		if (super.addConfigSet(configSet)) {
			notifyListeners();
			return true;
		}
		return false;
	}

	public void removeConfigSet(String configSetName) {
		super.removeConfigSet(configSetName);
		notifyListeners();
	}

	protected final void notifyListeners() {
		PropertiesModel model = (PropertiesModel) getElementParent();
		model.notifyListeners(this, Type.CHANGED);
	}
}
