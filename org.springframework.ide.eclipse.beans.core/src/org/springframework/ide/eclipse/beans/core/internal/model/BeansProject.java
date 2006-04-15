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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.project.BeansProjectDescription;
import org.springframework.ide.eclipse.beans.core.internal.project.BeansProjectDescriptionReader;
import org.springframework.ide.eclipse.beans.core.internal.project.BeansProjectDescriptionWriter;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.model.AbstractResourceModelElement;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;

/**
 * This class provides information for a Spring Beans project.
 *
 * @author Torsten Juergeleit
 */
public class BeansProject extends AbstractResourceModelElement
													 implements IBeansProject {
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
		ArrayList children = new ArrayList(getDescription().getConfigs());
		children.addAll(getDescription().getConfigSets());
		return (IModelElement[]) children.toArray(
										   new IModelElement[children.size()]);
	}

	public IResource getElementResource() {
		return project;
	}

	public void accept(IModelElementVisitor visitor, IProgressMonitor monitor) {

		// First visit this project
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {

			// Now ask this project's configs
			Iterator iter = getDescription().getConfigs().iterator();
			while (iter.hasNext()) {
				IModelElement element = (IModelElement) iter.next();
				element.accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}

			// Finally ask this project's config sets
			iter = getDescription().getConfigSets().iterator();
			while (iter.hasNext()) {
				IModelElement element = (IModelElement) iter.next();
				element.accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}
		}
	}

	public IProject getProject() {
		return project;
	}

	public Collection getConfigExtensions() {
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
		boolean removedConfig = getDescription().removeConfig(file);
		boolean removedExternalConfig = getDescription().
				removeExternalConfig(file);
		if (removedConfig || removedExternalConfig && doSaveDescription) {
			saveDescription();
		}
	}

	public Collection getConfigNames() {
		return getDescription().getConfigNames();
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
        }
        else {
            return getDescription().getConfig(configName);
        }
	}

	public Collection getConfigs() {
		return getDescription().getConfigs();
	}

    public boolean hasConfigSet(String configSetName) {
        IBeansConfigSet configSet = getDescription().getConfigSet(configSetName);
        return configSet != null;
    }

	public IBeansConfigSet getConfigSet(String configSetName) {
		return getDescription().getConfigSet(configSetName);
	}

	public Collection getConfigSets() {
		return getDescription().getConfigSets();
	}

	public boolean isBeanClass(String className) {
		Iterator configs = getDescription().getConfigs().iterator();
		while (configs.hasNext()) {
			IBeansConfig config = (IBeansConfig) configs.next();
			if (config.isBeanClass(className)) {
				return true;
			}
		}
		return false;
	}

	public Collection getBeanClasses() {
		Set beanClasses = new HashSet();
		Iterator configs = getDescription().getConfigs().iterator();
		while (configs.hasNext()) {
			IBeansConfig config = (IBeansConfig) configs.next();
			beanClasses.addAll(config.getBeanClasses());
		}
		return beanClasses;
	}

	public Collection getBeans(String className) {
		List beans = new ArrayList(); 
		Iterator configs = getDescription().getConfigs().iterator();
		while (configs.hasNext()) {
			IBeansConfig config = (IBeansConfig) configs.next();
			if (config.isBeanClass(className)) {
				beans.addAll(config.getBeans(className));
			}
		}
		return beans;
	}

	/**
	 * Updates the list of config extensions belonging to this project.
	 * Optionally (by setting <code>doSaveDescription</code> to
	 * <code>true</code> the modified project description is saved to disk.
	 * Saving is done after deleting all Spring IDE problem markers from the
	 * removed configs.
	 * @param extensions  list of config extensions
	 * @param doSaveDescription  if <code>true</code> then the project's
	 * 				modified configuration is saved to the config file
	 * 				<code>IBeansProject.DESCRIPTION_FILE</code>
	 */
	public void setConfigExtensions(Set extensions,
									boolean doSaveDescription) {
		getDescription().setConfigExtensions(extensions);
		if (doSaveDescription) {
			saveDescription();
		}
	}

	/**
	 * Updates the list of configs (by name) belonging to this project.
	 * Optionally (by setting <code>doSaveDescription</code> to
	 * <code>true</code> the modified project description is saved to disk.
	 * Saving is done after deleting all Spring IDE problem markers from the
	 * removed configs.
	 * @param configs  list of config names
	 * @param doSaveDescription  if <code>true</code> then the project's
	 * 				modified configuration is saved to the config file
	 * 				<code>IBeansProject.DESCRIPTION_FILE</code>
	 */
	public void setConfigs(List configs, boolean doSaveDescription) {
		BeansProjectDescription description = getDescription();

		// Look for removed config files and
		// 1. delete all problem markers from them
		// 2. remove config from any config set
		ArrayList toBeRemoved = new ArrayList();
		
		Iterator iter = description.getConfigNames().iterator();
		while (iter.hasNext()) {
			String config = (String) iter.next();
			if (!configs.contains(config)) {
				toBeRemoved.add(config);
				if (doSaveDescription) {
					IFile file = getConfig(config).getConfigFile();
					BeansCoreUtils.deleteProblemMarkers(file);
				}
			}
		}
		
		for (int i = 0; i < toBeRemoved.size(); i++)
		{
		    String config = (String) toBeRemoved.get(i);
		    description.removeConfig(config);
		}
		
		description.setConfigNames(configs);
		if (doSaveDescription) {
			saveDescription();
		}
	}

	/**
	 * Updates the <code>BeansConfigSet</code>s defined within this project.
	 * Optionally (by setting <code>doSaveDescription</code> to
	 * <code>true</code> the modified project description is saved to disk.
	 * @param configSets  list of <code>BeansConfigSet</code> instances
	 * @param doSaveDescription  if <code>true</code> then the project's
	 * 				modified configuration is saved to the config file
	 * 				<code>IBeansProject.DESCRIPTION_FILE</code>
	 * @see org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet
	 */
	public void setConfigSets(List configSets, boolean doSaveDescription) {
		BeansProjectDescription description = getDescription();
		description.setConfigSets(configSets);
		if (doSaveDescription) {
			saveDescription();
		}
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
