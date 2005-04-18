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
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.project.BeansProjectDescription;
import org.springframework.ide.eclipse.beans.core.internal.project.BeansProjectDescriptionReader;
import org.springframework.ide.eclipse.beans.core.internal.project.BeansProjectDescriptionWriter;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.model.AbstractLocatableModelElement;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;

public class BeansProject extends AbstractLocatableModelElement
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

	public void accept(IModelElementVisitor visitor) {

		// First visit this project
		if (visitor.visit(this)) {

			// Now ask this project's configs
			Iterator iter = description.getConfigs().iterator();
			while (iter.hasNext()) {
				IModelElement element = (IModelElement) iter.next();
				element.accept(visitor);
			}

			// Finally ask this project's config sets
			iter = description.getConfigSets().iterator();
			while (iter.hasNext()) {
				IModelElement element = (IModelElement) iter.next();
				element.accept(visitor);
			}
		}
	}

	public IProject getProject() {
		return project;
	}

	/**
	 * Sets internal <code>BeansProjectDescription</code> to <code>null</code>.
	 * Any further access to the data of this instance of
	 * <code>BeansProject</code> leads to reloading of this beans project's
	 * config description file.
	 */
	public void reset() {
		this.description = null;
	}

	public void addConfig(IFile file) {
		getDescription().addConfig(file);
	}

	public void removeConfig(IFile file) {
		getDescription().removeConfig(file);
		BeansProjectDescriptionWriter.write(project, description);
	}

	public Collection getConfigNames() {
		return getDescription().getConfigNames();
	}

	/**
	 * Returns true if given file belongs to the list of Spring bean config
	 * files which are stored in the project description. 
	 */
	public boolean hasConfig(IFile file) {
		return getDescription().hasConfig(file);
	}

	/**
	 * Returns true if given config name belongs to the list of Spring bean
	 * config files which are stored in the project description. 
	 */
	public boolean hasConfig(String configName) {
		return getDescription().hasConfig(configName);
	}

	/**
	 * Returns <code>IBeansConfig</code> for given config file. 
	 */
	public IBeansConfig getConfig(IFile configFile) {
		return getDescription().getConfig(configFile);
	}

	/**
	 * Returns <code>IBeansConfig</code> of given name. 
	 */
	public IBeansConfig getConfig(String configName) {
		return getDescription().getConfig(configName);
	}

	/**
	 * Returns a collection of all <code>IBeansConfig</code>s defined in this
	 * project.
	 * @see org.springframework.ide.eclipse.beans.core.model.IBeansConfig
	 */
	public Collection getConfigs() {
		return getDescription().getConfigs();
	}

    public boolean hasConfigSet(String configSetName) {
        IBeansConfigSet configSet = getDescription().getConfigSet(configSetName);
        return configSet != null;
    }

	/**
	 * Returns a list of <code>IBeansConfigSet</code>s known defined within this
	 * project.
	 * @see org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet
	 */
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
	 * Updates the list of configs (by name) belonging to this project.
	 * After deleting all problem markers from configs the modified project
	 * description is saved to disk.
	 * @param configs  list of config names
	 */
	public void setConfigs(Collection configs) {
		BeansProjectDescription description = getDescription();

		// Look for removed config files and
		// 1. delete all problem markers from them
		// 2. remove config from any config set
		ArrayList toBeRemoved = new ArrayList();
		
		Iterator iter = description.getConfigNames().iterator();
		while (iter.hasNext()) {
			String config = (String) iter.next();
			if (!configs.contains(config)) {
				IFile file = getConfigFile(config);
				BeansCoreUtils.deleteProblemMarkers(file);
				toBeRemoved.add(config);
			}
		}
		
		for (int i = 0; i < toBeRemoved.size(); i++)
		{
		    String config = (String) toBeRemoved.get(i);
		    description.removeConfig(config);
		}
		
		description.setConfigNames(configs);
		BeansProjectDescriptionWriter.write(project, description);
	}

	/**
	 * Updates the <code>BeansConfigSet</code>s defined within this project.
	 * The modified project description is saved to disk.
	 * @param configSets  list of BeansConfigSet instances
	 * @see org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet
	 */
	public void setConfigSets(List configSets) {
		BeansProjectDescription description = getDescription();
		description.setConfigSets(configSets);
		BeansProjectDescriptionWriter.write(project, description);
	}

	/**
	 * Deletes all problem markers from config files.
	 */
	public void deleteProblemMarkers() {
		BeansProjectDescription description = getDescription();
		Iterator iter = description.getConfigNames().iterator();
		while (iter.hasNext()) {
			IFile file = getConfigFile((String) iter.next());
			BeansCoreUtils.deleteProblemMarkers(file);
		}
	}

	public String toString() {
		return getElementName();
	}

	/**
	 * Returns lazily loaded project description.
	 * <b>This nature's project has to be set first!!!</b> 
	 */
	private BeansProjectDescription getDescription() {
		if (description == null) {
			description = BeansProjectDescriptionReader.read(this);
		}
		return description;
	}

	private IFile getConfigFile(String configName) {
		if (configName.charAt(0) == '/') {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			return (IFile) root.findMember(configName);
		}
		return (IFile) project.findMember(configName);
	}
}
