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
package org.springframework.ide.eclipse.beans.core.internal.model;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IPersistableElement;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanAlias;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.model.AbstractResourceModelElement;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.util.ObjectUtils;

/**
 * This class defines a Spring beans config set (a list of beans config names).
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeansConfigSet extends AbstractResourceModelElement implements
		IBeansConfigSet {

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
	private final Lock r = rwl.readLock();
	private final Lock w = rwl.writeLock(); 
	
	protected Set<String> configNames;

	private boolean allowAliasOverriding;
	private boolean allowBeanDefinitionOverriding;
	private boolean isIncomplete;

	private volatile Map<String, IBeanAlias> aliasesMap;
	private volatile boolean isAliasesMapPopulated = false;
	private volatile Set<IBeansComponent> components;
	private volatile boolean isComponentsPopulated = false;
	private volatile Map<String, IBean> beansMap;
	private volatile boolean isBeansMapPopulated = false;
	private volatile Map<String, Set<IBean>> beanClassesMap;
	private volatile boolean isBeanClassesMapPopulated = false;

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
		try {
			w.lock();
			aliasesMap = null;
			isAliasesMapPopulated = false;
			components= null;
			isComponentsPopulated = false;
			beansMap = null;
			isBeansMapPopulated = false;
			beanClassesMap = null;
			isBeanClassesMapPopulated = false;
		}
		finally {
			w.unlock();
		}
	}

	public int getElementType() {
		return IBeansModelElementTypes.CONFIG_SET_TYPE;
	}

	@Override
	public IModelElement[] getElementChildren() {
		Set<IBeansConfig> children = getConfigs();
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

	public void addConfig(String configName) {
		if (configName.length() > 0 && !getConfigNames().contains(configName)) {
			try {
				w.lock();
				configNames.add(configName);
			}
			finally {
				w.unlock();
			}
			reset();
		}
	}

	public boolean hasConfig(String configName) {
		return getConfigNames().contains(configName);
	}

	public boolean hasConfig(IFile file) {
		if (file.getProject().equals(
				((IBeansProject) getElementParent()).getProject())) {
			return getConfigNames().contains(file.getProjectRelativePath()
					.toString());
		}
		return getConfigNames().contains(file.getFullPath().toString());
	}

	public void removeConfig(String configName) {
		try {
			w.lock();
			configNames.remove(configName);
		}
		finally {
			w.unlock();
		}
		reset();
	}

	public void removeAllConfigs() {
		try {
			w.lock();
			configNames.clear();
		}
		finally {
			w.unlock();
		}
		reset();
	}

	public Set<IBeansConfig> getConfigs() {
		Set<IBeansConfig> configs = new LinkedHashSet<IBeansConfig>();
		for (String configName : getConfigNames()) {
			IBeansConfig config = BeansModelUtils.getConfig(configName, this);
			if (config != null) {
				configs.add(config);
			}
		}
		return configs;
	}

	public Set<String> getConfigNames() {
		try {
			r.lock();
			return new LinkedHashSet<String>(configNames);
		}
		finally {
			r.unlock();
		}
	}

	public boolean hasAlias(String name) {
		return getAliasesMap().containsKey(name);
	}

	public IBeanAlias getAlias(String name) {
		return getAliasesMap().get(name);
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

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeansConfigSet)) {
			return false;
		}
		try {
			r.lock();
			BeansConfigSet that = (BeansConfigSet) other;
			if (!ObjectUtils.nullSafeEquals(this.configNames, that.configNames))
				return false;
			if (!ObjectUtils.nullSafeEquals(this.allowAliasOverriding,
					that.allowAliasOverriding))
				return false;
			if (!ObjectUtils.nullSafeEquals(this.allowBeanDefinitionOverriding,
					that.allowBeanDefinitionOverriding))
				return false;
			if (!ObjectUtils.nullSafeEquals(this.isIncomplete, that.isIncomplete))
				return false;
			return super.equals(other);
		}
		finally {
			r.unlock();
		}
	}

	@Override
	public int hashCode() {
		try {
			r.lock();
			int hashCode = ObjectUtils.nullSafeHashCode(configNames);
			hashCode = getElementType() * hashCode
					+ ObjectUtils.nullSafeHashCode(allowAliasOverriding);
			hashCode = getElementType() * hashCode
					+ ObjectUtils.nullSafeHashCode(allowBeanDefinitionOverriding);
			hashCode = getElementType() * hashCode
					+ ObjectUtils.nullSafeHashCode(isIncomplete);
			return getElementType() * hashCode + super.hashCode();
		}
		finally {
			r.unlock();
		}
	}

	@Override
	public String toString() {
		try {
			r.lock();
			return getElementName() + ": " + configNames.toString();
		}
		finally {
			r.unlock();
		}
	}

	/**
	 * Returns lazily initialized map with all beans defined in this config set.
	 */
	private Map<String, IBeanAlias> getAliasesMap() {
		if (!this.isAliasesMapPopulated) {
			try {
				w.lock();
				if (this.isAliasesMapPopulated) {
					return aliasesMap;
				}
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
			finally {
				this.isAliasesMapPopulated = true;
				w.unlock();
			}
		}
		try {
			r.lock();
			return aliasesMap;
		}
		finally {
			r.unlock();
		}
	}

	/**
	 * Returns lazily initialized list with all componets defined in this
	 * config set.
	 */
	private Set<IBeansComponent> getComponentsList() {
		if (!this.isComponentsPopulated) {
			try {
				w.lock();
				if (this.isComponentsPopulated) {
					return components;
				}
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
			finally {
				this.isComponentsPopulated = true;
				w.unlock();
			}
		}
		try {
			r.lock();
			return components;
		}
		finally {
			r.unlock();
		}
	}

	/**
	 * Returns lazily initialized map with all beans defined in this config set.
	 */
	private Map<String, IBean> getBeansMap() {
		if (!this.isBeansMapPopulated) {
			try {
				w.lock();
				if (this.isBeansMapPopulated) {
					return beansMap;
				}
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
			finally {
				this.isBeansMapPopulated = true;
				w.unlock();
			}
		}
		try {
			r.lock();
			return beansMap;
		}
		finally {
			r.unlock();
		}
	}

	/**
	 * Returns lazily initialized map with all bean classes used in this config
	 * set.
	 */
	private Map<String, Set<IBean>> getBeanClassesMap() {
		if (!this.isBeanClassesMapPopulated) {
			try {
				w.lock();
				if (this.isBeanClassesMapPopulated) {
					return beanClassesMap;
				}
				beanClassesMap = new LinkedHashMap<String, Set<IBean>>();
				for (IBean bean : getBeansMap().values()) {
					addBeanClassToMap(bean);
					for (IBean innerBean : BeansModelUtils.getInnerBeans(bean)) {
						addBeanClassToMap(innerBean);
					}
				}
			}
			finally {
				this.isBeanClassesMapPopulated = true;
				w.unlock();
			}
		}
		try {
			r.lock();
			return beanClassesMap;
		}
		finally {
			r.unlock();
		}
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
	
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IPersistableElement.class) {
			return new BeansModelElementToPersistableElementAdapter(this);
		}
		else if (adapter == IProject.class) {
			if (getElementParent() instanceof IBeansProject) {
				return ((IBeansProject) getElementParent()).getProject();
			}
		}
		return super.getAdapter(adapter);
	}
}
