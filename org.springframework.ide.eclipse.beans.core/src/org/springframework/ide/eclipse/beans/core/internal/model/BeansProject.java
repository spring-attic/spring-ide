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

package org.springframework.ide.eclipse.beans.core.internal.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.project.BeansProjectDescriptionReader;
import org.springframework.ide.eclipse.beans.core.internal.project.BeansProjectDescriptionWriter;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.model.AbstractResourceModelElement;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.ModelUtils;
import org.springframework.util.ObjectUtils;

/**
 * This class provides information for a Spring Beans project.
 * 
 * @author Torsten Juergeleit
 */
public class BeansProject extends AbstractResourceModelElement implements
		IBeansProject {

	private IProject project;
	private Set<String> configExtensions;
	private Map<String, IBeansConfig> configs;
	private Map<String, IBeansConfigSet> configSets;

	/**
	 * Create a deep copy of given project associated with the given model.
	 */
	public BeansProject(IBeansModel model, IBeansProject project) {
		this(model, project.getProject());

		configExtensions = new LinkedHashSet<String>();
		configExtensions.addAll(project.getConfigExtensions());

		configs = new LinkedHashMap<String, IBeansConfig>();
		for (IBeansConfig config : project.getConfigs()) {
			addConfig(config.getElementName());
		}

		configSets = new LinkedHashMap<String, IBeansConfigSet>();
		setConfigSets(project.getConfigSets());
	}

	public BeansProject(IBeansModel model, IProject project) {
		super(model, project.getName());
		this.project = project;
	}

	public int getElementType() {
		return IBeansModelElementTypes.PROJECT_TYPE;
	}

	public IModelElement[] getElementChildren() {
		Set<IModelElement> children = new LinkedHashSet<IModelElement>(
				getConfigs());
		children.addAll(getConfigSets());
		return children.toArray(new IModelElement[children.size()]);
	}

	public IResource getElementResource() {
		return project;
	}

	public boolean isElementArchived() {
		return false;
	}

	public void accept(IModelElementVisitor visitor, IProgressMonitor monitor) {

		// First visit this project
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {

			// Now ask this project's configs
			for (IBeansConfig config : getConfigs()) {
				config.accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}

			// Finally ask this project's config sets
			for (IBeansConfigSet configSet : getConfigSets()) {
				configSet.accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}
		}
	}

	public IProject getProject() {
		return project;
	}

	/**
	 * Updates the list of config extensions belonging to this project.
	 * <p>
	 * The modified project description has to be saved to disk by calling
	 * {@link #saveDescription()}.
	 * @param extensions  list of config extensions
	 */
	public void setConfigExtensions(Set<String> extensions) {
		if (configExtensions == null) {
			readDescription();
		}
		configExtensions.clear();
		configExtensions.addAll(extensions);
	}

	public boolean addConfigExtension(String extension) {
		if (extension != null && extension.length() > 0) {
			if (configExtensions == null) {
				readDescription();
			}
			if (!configExtensions.contains(extension)) {
				configExtensions.add(extension);
				return true;
			}
		}
		return false;
	}

	public Set<String> getConfigExtensions() {
		if (configExtensions == null) {
			readDescription();
		}
		return Collections.unmodifiableSet(configExtensions);
	}

	public boolean hasConfigExtension(String extension) {
		return getConfigExtensions().contains(extension);
	}

	/**
	 * Updates the list of configs (by name) belonging to this project.
	 * From all removed configs the Spring IDE problem markers are deleted.
	 * <p>
	 * The modified project description has to be saved to disk by calling
	 * {@link #saveDescription()}.
	 * @param configNames  list of config names
	 */
	public void setConfigs(Set<String> configNames) {
		if (configs == null) {
			readDescription();
		}

		// Look for removed configs and
		// 1. delete all problem markers from them
		// 2. remove config from any config set
		for (IBeansConfig config : configs.values()) {
			String configName = config.getElementName();
			if (!configNames.contains(configName)) {
				ModelUtils.deleteProblemMarkers(config);
				removeConfig(configName);
			}
		}

		// Create new list of configs
		configs.clear();
		for (String configName : configNames){
			configs.put(configName, new BeansConfig(this, configName));
		}
	}

	/**
	 * Adds the given beans config to the list of configs.
	 * Optionally (by setting <code>doSaveDescription</code> to
	 * <code>true</code> the modified project description is saved to disk.
	 * @param file  the config file to add
	 * @param doSaveDescription  if <code>true</code> then the project's
	 * 				modified configuration is saved to the config file
	 * 				{@link IBeansProject.DESCRIPTION_FILE}
	 */
	public boolean addConfig(IFile file, boolean doSaveDescription) {
		if (addConfig(getConfigName(file)) && doSaveDescription) {
			saveDescription();
			return true;
		}
		return false;
	}

	public boolean addConfig(String configName) {
		if (configs == null) {
			readDescription();
		}
		if (configName.length() > 0 && !configs.containsKey(configName)) {
			configs.put(configName, new BeansConfig(this, configName));
			return true;
		}
		return false;
	}

	/**
	 * Remove the given beans config from the list of configs and from all
	 * config sets. Optionally (by setting <code>doSaveDescription</code> to
	 * <code>true</code> the modified project description is saved to disk.
	 * @param file  the config file to remove
	 * @param doSaveDescription  if <code>true</code> then the project's
	 * 				modified configuration is saved to the config file
	 * 				{@link IBeansProject.DESCRIPTION_FILE}
	 */
	public boolean removeConfig(IFile file, boolean doSaveDescription) {
		if (file.getProject().equals(project)) {
			if (removeConfig(file.getProjectRelativePath().toString())
					&& doSaveDescription) {
				saveDescription();
				return true;
			}
			return false;
		}

		// External configs only remove from all config sets
		if (removeConfigFromConfigSets(file.getFullPath().toString())
				&& doSaveDescription) {
			saveDescription();
			return true;
		}
		return false;
	}

	public boolean removeConfig(String configName) {
		if (hasConfig(configName)) {
			configs.remove(configName);
			removeConfigFromConfigSets(configName);
			return true;
		}
		return false;
	}

	public boolean hasConfig(IFile file) {
		return hasConfig(getConfigName(file));
	}

	public boolean hasConfig(String configName) {
		if (configs == null) {
			readDescription();
		}
		return configs.containsKey(configName);
	}

	public IBeansConfig getConfig(IFile file) {
		return getConfig(getConfigName(file));
	}

	public IBeansConfig getConfig(String configName) {
		if (configName != null && configName.charAt(0) == '/') {
			return BeansCorePlugin.getModel().getConfig(configName);
		} else {
			if (configs == null) {
				readDescription();
			}
			return configs.get(configName);
		}
	}

	public Set<String> getConfigNames() {
		if (configs == null) {
			readDescription();
		}
		return new LinkedHashSet<String>(configs.keySet());
	}

	public Set<IBeansConfig> getConfigs() {
		if (configs == null) {
			readDescription();
		}
		return new LinkedHashSet<IBeansConfig>(configs.values());
	}

	/**
	 * Updates the {@link BeansConfigSet}s defined within this project.
	 * <p>
	 * The modified project description has to be saved to disk by calling
	 * {@link #saveDescription()}.
	 * @param configSets  list of {@link BeansConfigSet} instances
	 */
	public void setConfigSets(Set<IBeansConfigSet> configSets) {
		if (this.configSets == null) {
			readDescription();
		}
		this.configSets.clear();
		for (IBeansConfigSet configSet : configSets) {
			this.configSets.put(configSet.getElementName(), new BeansConfigSet(
					this, configSet));
		}
	}

	public boolean addConfigSet(IBeansConfigSet configSet) {
		if (configSets == null) {
			readDescription();
		}
		if (!configSets.values().contains(configSet)) {
			configSets.put(configSet.getElementName(), new BeansConfigSet(this,
					configSet));
			return true;
		}
		return false;
	}

	public void removeConfigSet(String configSetName) {
		configSets.remove(configSetName);
	}

	public boolean hasConfigSet(String configSetName) {
		if (configSets == null) {
			readDescription();
		}
		return configSets.containsKey(configSetName);
	}

	public IBeansConfigSet getConfigSet(String configSetName) {
		if (configSets == null) {
			readDescription();
		}
		return configSets.get(configSetName);
	}

	public Set<IBeansConfigSet> getConfigSets() {
		if (configSets == null) {
			readDescription();
		}
		return new LinkedHashSet<IBeansConfigSet>(configSets.values());
	}

	public boolean isBeanClass(String className) {
		for (IBeansConfig config : getConfigs()) {
			if (config.isBeanClass(className)) {
				return true;
			}
		}
		return false;
	}

	public Set<String> getBeanClasses() {
		Set<String> beanClasses = new LinkedHashSet<String>();
		for (IBeansConfig config : getConfigs()) {
			beanClasses.addAll(config.getBeanClasses());
		}
		return beanClasses;
	}

	public Set<IBean> getBeans(String className) {
		Set<IBean> beans = new LinkedHashSet<IBean>();
		for (IBeansConfig config : getConfigs()) {
			if (config.isBeanClass(className)) {
				beans.addAll(config.getBeans(className));
			}
		}
		return beans;
	}

	/**
	 * Writes the current project description to the corresponding XML file
	 * defined in {@link IBeansProject.DESCRIPTION_FILE}.
	 */
	public void saveDescription() {
		BeansProjectDescriptionWriter.write(this);
	}

	/**
	 * Resets the internal data. Any further access to the data of this instance
	 * of {@link BeansProject} leads to reloading of this beans project's config
	 * description file.
	 */
	public void reset() {
		configExtensions.clear();
		configs.clear();
		configSets.clear();
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeansProject)) {
			return false;
		}
		BeansProject that = (BeansProject) other;
		if (!ObjectUtils.nullSafeEquals(this.project, that.project))
			return false;
		return super.equals(other);
	}

	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(project);
		return getElementType() * hashCode + super.hashCode();
	}

	public String toString() {
		return "Project=" + getElementName() + "ConfigExtensions="
				+ configExtensions + ", Configs=" + configs.values()
				+ ", ConfigsSets=" + configSets;
	}

	private boolean removeConfigFromConfigSets(String configName) {
		if (configSets == null) {
			readDescription();
		}
		for (IBeansConfigSet configSet : configSets.values()) {
			if (configSet.hasConfig(configName)) {
				((BeansConfigSet) configSet).removeConfig(configName);
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the config name from given file. If the file belongs to this
	 * project then the config name is the project-relativ path of the given
	 * file otherwise it's the workspace-relativ path with a leading '/'.
	 */
	private String getConfigName(IFile file) {
		String configName;
		if (file.getProject().equals(project.getProject())) {
			configName = file.getProjectRelativePath().toString();
		} else {
			configName = file.getFullPath().toString();
		}
		return configName;
	}

	/**
	 * Returns the project description lazily loaded from the XML file defined
	 * in {@link IBeansProject.DESCRIPTION_FILE}.
	 * <p>
	 * <b>This project's nature has to be set first!!!</b> 
	 */
	private void readDescription() {
		configExtensions = new LinkedHashSet<String>();
		configs = new LinkedHashMap<String, IBeansConfig>();
		configSets = new LinkedHashMap<String, IBeansConfigSet>();
		BeansProjectDescriptionReader.read(this);
	}
}
