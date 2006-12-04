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

package org.springframework.ide.eclipse.beans.core.internal.model;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.project.BeansProjectDescription;
import org.springframework.ide.eclipse.beans.core.internal.project.BeansProjectDescriptionReader;
import org.springframework.ide.eclipse.beans.core.internal.project.BeansProjectDescriptionWriter;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.model.AbstractResourceModelElement;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.ModelUtils;

/**
 * This class provides information for a Spring Beans project.
 *
 * @author Torsten Juergeleit
 */
public class BeansProject extends AbstractResourceModelElement implements
		IBeansProject {
	private IProject project;
	private BeansProjectDescription description;

	public BeansProject(IProject project) {
		super(BeansCorePlugin.getModel(), project.getName());
		this.project = project;
	}

	public int getElementType() {
		return IBeansModelElementTypes.PROJECT_TYPE;
	}

	public IModelElement[] getElementChildren() {
		Set<IModelElement> children = new LinkedHashSet<IModelElement>(
				getDescription().getConfigs());
		children.addAll(getDescription().getConfigSets());
		return children.toArray(new IModelElement[children.size()]);
	}

	public IResource getElementResource() {
		return project;
	}

	public boolean isElementArchived() {
		return false;
	}

	public void accept(IModelElementVisitor visitor,
			IProgressMonitor monitor) {

		// First visit this project
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {

			// Now ask this project's configs
			for (IBeansConfig config : getDescription().getConfigs()) {
				config.accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}

			// Finally ask this project's config sets
			for (IBeansConfigSet configSet : getDescription().getConfigSets()) {
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

	public Set<String> getConfigExtensions() {
		return getDescription().getConfigExtensions();
	}

	public boolean hasConfigExtension(String extension) {
		return getDescription().getConfigExtensions().contains(extension);
	}

	/**
	 * Sets internal <code>BeansProjectDescription</code> to <code>null</code>.
	 * Any further access to the data of this instance of
	 * <code>BeansProject</code> leads to reloading of this beans project's
	 * config description file.
	 */
	public void reset() {
		description = null;
	}

	/**
	 * Adds the given beans config to the list of configs.
	 * Optionally (by setting <code>doSaveDescription</code> to
	 * <code>true</code> the modified project description is saved to disk.
	 * @param file  the config file to add
	 * @param doSaveDescription  if <code>true</code> then the project's
	 * 				modified configuration is saved to the config file
	 * 				<code>IBeansProject.DESCRIPTION_FILE</code>
	 */
	public void addConfig(IFile file, boolean doSaveDescription) {
		if (getDescription().addConfig(file) && doSaveDescription) {
			saveDescription();
		}
	}

	/**
	 * Remove the given beans config from the list of configs and from all
	 * config sets. Optionally (by setting <code>doSaveDescription</code> to
	 * <code>true</code> the modified project description is saved to disk.
	 * @param file  the config file to remove
	 * @param doSaveDescription  if <code>true</code> then the project's
	 * 				modified configuration is saved to the config file
	 * 				<code>IBeansProject.DESCRIPTION_FILE</code>
	 */
	public void removeConfig(IFile file, boolean doSaveDescription) {
		if (getDescription().removeConfig(file) && doSaveDescription) {
			saveDescription();
		}
	}

	public boolean hasConfig(IFile file) {
		return getDescription().hasConfig(file);
	}

	public boolean hasConfig(String configName) {
		return getDescription().hasConfig(configName);
	}

	public IBeansConfig getConfig(IFile configFile) {
		return getDescription().getConfig(configFile);
	}

	public IBeansConfig getConfig(String configName) {
		if (configName != null && configName.charAt(0) == '/') {
			return BeansCorePlugin.getModel().getConfig(configName);
		} else {
			return getDescription().getConfig(configName);
		}
	}

	public Set<IBeansConfig> getConfigs() {
		return getDescription().getConfigs();
	}

	public boolean hasConfigSet(String configSetName) {
		IBeansConfigSet configSet = getDescription()
				.getConfigSet(configSetName);
		return configSet != null;
	}

	public IBeansConfigSet getConfigSet(String configSetName) {
		return getDescription().getConfigSet(configSetName);
	}

	public Set<IBeansConfigSet> getConfigSets() {
		return getDescription().getConfigSets();
	}

	public boolean isBeanClass(String className) {
		for (IBeansConfig config : getDescription().getConfigs()) {
			if (config.isBeanClass(className)) {
				return true;
			}
		}
		return false;
	}

	public Set<String> getBeanClasses() {
		Set<String> beanClasses = new LinkedHashSet<String>();
		for (IBeansConfig config : getDescription().getConfigs()) {
			beanClasses.addAll(config.getBeanClasses());
		}
		return beanClasses;
	}

	public Set<IBean> getBeans(String className) {
		Set<IBean> beans = new LinkedHashSet<IBean>();
		for (IBeansConfig config : getDescription().getConfigs()) {
			if (config.isBeanClass(className)) {
				beans.addAll(config.getBeans(className));
			}
		}
		return beans;
	}

	/**
	 * Updates the list of config extensions belonging to this project.
	 * <p>
	 * The modified project description has to be saved to disk by calling
	 * <code>saveDescription()</code>.
	 * @param extensions  list of config extensions
	 */
	public void setConfigExtensions(Set<String> extensions) {
		getDescription().setConfigExtensions(extensions);
	}

	/**
	 * Updates the list of configs (by name) belonging to this project.
	 * From all removed configs the Spring IDE problem markers are deleted.
	 * <p>
	 * The modified project description has to be saved to disk by calling
	 * <code>saveDescription()</code>.
	 * @param configs  list of config names
	 */
	public void setConfigs(Set<String> configs) {
		BeansProjectDescription description = getDescription();

		// Look for removed configs and
		// 1. delete all problem markers from them
		// 2. remove config from any config set
		for (IBeansConfig config : getDescription().getConfigs()) {
			if (!configs.contains(config.getElementName())) {
				ModelUtils.deleteProblemMarkers(config);
				description.removeConfig(config.getElementName());
			}
		}
		description.setConfigNames(configs);
	}

	/**
	 * Updates the <code>BeansConfigSet</code>s defined within this project.
	 * <p>
	 * The modified project description has to be saved to disk by calling
	 * <code>saveDescription()</code>.
	 * @param configSets  list of <code>BeansConfigSet</code> instances
	 * @see org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet
	 */
	public void setConfigSets(Set<IBeansConfigSet> configSets) {
		getDescription().setConfigSets(configSets);
	}

	/**
	 * Writes the current project description to the corresponding XML file
	 * defined in <code>IBeansProject.DESCRIPTION_FILE</code>.
	 */
	public void saveDescription() {
		BeansProjectDescriptionWriter.write(project, getDescription());
	}

	public String toString() {
		return getElementName();
	}

	/**
	 * Returns the project description lazily loaded from the XML file defined
	 * in <code>IBeansProject.DESCRIPTION_FILE</code>.
	 * <p>
	 * <b>This project's nature has to be set first!!!</b> 
	 */
	private BeansProjectDescription getDescription() {
		if (description == null) {
			description = BeansProjectDescriptionReader.read(this);
		}
		return description;
	}
}
