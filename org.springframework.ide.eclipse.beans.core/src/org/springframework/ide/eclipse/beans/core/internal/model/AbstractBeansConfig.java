/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.beans.factory.xml.DocumentDefaultsDefinition;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanAlias;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.beans.core.model.IImportedBeansConfig;
import org.springframework.ide.eclipse.core.model.AbstractResourceModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.IModelSourceLocation;
import org.springframework.ide.eclipse.core.model.ModelUtils;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblem;
import org.springframework.ide.eclipse.core.model.xml.XmlSourceLocation;
import org.springframework.util.ObjectUtils;

/**
 * This class gathers common functionality for core model components representing a single instance
 * of xml configuration file.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public abstract class AbstractBeansConfig extends AbstractResourceModelElement implements
		IBeansConfig {

	/** This bean's config file */
	protected IFile file;

	/** This bean config file's timestamp of last modification */
	protected long modificationTimestamp;

	/** Indicator for a beans configuration embedded in a ZIP file */
	protected boolean isArchived;

	/** Defaults values for this beans config file */
	protected volatile DocumentDefaultsDefinition defaults;

	/** List of imports (in registration order) */
	protected volatile Set<IBeansImport> imports;

	/** List of aliases (in registration order) */
	protected volatile Map<String, IBeanAlias> aliases;

	/** List of components (in registration order) */
	protected volatile Set<IBeansComponent> components;

	/** List of bean names mapped beans (in registration order) */
	protected volatile Map<String, IBean> beans;

	protected volatile Type type;

	/**
	 * List of bean class names mapped to list of beans implementing the corresponding class
	 */
	protected volatile Map<String, Set<IBean>> beanClassesMap;

	protected volatile boolean isBeanClassesMapPopulated = false;

	protected final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	protected final Lock r = rwl.readLock();

	protected final Lock w = rwl.writeLock();

	protected volatile boolean isModelPopulated = false;

	/**
	 * List of parsing errors.
	 */
	protected Set<ValidationProblem> problems;

	public AbstractBeansConfig(IBeansModelElement project, String name, Type type) {
		super(project, name);
		this.type = type;
	}

	@Override
	public void accept(IModelElementVisitor visitor, IProgressMonitor monitor) {
		// First visit this config
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {

			// Now ask this config's imports
			for (IBeansImport imp : getImports()) {
				imp.accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}

			// Now ask this config's aliases
			for (IBeanAlias alias : getAliases()) {
				alias.accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}

			// Now ask this config's components
			for (IBeansComponent component : getComponents()) {
				component.accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}

			// Finally ask this configs's beans
			for (IBean bean : getBeans()) {
				bean.accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}
		}
	}

	private void addBeanClass(IBean bean, Map<String, Set<IBean>> beanClasses) {

		// Get name of bean class - strip name of any inner class
		String className = bean.getClassName();
		if (className != null) {
			int pos = className.indexOf('$');
			if (pos > 0) {
				className = className.substring(0, pos);
			}

			// Maintain a list of bean names within every entry in the
			// bean class map
			Set<IBean> beanClassBeans = beanClasses.get(className);
			if (beanClassBeans == null) {
				beanClassBeans = new LinkedHashSet<IBean>();
				beanClasses.put(className, beanClassBeans);
			}
			beanClassBeans.add(bean);
		}
	}

	private void addBeanClasses(IBean bean, Map<String, Set<IBean>> beanClasses) {
		addBeanClass(bean, beanClasses);
		for (IBean innerBean : BeansModelUtils.getInnerBeans(bean)) {
			addBeanClass(innerBean, beanClasses);
		}
	}

	private void addComponentBeanClasses(IBeansComponent component,
			Map<String, Set<IBean>> beanClasses) {
		for (IBean bean : component.getBeans()) {
			addBeanClasses(bean, beanClasses);
		}
		for (IBeansComponent innerComponent : component.getComponents()) {
			addComponentBeanClasses(innerComponent, beanClasses);
		}
	}

	private boolean changedImportedBeansConfig() {
		try {
			r.lock();
			for (IBeansImport beanImport : imports) {
				for (IImportedBeansConfig importedConfig : beanImport.getImportedBeansConfigs()) {
					if (importedConfig.resourceChanged()) {
						return true;
					}
				}
			}
		}
		finally {
			r.unlock();
		}
		return false;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AbstractBeansConfig)) {
			return false;
		}
		AbstractBeansConfig that = (AbstractBeansConfig) other;
		if (!ObjectUtils.nullSafeEquals(this.isArchived, that.isArchived))
			return false;
		if (this.defaults != null && that.defaults != null && this.defaults != that.defaults) {
			if (!ObjectUtils.nullSafeEquals(this.defaults.getLazyInit(), that.defaults
					.getLazyInit()))
				return false;
			if (!ObjectUtils.nullSafeEquals(this.defaults.getAutowire(), that.defaults
					.getAutowire()))
				return false;
			if (!ObjectUtils.nullSafeEquals(this.defaults.getDependencyCheck(), that.defaults
					.getDependencyCheck()))
				return false;
			if (!ObjectUtils.nullSafeEquals(this.defaults.getInitMethod(), that.defaults
					.getInitMethod()))
				return false;
			if (!ObjectUtils.nullSafeEquals(this.defaults.getDestroyMethod(), that.defaults
					.getDestroyMethod()))
				return false;
			if (!ObjectUtils.nullSafeEquals(this.defaults.getMerge(), that.defaults.getMerge()))
				return false;
		}
		return super.equals(other);
	}

	public IBeanAlias getAlias(String name) {
		if (name != null) {
			try {
				r.lock();
				IBeanAlias alias = aliases.get(name);
				if (alias != null) {
					return alias;
				}

				for (IBeansImport beansImport : imports) {
					for (IBeansConfig bc : beansImport.getImportedBeansConfigs()) {
						alias = bc.getAlias(name);
						if (alias != null) {
							return alias;
						}
					}
				}
			}
			finally {
				r.unlock();
			}
		}
		return null;
	}

	public Set<IBeanAlias> getAliases() {
		// Lazily initialization of this config
		readConfig();

		try {
			r.lock();
			Set<IBeanAlias> allAliases = new LinkedHashSet<IBeanAlias>(aliases.values());
			for (IBeansImport beansImport : imports) {
				for (IBeansConfig bc : beansImport.getImportedBeansConfigs()) {
					allAliases.addAll(bc.getAliases());
				}
			}
			return Collections.unmodifiableSet(new LinkedHashSet<IBeanAlias>(allAliases));
		}
		finally {
			r.unlock();
		}
	}

	public IBean getBean(String name) {
		if (name != null) {
			// Lazily initialization of this config
			readConfig();

			try {
				r.lock();
				IBean bean = beans.get(name);
				if (bean != null) {
					return bean;
				}

				for (IBeansImport beansImport : imports) {
					for (IBeansConfig bc : beansImport.getImportedBeansConfigs()) {
						bean = bc.getBean(name);
						if (bean != null) {
							return bean;
						}
					}
				}
			}
			finally {
				r.unlock();
			}
		}
		return null;
	}

	public Set<String> getBeanClasses() {
		return Collections.unmodifiableSet(new LinkedHashSet<String>(getBeanClassesMap().keySet()));
	}

	/**
	 * Returns lazily initialized map with all bean classes used in this config.
	 */
	protected Map<String, Set<IBean>> getBeanClassesMap() {
		if (!this.isBeanClassesMapPopulated) {
			try {
				w.lock();
				if (this.isBeanClassesMapPopulated) {
					return beanClassesMap;
				}
				beanClassesMap = new LinkedHashMap<String, Set<IBean>>();
				for (IBeansComponent component : getComponents()) {
					addComponentBeanClasses(component, beanClassesMap);
				}
				for (IBean bean : getBeans()) {
					addBeanClasses(bean, beanClassesMap);
				}
				for (IBeansImport beansImport : imports) {
					for (IBeansConfig bc : beansImport.getImportedBeansConfigs()) {
						for (IBeansComponent component : bc.getComponents()) {
							addComponentBeanClasses(component, beanClassesMap);
						}
						for (IBean bean : bc.getBeans()) {
							addBeanClasses(bean, beanClassesMap);
						}
					}
				}

			}
			finally {
				this.isBeanClassesMapPopulated = true;
				w.unlock();
			}
		}
		return beanClassesMap;
	}

	public Set<IBean> getBeans() {
		// Lazily initialization of this config
		readConfig();

		try {
			r.lock();
			Set<IBean> allBeans = new LinkedHashSet<IBean>(beans.values());
			for (IBeansImport beansImport : imports) {
				for (IBeansConfig bc : beansImport.getImportedBeansConfigs()) {
					allBeans.addAll(bc.getBeans());
				}
			}

			return Collections.unmodifiableSet(new LinkedHashSet<IBean>(allBeans));
		}
		finally {
			r.unlock();
		}
	}

	public Set<IBean> getBeans(String className) {
		if (isBeanClass(className)) {
			return Collections.unmodifiableSet(getBeanClassesMap().get(className));
		}
		return new HashSet<IBean>();
	}

	public Set<IBeansComponent> getComponents() {
		// Lazily initialization of this config
		readConfig();

		try {
			r.lock();
			Set<IBeansComponent> allComponents = new LinkedHashSet<IBeansComponent>(components);
			for (IBeansImport beansImport : imports) {
				for (IBeansConfig bc : beansImport.getImportedBeansConfigs()) {
					allComponents.addAll(bc.getComponents());
				}
			}

			return Collections.unmodifiableSet(new LinkedHashSet<IBeansComponent>(allComponents));
		}
		finally {
			r.unlock();
		}
	}

	public String getDefaultAutowire() {
		// Lazily initialization of this config
		readConfig();

		try {
			r.lock();
			return (defaults != null ? defaults.getAutowire() : DEFAULT_AUTO_WIRE);
		}
		finally {
			r.unlock();
		}
	}

	public String getDefaultDependencyCheck() {
		// Lazily initialization of this config
		readConfig();

		try {
			r.lock();
			return (defaults != null ? defaults.getDependencyCheck() : DEFAULT_DEPENDENCY_CHECK);
		}
		finally {
			r.unlock();
		}
	}

	public String getDefaultDestroyMethod() {
		// Lazily initialization of this config
		readConfig();

		try {
			r.lock();
			return (defaults != null && defaults.getDestroyMethod() != null ? defaults
					.getDestroyMethod() : DEFAULT_DESTROY_METHOD);
		}
		finally {
			r.unlock();
		}
	}

	public String getDefaultInitMethod() {
		// Lazily initialization of this config
		readConfig();

		try {
			r.lock();
			return (defaults != null && defaults.getInitMethod() != null ? defaults.getInitMethod()
					: DEFAULT_INIT_METHOD);
		}
		finally {
			r.unlock();
		}
	}

	public String getDefaultLazyInit() {
		// Lazily initialization of this config
		readConfig();

		try {
			r.lock();
			return (defaults != null ? defaults.getLazyInit() : DEFAULT_LAZY_INIT);
		}
		finally {
			r.unlock();
		}
	}

	public String getDefaultMerge() {
		// Lazily initialization of this config
		readConfig();

		try {
			r.lock();
			// This default value was introduced with Spring 2.0 -> so we have
			// to check for an empty string here as well
			return (defaults != null && defaults.getMerge() != null
					&& defaults.getMerge().length() > 0 ? defaults.getMerge() : DEFAULT_MERGE);
		}
		finally {
			r.unlock();
		}

	}

	public final IResource getElementResource() {
		return file;
	}

	public final int getElementStartLine() {
		// Lazily initialization of this config
		readConfig();

		IModelSourceLocation location = ModelUtils.getSourceLocation(defaults);
		return (location != null ? location.getStartLine() : -1);
	}

	public final int getElementType() {
		return IBeansModelElementTypes.CONFIG_TYPE;
	}

	public Set<IBeansImport> getImports() {
		// Lazily initialization of this config
		readConfig();

		try {
			r.lock();
			return Collections.unmodifiableSet(imports);
		}
		finally {
			r.unlock();
		}
	}

	public final Set<ValidationProblem> getProblems() {
		// Lazily initialization of this config
		readConfig();

		try {
			r.lock();
			return problems;
		}
		finally {
			r.unlock();
		}
	}

	public boolean hasBean(String name) {
		if (name != null) {
			// Lazily initialization of this config
			readConfig();

			try {
				r.lock();
				IBean bean = beans.get(name);
				if (bean != null) {
					return true;
				}

				for (IBeansImport beansImport : imports) {
					for (IBeansConfig bc : beansImport.getImportedBeansConfigs()) {
						bean = bc.getBean(name);
						if (bean != null) {
							return true;
						}
					}
				}

				return false;
			}
			finally {
				r.unlock();
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(isArchived);
		if (defaults != null) {
			hashCode = getElementType() * hashCode
					+ ObjectUtils.nullSafeHashCode(defaults.getLazyInit());
			hashCode = getElementType() * hashCode
					+ ObjectUtils.nullSafeHashCode(defaults.getAutowire());
			hashCode = getElementType() * hashCode
					+ ObjectUtils.nullSafeHashCode(defaults.getDependencyCheck());
			hashCode = getElementType() * hashCode
					+ ObjectUtils.nullSafeHashCode(defaults.getInitMethod());
			hashCode = getElementType() * hashCode
					+ ObjectUtils.nullSafeHashCode(defaults.getDestroyMethod());
			hashCode = getElementType() * hashCode
					+ ObjectUtils.nullSafeHashCode(defaults.getMerge());
		}
		return getElementType() * hashCode + super.hashCode();
	}

	public boolean isBeanClass(String className) {
		if (className != null) {
			return getBeanClassesMap().containsKey(className);
		}
		return false;
	}

	public final boolean isElementArchived() {
		return isArchived;
	}

	protected abstract void readConfig();

	public boolean resourceChanged() {
		return modificationTimestamp < file.getModificationStamp() || changedImportedBeansConfig();
	}

	@Override
	public String toString() {
		return getElementName() + ": " + getBeans();
	}

	public Type getType() {
		return type;
	}

	public boolean configAffectedByJavaChange(IResource resource) {
		for (IBeansComponent component : getComponents()) {
			if (component.getElementSourceLocation() instanceof XmlSourceLocation) {
				XmlSourceLocation location = (XmlSourceLocation) component
						.getElementSourceLocation();
				if ("component-scan".equals(location.getLocalName())
						&& "http://www.springframework.org/schema/context".equals(location
								.getNamespaceURI())) {
					return true;
				}
			}
		}
		return false;
	}

}
