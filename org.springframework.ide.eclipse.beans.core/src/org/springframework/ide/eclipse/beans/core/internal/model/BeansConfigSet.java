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

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanAlias;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.model.AbstractResourceModelElement;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * This class defines a Spring beans config set (a list of beans config names).
 * @author Torsten Juergeleit
 */
public class BeansConfigSet extends AbstractResourceModelElement
												   implements IBeansConfigSet {
	private boolean allowAliasOverriding;
	private boolean allowBeanDefinitionOverriding;
	private boolean isIncomplete;

	private Set<String> configNames;
	private Map<String, IBeanAlias> aliasesMap;
	private Set<IBeansComponent> components;
	private Map<String, IBean> beansMap;
	private Map<String, Set<IBean>> beanClassesMap;

	public BeansConfigSet(IBeansProject project, String name) {
		this(project, name, new HashSet<String>());
	}

	public BeansConfigSet(IBeansProject project, String name,
			Set<String> configNames) {
		super(project, name);
		this.configNames = new LinkedHashSet<String>(configNames);
		allowAliasOverriding = true;
		allowBeanDefinitionOverriding = true;
	}

	/**
	 * Sets internal maps with <code>IBean</code>s and bean classes to
	 * <code>null</code>.
	 */
	public void reset() {
		aliasesMap = null;
		components= null;
		beansMap = null;
		beanClassesMap = null;
	}

	public int getElementType() {
		return IBeansModelElementTypes.CONFIG_SET_TYPE;
	}

	public IModelElement[] getElementChildren() {
		Set<IModelElement> children = new LinkedHashSet<IModelElement>();
		for (String configName : configNames) {
			IBeansConfig config = BeansModelUtils.getConfig(configName, this);
			if (config != null) {
				children.add(config);
			}
		}
		return children.toArray(new IModelElement[children.size()]);
	}

	public IResource getElementResource() {
		return ((IBeansProject) getElementParent()).getProject();
	}

	public boolean isElementArchived() {
		return false;
	}

	public void setAllowAliasOverriding(boolean allowAliasOverriding) {
		this.allowAliasOverriding = allowAliasOverriding;
		reset();
	}

	public boolean isAllowAliasOverriding() {
		return allowAliasOverriding;
	}

	public void setAllowBeanDefinitionOverriding(
									   boolean allowBeanDefinitionOverriding) {
		this.allowBeanDefinitionOverriding = allowBeanDefinitionOverriding;
		reset();
	}

	public boolean isAllowBeanDefinitionOverriding() {
		return allowBeanDefinitionOverriding;
	}

	public void setIncomplete(boolean isIncomplete) {
		this.isIncomplete = isIncomplete;
	}

	public boolean isIncomplete() {
		return isIncomplete;
	}

	public void addConfigName(String configName) {
		if (configName.length() > 0 && !configNames.contains(configName)) {
			configNames.add(configName);
			reset();
		}
	}

	public boolean hasConfig(String configName) {
		return configNames.contains(configName);
	}

	public boolean hasConfig(IFile file) {
		if (file.getProject().equals(
				((IBeansProject) getElementParent()).getProject())) {
			return configNames.contains(file.getProjectRelativePath()
					.toString());
		}
		return configNames.contains(file.getFullPath().toString());
	}

	public void removeConfig(String configName) {
		configNames.remove(configName);
		reset();
	}

	public Set<IBeansConfig> getConfigs() {
		Set<IBeansConfig> configs = new LinkedHashSet<IBeansConfig>();
		for (String configName : configNames) {
			IBeansConfig config = BeansModelUtils.getConfig(configName, this);
			if (config != null) {
				configs.add(config);
			}
		}
		return configs;
	}

	public Set<String> getConfigNames() {
		return Collections.unmodifiableSet(configNames);
	}

	public boolean hasAlias(String name) {
		return getAliasesMap().containsKey(name);
	}

	public IBeanAlias getAlias(String name) {
		return (IBeanAlias) getAliasesMap().get(name);
	}

	public Set<IBeanAlias> getAliases() {
		return new LinkedHashSet<IBeanAlias>(getAliasesMap().values());
	}

	public Set<IBeansComponent> getComponents() {
		return new LinkedHashSet<IBeansComponent>(getComponentsList());
	}

	public boolean hasBean(String name) {
		return getBeansMap().containsKey(name);
	}

	public IBean getBean(String name) {
		return getBeansMap().get(name);
	}

	public Set<IBean> getBeans() {
		return new LinkedHashSet<IBean>(getBeansMap().values());
	}

	public boolean isBeanClass(String className) {
		return getBeanClassesMap().containsKey(className);
	}

	public Set<String> getBeanClasses() {
		return new LinkedHashSet<String>(getBeanClassesMap().keySet());
	}

	public Set<IBean> getBeans(String className) {
		if (isBeanClass(className)) {
			return new LinkedHashSet<IBean>(getBeanClassesMap().get(className));
		}
		return new HashSet<IBean>();
	}

	public String toString() {
		return getElementName() + ": " + configNames.toString();
	}

	/**
	 * Returns lazily initialized map with all beans defined in this config set.
	 */
	private Map<String, IBeanAlias> getAliasesMap() {
		if (aliasesMap == null) {
			aliasesMap = new LinkedHashMap<String, IBeanAlias>();
			for (String configName : configNames) {
				IBeansConfig config = BeansModelUtils.getConfig(configName,
						this);
				if (config != null) {
					for (IBeanAlias alias : config.getAliases()) {
						if (allowAliasOverriding
								|| !aliasesMap.containsKey(alias
										.getElementName())) {
							aliasesMap.put(alias.getElementName(), alias);
						}
					}
				}
			}
		}
		return aliasesMap;
	}

	/**
	 * Returns lazily initialized list with all componets defined in this
	 * config set.
	 */
	private Set<IBeansComponent> getComponentsList() {
		if (components == null) {
			components = new LinkedHashSet<IBeansComponent>();
			for (String configName : configNames) {
				IBeansConfig config = BeansModelUtils.getConfig(configName,
						this);
				if (config != null) {
					for (IBeansComponent component : config.getComponents()) {
						components.add(component);
					}
				}
			}
		}
		return components;
	}

	/**
	 * Returns lazily initialized map with all beans defined in this config set.
	 */
	private Map<String, IBean> getBeansMap() {
		if (beansMap == null) {
			beansMap = new LinkedHashMap<String, IBean>();
			for (String configName : configNames) {
				IBeansConfig config = BeansModelUtils.getConfig(configName,
						this);
				if (config != null) {
					for (IBean bean : config.getBeans()) {
						if (allowBeanDefinitionOverriding ||
								 !beansMap.containsKey(bean.getElementName())) {
							beansMap.put(bean.getElementName(), bean);
						}
					}
				}
			}
		}
		return beansMap;
	}

	/**
	 * Returns lazily initialized map with all bean classes used in this config
	 * set.
	 */
	private Map<String, Set<IBean>> getBeanClassesMap() {
		if (beanClassesMap == null) {
			beanClassesMap = new LinkedHashMap<String, Set<IBean>>();
			for (IBean bean : getBeansMap().values()) {
				addBeanClassToMap(bean);
				for (IBean innerBean :bean.getInnerBeans()) {
					addBeanClassToMap(innerBean);
				}
			}
		}
		return beanClassesMap;
	}

	private void addBeanClassToMap(IBean bean) {

		// Get name of bean class - strip name of any inner class
		String className = bean.getClassName();
		if (className != null) {
			int pos = className.indexOf('$');
			if  (pos > 0) {
				className = className.substring(0, pos);
			}

			// Maintain a list of bean names within every entry in the
			// bean class map
			Set<IBean> beanClassBeans = beanClassesMap.get(className);
			if (beanClassBeans == null) {
				beanClassBeans = new LinkedHashSet<IBean>();
				beanClassesMap.put(className, beanClassBeans);
			}
			beanClassBeans.add(bean);
		}
	}
}
