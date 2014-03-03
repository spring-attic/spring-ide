/*******************************************************************************
 * Copyright (c) 2004, 2014 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.project.BeansProjectDescriptionReader;
import org.springframework.ide.eclipse.beans.core.internal.project.BeansProjectDescriptionWriter;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig.Type;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigEventListener;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.locate.BeansConfigLocatorDefinition;
import org.springframework.ide.eclipse.beans.core.model.locate.BeansConfigLocatorFactory;
import org.springframework.ide.eclipse.beans.core.model.locate.IBeansConfigLocator;
import org.springframework.ide.eclipse.beans.core.model.locate.IJavaConfigLocator;
import org.springframework.ide.eclipse.beans.core.model.process.IBeansConfigPostProcessor;
import org.springframework.ide.eclipse.core.MarkerUtils;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.model.AbstractModel;
import org.springframework.ide.eclipse.core.model.AbstractResourceModelElement;
import org.springframework.ide.eclipse.core.model.ILazyInitializedModelElement;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent;
import org.springframework.util.ObjectUtils;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;

/**
 * This class holds information for a Spring Beans project. The information is
 * lazily read from the corresponding project description XML file defined in
 * {@link IBeansProject#DESCRIPTION_FILE}.
 * <p>
 * The information can be persisted by calling the method
 * {@link #saveDescription()}.
 * 
 * @author Torsten Juergeleit
 * @author Dave Watkins
 * @author Christian Dupuis
 * @author Martin Lippert
 * @author Leo Dos Santos
 */
public class BeansProject extends AbstractResourceModelElement implements IBeansProject, ILazyInitializedModelElement {

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	private final Lock r = rwl.readLock();

	private final Lock w = rwl.writeLock();

	protected volatile boolean modelPopulated = false;

	private final IProject project;

	protected volatile Set<String> configSuffixes = new LinkedHashSet<String>();

	/** the internal flag to specify if import processing is enabled */
	protected volatile boolean isImportsEnabled = DEFAULT_IMPORTS_ENABLED;

	/** Internal version number; intentionally set to lower value */
	protected volatile String version = "2.0.0";

	protected volatile Map<String, IBeansConfig> configs = new LinkedHashMap<String, IBeansConfig>();
	protected volatile Map<String, IBeansConfig> autoDetectedConfigs = new LinkedHashMap<String, IBeansConfig>();
	protected volatile Set<IBeansConfig> allConfigs = Collections.unmodifiableSet(new CopyOnWriteArraySet<IBeansConfig>());

	protected volatile Map<String, Set<String>> autoDetectedConfigsByLocator = new LinkedHashMap<String, Set<String>>();

	protected volatile Map<String, String> locatorByAutoDetectedConfig = new LinkedHashMap<String, String>();

	protected volatile Map<String, IBeansConfigSet> configSets = new LinkedHashMap<String, IBeansConfigSet>();

	protected volatile Map<String, IBeansConfigSet> autoDetectedConfigSets = new LinkedHashMap<String, IBeansConfigSet>();

	protected volatile Map<String, String> autoDetectedConfigSetsByLocator = new LinkedHashMap<String, String>();

	protected volatile IBeansConfigEventListener eventListener;

	private boolean isAutoConfigStatePersisted = false;

	public BeansProject(IBeansModel model, IProject project) {
		super(model, project.getName());
		this.project = project;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getElementType() {
		return IBeansModelElementTypes.PROJECT_TYPE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IModelElement[] getElementChildren() {
		Set<IModelElement> children = new LinkedHashSet<IModelElement>(getConfigs());
		children.addAll(getConfigSets());
		return children.toArray(new IModelElement[children.size()]);
	}

	/**
	 * {@inheritDoc}
	 */
	public IResource getElementResource() {
		return project;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isElementArchived() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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

	/**
	 * {@inheritDoc}
	 */
	public IProject getProject() {
		return project;
	}

	/**
	 * Updates the list of config suffixes belonging to this project.
	 * <p>
	 * The modified project description has to be saved to disk by calling
	 * {@link #saveDescription()}.
	 * 
	 * @param suffixes
	 *            list of config suffixes
	 */
	public void setConfigSuffixes(Set<String> suffixes) {
		if (!this.modelPopulated) {
			populateModel();
		}
		try {
			w.lock();
			configSuffixes.clear();
			configSuffixes.addAll(suffixes);
		} finally {
			w.unlock();
		}
	}

	public boolean addConfigSuffix(String suffix) {
		if (suffix != null && suffix.length() > 0) {
			if (!this.modelPopulated) {
				populateModel();
			}
			try {
				w.lock();
				if (!configSuffixes.contains(suffix)) {
					configSuffixes.add(suffix);
					return true;
				}
			} finally {
				w.unlock();
			}
		}
		return false;
	}

	public Set<String> getConfigSuffixes() {
		if (!this.modelPopulated) {
			populateModel();
		}
		try {
			r.lock();
			return Collections.unmodifiableSet(configSuffixes);
		} finally {
			r.unlock();
		}
	}

	/**
	 * @deprecated use {@link #getConfigSuffixes()} instead.
	 */
	@Deprecated
	public Set<String> getConfigExtensions() {
		return getConfigSuffixes();
	}

	public boolean hasConfigSuffix(String suffix) {
		try {
			r.lock();
			return getConfigSuffixes().contains(suffix);
		} finally {
			r.unlock();
		}
	}

	/**
	 * @deprecated use {@link #hasConfigSuffix(String)} instead.
	 */
	@Deprecated
	public boolean hasConfigExtension(String extension) {
		return hasConfigSuffix(extension);
	}

	/**
	 * Updates the list of configs (by name) belonging to this project. From all
	 * removed configs the Spring IDE problem markers are deleted.
	 * <p>
	 * The modified project description has to be saved to disk by calling
	 * {@link #saveDescription()}.
	 * 
	 * @param configNames
	 *            list of config names
	 */
	public void setConfigs(Set<String> configNames) {
		if (!this.modelPopulated) {
			populateModel();
		}
		List<IResource> deleteMarkersFrom = new ArrayList<IResource>();
		try {
			w.lock();
			// Look for removed configs and
			// 1. delete all problem markers from them
			// 2. remove config from any config set
			for (IBeansConfig config : configs.values()) {
				String configName = config.getElementName();
				if (!configNames.contains(configName)) {
					removeConfig(configName);

					// Defer deletion of problem markers until write lock is
					// released
					deleteMarkersFrom.add(config.getElementResource());
				}
			}

			// Create new list of configs
			configs.clear();
			for (String configName : configNames) {
				configs.put(configName, BeansConfigFactory.create(this, configName, Type.MANUAL));
			}
		} finally {
			updateAllConfigsCache();
			w.unlock();
		}

		// Delete the problem markers after the write lock is released -
		// otherwise this may be interfering with a ResourceChangeListener
		// referring to this beans project
		for (IResource configResource : deleteMarkersFrom) {
			MarkerUtils.deleteMarkers(configResource, SpringCore.MARKER_ID);
		}
	}

	/**
	 * Adds the given beans config file's name to the list of configs.
	 * <p>
	 * The modified project description has to be saved to disk by calling
	 * {@link #saveDescription()}.
	 * 
	 * @param file
	 *            the config file to add
	 * @return <code>true</code> if config file was added to this project
	 */
	public boolean addConfig(IFile file, IBeansConfig.Type type) {
		return addConfig(this.getConfigName(file), type);
	}

	/**
	 * Adds the given beans config to the list of configs.
	 * <p>
	 * The modified project description has to be saved to disk by calling
	 * {@link #saveDescription()}.
	 * 
	 * @param configName
	 *            the config name to add
	 * @return <code>true</code> if config was added to this project
	 */
	public boolean addConfig(String configName, IBeansConfig.Type type) {
		if (!this.modelPopulated) {
			populateModel();
		}
		try {
			w.lock();
			if (configName.length() > 0 && !configs.containsKey(configName)) {
				if (type == IBeansConfig.Type.MANUAL) {
					IBeansConfig config = BeansConfigFactory.create(this, configName, type);
					addConfig(config);
					return true;
				} else if (type == IBeansConfig.Type.AUTO_DETECTED && !autoDetectedConfigs.containsKey(configName)) {
					populateAutoDetectedConfigsAndConfigSets(null);
					return true;
				}
			}
		} finally {
			updateAllConfigsCache();
			w.unlock();
		}
		return false;
	}

	/**
	 * Adds the given beans config to the list of configs.
	 * <p>
	 * The modified project description has to be saved to disk by calling
	 * {@link #saveDescription()}.
	 * 
	 * @param config
	 *            the config to add
	 * @return <code>true</code> if config file was added to this project
	 */
	private boolean addConfig(IBeansConfig config) {
		String configName = config.getElementName();

		if (configs.containsKey(configName)) {
			return false;
		}

		configs.put(configName, config);
		config.registerEventListener(eventListener);

		if (autoDetectedConfigs.containsKey(configName)) {
			autoDetectedConfigs.remove(configName);
			String locatorId = locatorByAutoDetectedConfig.remove(configName);
			if (locatorId != null && autoDetectedConfigsByLocator.containsKey(locatorId)) {
				autoDetectedConfigsByLocator.get(locatorId).remove(configName);
			}
		}
		return true;
	}

	/**
	 * I * Removes the given beans config from the list of configs and from all
	 * config sets.
	 * <p>
	 * The modified project description has to be saved to disk by calling
	 * {@link #saveDescription()}.
	 * 
	 * @param file
	 *            the config file to remove
	 * @return <code>true</code> if config was removed to this project
	 */
	public boolean removeConfig(IFile file) {
		if (file.getProject().equals(project)) {
			return removeConfig(getConfigName(file));
		}

		// External configs only remove from all config sets
		return removeConfigFromConfigSets(getConfigName(file));
	}

	/**
	 * Removes the given beans config from the list of configs and from all
	 * config sets.
	 * <p>
	 * The modified project description has to be saved to disk by calling
	 * {@link #saveDescription()}.
	 * 
	 * @param configName
	 *            the config name to remove
	 * @return <code>true</code> if config was removed to this project
	 */
	public boolean removeConfig(String configName) {
		if (hasConfig(configName)) {
			try {
				w.lock();
				IBeansConfig config = configs.remove(configName);
				IBeansConfig autoDetectedConfig = autoDetectedConfigs.remove(configName);
				if (config != null) {
					config.unregisterEventListener(eventListener);
				}
				if (autoDetectedConfig != null) {
					autoDetectedConfig.unregisterEventListener(eventListener);
				}
				String locatorId = locatorByAutoDetectedConfig.remove(configName);
				if (locatorId != null && autoDetectedConfigsByLocator.containsKey(locatorId)) {
					autoDetectedConfigsByLocator.get(locatorId).remove(configName);
				}
			} finally {
				updateAllConfigsCache();
				w.unlock();
			}
			removeConfigFromConfigSets(configName);
			return true;
		}
		return false;
	}

	public boolean hasConfig(IFile file) {
		return hasConfig(getConfigName(file));
	}

	public boolean hasConfig(String configName) {
		if (!this.modelPopulated) {
			populateModel();
		}
		try {
			r.lock();
			return (configs.containsKey(configName) || autoDetectedConfigs.containsKey(configName));
		} finally {
			r.unlock();
		}
	}

	public boolean hasConfig(IFile configFile, String configName, boolean includeImported) {
		if (hasConfig(configName)) {
			return true;
		}

		for (IBeansConfig config : getConfigs()) {
			if (config.getElementResource() != null && config.getElementResource().equals(configFile)) {
				return true;
			}
		}

		if (isImportsEnabled() && includeImported) {
			try {
				r.lock();
				for (IBeansConfig bc : getConfigs()) {
					if (hasImportedBeansConfig(configFile, bc)) {
						return true;
					}
				}
			} finally {
				r.unlock();
			}
		}
		return false;
	}

	public IBeansConfig getConfig(IFile configFile, boolean includeImported) {
		Set<IBeansConfig> beansConfigs = getConfigs(configFile, includeImported);
		Iterator<IBeansConfig> iterator = beansConfigs.iterator();
		if (iterator.hasNext()) {
			return iterator.next();
		}
		return null;
	}

	public Set<IBeansConfig> getConfigs(IFile file, boolean includeImported) {
		Set<IBeansConfig> beansConfigs = new LinkedHashSet<IBeansConfig>();

		if (file.getProject() != null && !this.project.equals(file.getProject())) {
			IBeansProject otherBeansProject = BeansCorePlugin.getModel().getProject(file.getProject());
			if (otherBeansProject != null) {
				Set<IBeansConfig> otherProjectConfigs = otherBeansProject.getConfigs(file, false);
				for (IBeansConfig otherProjectConfig : otherProjectConfigs) {
					beansConfigs.add(otherProjectConfig);
				}
			}
		}

		Set<IBeansConfig> ownConfigs = getConfigs();
		if (ownConfigs != null) {
			for (IBeansConfig config : ownConfigs) {
				if (config.getElementResource() != null && config.getElementResource().equals(file)) {
					beansConfigs.add(config);
				}
			}
		}

		// make sure that we run into the next block only if <import> support is
		// enabled
		// not executing the block will safe lots of execution time as
		// configuration files don't
		// need to get loaded.
		if ((isImportsEnabled() && includeImported)) {
			try {
				r.lock();
				if (ownConfigs != null) {
					for (IBeansConfig bc : ownConfigs) {
						checkForImportedBeansConfig(file, bc, beansConfigs);
					}
				}
			} finally {
				r.unlock();
			}
		}
		return beansConfigs;
	}

	private void checkForImportedBeansConfig(IFile file, IBeansConfig bc, Set<IBeansConfig> beansConfigs) {
		if (bc.getElementResource() != null && bc.getElementResource().equals(file)) {
			beansConfigs.add(bc);
		}

		for (IBeansImport bi : bc.getImports()) {
			for (IBeansConfig importedBc : bi.getImportedBeansConfigs()) {
				if (importedBc.getElementResource() != null && importedBc.getElementResource().equals(file)) {
					beansConfigs.add(importedBc);
				}
				for (IBeansImport iBi : importedBc.getImports()) {
					for (IBeansConfig iBc : iBi.getImportedBeansConfigs()) {
						checkForImportedBeansConfig(file, iBc, beansConfigs);
					}
				}
			}
		}
	}

	private boolean hasImportedBeansConfig(IFile file, IBeansConfig bc) {
		if (bc.getElementResource() != null && bc.getElementResource().equals(file)) {
			return true;
		}

		for (IBeansImport bi : bc.getImports()) {
			for (IBeansConfig importedBc : bi.getImportedBeansConfigs()) {
				if (importedBc.getElementResource() != null && importedBc.getElementResource().equals(file)) {
					return true;
				}
				for (IBeansImport iBi : importedBc.getImports()) {
					for (IBeansConfig iBc : iBi.getImportedBeansConfigs()) {
						if (hasImportedBeansConfig(file, iBc)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public IBeansConfig getConfig(IFile file) {
		IBeansConfig config = getConfig(getConfigName(file));
		if (config == null) {
			if (!this.modelPopulated) {
				populateModel();
			}
			try {
				r.lock();
				for (IBeansConfig beansConfig : configs.values()) {
					if (beansConfig.getElementResource() != null && beansConfig.getElementResource().equals(file)) {
						return beansConfig;
					}
				}
			} finally {
				r.unlock();
			}

		}
		return config;
	}

	/**
	 * {@inheritDoc}
	 */
	public IBeansConfig getConfig(String configName) {
		if (configName != null && configName.length() > 0 && configName.charAt(0) == '/') {
			return BeansCorePlugin.getModel().getConfig(configName);
		}
		if (!this.modelPopulated) {
			populateModel();
		}
		try {
			r.lock();
			if (configs.containsKey(configName)) {
				return configs.get(configName);
			} else if (autoDetectedConfigs.containsKey(configName)) {
				return autoDetectedConfigs.get(configName);
			}
			return null;
		} finally {
			r.unlock();
		}
	}

	public Set<String> getConfigNames() {
		if (!this.modelPopulated) {
			populateModel();
		}
		try {
			r.lock();
			Set<String> configNames = new LinkedHashSet<String>(configs.keySet());
			configNames.addAll(autoDetectedConfigs.keySet());
			return configNames;
		} finally {
			r.unlock();
		}
	}

	public Set<String> getManualConfigNames() {
		if (!this.modelPopulated) {
			populateModel();
		}
		try {
			r.lock();
			return new LinkedHashSet<String>(configs.keySet());
		} finally {
			r.unlock();
		}
	}

	public Set<String> getAutoConfigNames() {
		if (!this.modelPopulated) {
			populateModel();
		}
		try {
			r.lock();
			return new LinkedHashSet<String>(autoDetectedConfigs.keySet());
		} finally {
			r.unlock();
		}
	}

	public Set<String> getManualConfigSetNames() {
		if (!this.modelPopulated) {
			populateModel();
		}
		try {
			r.lock();
			return new LinkedHashSet<String>(configSets.keySet());
		} finally {
			r.unlock();
		}
	}

	public Set<String> getAutoConfigSetNames() {
		if (!this.modelPopulated) {
			populateModel();
		}
		try {
			r.lock();
			return new LinkedHashSet<String>(autoDetectedConfigSets.keySet());
		} finally {
			r.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<IBeansConfig> getConfigs() {
		if (!this.modelPopulated) {
			populateModel();
		}
		try {
			r.lock();
			return allConfigs;
			// Set<IBeansConfig> beansConfigs = new
			// LinkedHashSet<IBeansConfig>(configs.values());
			// beansConfigs.addAll(autoDetectedConfigs.values());
			// return beansConfigs;
		} finally {
			r.unlock();
		}
	}

	/**
	 * Updates the {@link BeansConfigSet}s defined within this project.
	 * <p>
	 * The modified project description has to be saved to disk by calling
	 * {@link #saveDescription()}.
	 * 
	 * @param configSets
	 *            list of {@link BeansConfigSet} instances
	 */
	public void setConfigSets(Set<IBeansConfigSet> configSets) {
		if (!this.modelPopulated) {
			populateModel();
		}
		try {
			w.lock();
			this.configSets.clear();
			for (IBeansConfigSet configSet : configSets) {
				this.configSets.put(configSet.getElementName(), configSet);
			}
		} finally {
			w.unlock();
		}
	}

	public boolean addConfigSet(IBeansConfigSet configSet) {
		if (!this.modelPopulated) {
			populateModel();
		}
		try {
			r.lock();
			if (!configSets.values().contains(configSet)) {
				configSets.put(configSet.getElementName(), configSet);

				if (autoDetectedConfigSets.containsKey(configSet.getElementName())) {
					autoDetectedConfigSets.remove(configSet.getElementName());
					autoDetectedConfigSetsByLocator.remove(configSet.getElementName());
				}

				return true;
			}
		} finally {
			r.unlock();
		}
		return false;
	}

	public void removeConfigSet(String configSetName) {
		try {
			w.lock();
			configSets.remove(configSetName);
		} finally {
			w.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasConfigSet(String configSetName) {
		if (!this.modelPopulated) {
			populateModel();
		}
		try {
			r.lock();
			return configSets.containsKey(configSetName);
		} finally {
			r.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public IBeansConfigSet getConfigSet(String configSetName) {
		if (!this.modelPopulated) {
			populateModel();
		}
		try {
			r.lock();
			IBeansConfigSet set = configSets.get(configSetName);
			if (set != null) {
				return set;
			}
			return autoDetectedConfigSets.get(configSetName);
		} finally {
			r.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<IBeansConfigSet> getConfigSets() {
		if (!this.modelPopulated) {
			populateModel();
		}
		try {
			r.lock();
			Set<IBeansConfigSet> configSets = new LinkedHashSet<IBeansConfigSet>(this.configSets.values());
			configSets.addAll(autoDetectedConfigSets.values());
			return configSets;
		} finally {
			r.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isBeanClass(String className) {
		for (IBeansConfig config : getConfigs()) {
			if (config.isBeanClass(className)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<String> getBeanClasses() {
		Set<String> beanClasses = new LinkedHashSet<String>();
		for (IBeansConfig config : getConfigs()) {
			beanClasses.addAll(config.getBeanClasses());
		}
		return beanClasses;
	}

	/**
	 * {@inheritDoc}
	 */
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
	 * defined in {@link IBeansProject#DESCRIPTION_FILE}.
	 */
	public void saveDescription() {

		// We can't acquire the write lock here - otherwise this may be
		// interfering with a ResourceChangeListener referring to this beans
		// project
		BeansProjectDescriptionWriter.write(this);
	}

	/**
	 * Resets the internal data. Any further access to the data of this instance
	 * of {@link BeansProject} leads to reloading of this beans project's config
	 * description file.
	 */
	public void reset() {
		try {
			w.lock();
			this.modelPopulated = false;
			configSuffixes.clear();
			configs.clear();
			configSets.clear();
			autoDetectedConfigs.clear();
			autoDetectedConfigsByLocator.clear();
			locatorByAutoDetectedConfig.clear();
			autoDetectedConfigSets.clear();
			autoDetectedConfigSetsByLocator.clear();
		} finally {
			updateAllConfigsCache();
			w.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
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

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(project);
		return getElementType() * hashCode + super.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		try {
			r.lock();
			return "Project=" + getElementName() + ", ConfigExtensions=" + configSuffixes + ", Configs=" + configs.values() + ", ConfigsSets="
					+ configSets;
		} finally {
			r.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isImportsEnabled() {
		return isImportsEnabled;
	}

	public void setImportsEnabled(boolean importEnabled) {
		this.isImportsEnabled = importEnabled;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isUpdatable() {
		IFile file = project.getProject().getFile(new Path(IBeansProject.DESCRIPTION_FILE));
		return !file.isReadOnly();
	}

	public void removeAutoDetectedConfigs(String locatorId) {
		try {
			w.lock();
			Set<String> configs = autoDetectedConfigsByLocator.get(locatorId);
			if (configs != null) {
				autoDetectedConfigsByLocator.remove(locatorId);
			}
			if (configs != null) {
				for (String configName : configs) {
					// Before actually removing make sure to delete ALL markers
					MarkerUtils.deleteAllMarkers(getConfig(configName).getElementResource(), SpringCore.MARKER_ID);

					// Remove the config from the internal list
					autoDetectedConfigs.remove(configName);
					locatorByAutoDetectedConfig.remove(configName);
				}
			}

			String configSet = autoDetectedConfigSetsByLocator.get(locatorId);
			if (configSets != null) {
				autoDetectedConfigSets.remove(configSet);
				autoDetectedConfigSetsByLocator.remove(configSet);
			}
		} finally {
			updateAllConfigsCache();
			w.unlock();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isExternal() {
		return false;
	}

	private boolean removeConfigFromConfigSets(String configName) {
		if (!this.modelPopulated) {
			populateModel();
		}
		boolean hasRemoved = false;
		try {
			r.lock();
			for (IBeansConfigSet configSet : configSets.values()) {
				if (configSet.hasConfig(configName)) {
					((BeansConfigSet) configSet).removeConfig(configName);
					hasRemoved = true;
				}
			}
			for (IBeansConfigSet configSet : autoDetectedConfigSets.values()) {
				if (configSet.hasConfig(configName)) {
					((BeansConfigSet) configSet).removeConfig(configName);
					hasRemoved = true;
				}
			}
		} finally {
			r.unlock();
		}
		return hasRemoved;
	}

	/**
	 * Returns the config name from given file. If the file belongs to this
	 * project then the config name is the project-relative path of the given
	 * file otherwise it's the workspace-relative path with a leading '/'.
	 */
	private String getConfigName(IFile file) {
		return BeansConfigFactory.getConfigName(file, this.project);
	}

	/**
	 * Populate the project's model with the information read from project
	 * description (an XML file defined in
	 * {@link ISpringProject.DESCRIPTION_FILE}).
	 */
	private void populateModel() {
		try {
			w.lock();
			if (this.modelPopulated) {
				return;
			}
			this.eventListener = new DefaultBeansConfigEventListener();
			this.modelPopulated = true;

			BeansProjectDescriptionReader.read(this);

			// Remove all invalid configs from this project
			Set<IBeansConfig> configuredConfigs = new LinkedHashSet<IBeansConfig>(configs.values());
			for (IBeansConfig config : configuredConfigs) {
				if (config.getElementResource() == null || !config.getElementResource().exists()) {
					removeConfig(config.getElementName());
				}
			}

			// Remove all invalid config names from this project's config sets
			Map<IBeansConfigSet, Set<String>> removedConfigsFromSets = new HashMap<IBeansConfigSet, Set<String>>();
			IBeansModel model = BeansCorePlugin.getModel();
			for (IBeansConfigSet configSet : configSets.values()) {
				for (String configName : configSet.getConfigNames()) {
					if (!hasConfig(configName) && model.getConfig(configName) == null) {
						((BeansConfigSet) configSet).removeConfig(configName);

						Set<String> removedConfigs = removedConfigsFromSets.get(configSet);
						if (removedConfigs == null) {
							removedConfigs = new HashSet<String>();
							removedConfigsFromSets.put(configSet, removedConfigs);
						}
						removedConfigs.add(configName);
					}
				}
			}

			// Add auto detected configs and config sets
			populateAutoDetectedConfigsAndConfigSets(removedConfigsFromSets);

			for (IBeansConfig config : configs.values()) {
				config.registerEventListener(eventListener);
			}
		} finally {
			updateAllConfigsCache();
			w.unlock();
		}
	}

	/**
	 * Runs the registered detectors and registers {@link IBeansConfig} and
	 * {@link IBeansConfigSet} with this project.
	 * <p>
	 * This method should only be called with having a write lock.
	 * 
	 * @param removedConfigsFromSets
	 */
	protected void populateAutoDetectedConfigsAndConfigSets(final Map<IBeansConfigSet, Set<String>> removedConfigsFromSets) {

		Job job = new Job("populate auto detected configs") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					populateAutoDetectedConfigsAndConfigSetsInternally();

					w.lock();
					restoreConfigSetState(removedConfigsFromSets);
				} finally {
					updateAllConfigsCache();
					w.unlock();
				}
				((AbstractModel) (BeansCorePlugin.getModel())).notifyListeners(BeansProject.this, ModelChangeEvent.Type.CHANGED);
				return Status.OK_STATUS;
			}

			@Override
			public boolean belongsTo(Object family) {
				return family.equals("populateAutoConfigsJobFamily");
			}
		};

		job.setPriority(Job.BUILD);
		job.setRule(project.getProject());
		job.schedule();
	}

	protected void populateAutoDetectedConfigsAndConfigSetsInternally() {

		final Map<BeansConfigLocatorDefinition, Map<String, IBeansConfig>> newAutoConfigs = new HashMap<BeansConfigLocatorDefinition, Map<String, IBeansConfig>>();
		final Map<BeansConfigLocatorDefinition, String> newConfigSetNames = new HashMap<BeansConfigLocatorDefinition, String>();

		// Find  auto detected beans configs
		for (final BeansConfigLocatorDefinition locator : BeansConfigLocatorFactory.getBeansConfigLocatorDefinitions()) {
			if (locator.isEnabled(getProject()) && locator.getBeansConfigLocator().supports(getProject())) {
				final Map<String, IBeansConfig> detectedConfigs = new HashMap<String, IBeansConfig>();
				newAutoConfigs.put(locator, detectedConfigs);

				// Prevent extension contribution from crashing the model
				// creation
				SafeRunner.run(new ISafeRunnable() {

					public void handleException(Throwable exception) {
						// nothing to handle here
					}

					public void run() throws Exception {
						IBeansConfigLocator configLocator = locator.getBeansConfigLocator();
						Set<IFile> files = configLocator.locateBeansConfigs(getProject(), null);
						
						for (IFile file : files) {
							BeansConfig config = new BeansConfig(BeansProject.this, file.getProjectRelativePath().toString(), Type.AUTO_DETECTED);
							String configName = getConfigName(file);
							if (!hasConfig(configName)) {
								detectedConfigs.put(configName, config);
							}
						}

						if (files.size() > 1) {
							String configSet = locator.getBeansConfigLocator().getBeansConfigSetName(files);
							if (configSet.length() > 0) {
								newConfigSetNames.put(locator, configSet);
							}
						}

						if (configLocator instanceof IJavaConfigLocator) {
							Set<IType> types = ((IJavaConfigLocator) configLocator).locateJavaConfigs(getProject(), null);
							for (IType type : types) {
								IBeansConfig config = new BeansJavaConfig(BeansProject.this, type, type.getFullyQualifiedName(), Type.AUTO_DETECTED);
								String configName = BeansConfigFactory.JAVA_CONFIG_TYPE + type.getFullyQualifiedName();
								if (!hasConfig(configName)) {
									detectedConfigs.put(configName, config);
								}
							}
						}
					}
				});
			}
		}

		setAutoDetectedConfigs(newAutoConfigs, newConfigSetNames);
	}

	protected void setAutoDetectedConfigs(Map<BeansConfigLocatorDefinition, Map<String, IBeansConfig>> newAutoConfigs,
			Map<BeansConfigLocatorDefinition, String> newConfigSetNames) {

		try {

			w.lock();

			for (IBeansConfig config : autoDetectedConfigs.values()) {
				config.unregisterEventListener(eventListener);
			}

			autoDetectedConfigs.clear();
			autoDetectedConfigsByLocator.clear();
			locatorByAutoDetectedConfig.clear();
			autoDetectedConfigSets.clear();
			autoDetectedConfigSetsByLocator.clear();

			Iterator<BeansConfigLocatorDefinition> locators = newAutoConfigs.keySet().iterator();
			while (locators.hasNext()) {

				BeansConfigLocatorDefinition locator = locators.next();

				Map<String, IBeansConfig> detectedConfigs = newAutoConfigs.get(locator);
				String configSetName = newConfigSetNames.get(locator);

				if (detectedConfigs.size() > 0) {
					Set<String> configNamesByLocator = new LinkedHashSet<String>();

					for (Map.Entry<String, IBeansConfig> detectedConfig : detectedConfigs.entrySet()) {
						String configName = detectedConfig.getKey();

						autoDetectedConfigs.put(configName, detectedConfig.getValue());
						detectedConfig.getValue().registerEventListener(eventListener);

						configNamesByLocator.add(configName);
						locatorByAutoDetectedConfig.put(configName, locator.getNamespaceUri() + "." + locator.getId());
					}
					autoDetectedConfigsByLocator.put(locator.getNamespaceUri() + "." + locator.getId(), configNamesByLocator);

					// Create a config set for auto detected configs if desired
					// by the extension
					if (configSetName != null && configSetName.length() > 0) {

						IBeansConfigSet configSet = new BeansConfigSet(BeansProject.this, configSetName, configNamesByLocator,
								IBeansConfigSet.Type.AUTO_DETECTED);

						// configure the created IBeansConfig
						locator.getBeansConfigLocator().configureBeansConfigSet(configSet);

						autoDetectedConfigSets.put(configSetName, configSet);
						autoDetectedConfigSetsByLocator.put(locator.getNamespaceUri() + "." + locator.getId(), configSetName);
					}
				}

			}

		} finally {
			w.unlock();
			
			SpringCoreUtils.buildFullProject(project);
		}
	}

	protected void restoreConfigSetState(Map<IBeansConfigSet, Set<String>> removedConfigsFromSets) {
		if (removedConfigsFromSets != null) {
			IBeansModel model = BeansCorePlugin.getModel();
			for (IBeansConfigSet configSet : removedConfigsFromSets.keySet()) {
				Set<String> removedConfigs = removedConfigsFromSets.get(configSet);
				for (String removedConfig : removedConfigs) {
					if (hasConfig(removedConfig) || model.getConfig(removedConfig) != null) {
						((BeansConfigSet) configSet).addConfig(removedConfig);
					}
				}
			}
		}
	}

	/**
	 * Update the internal cache for all configs in case something changed to
	 * this.configs or this.autoDetectedConfigs. This has to be called in a
	 * write-guarded block.
	 */
	protected void updateAllConfigsCache() {
		CopyOnWriteArraySet<IBeansConfig> newAllConfigs = new CopyOnWriteArraySet<IBeansConfig>(configs.values());
		newAllConfigs.addAll(autoDetectedConfigs.values());
		this.allConfigs = Collections.unmodifiableSet(newAllConfigs);
	}

	/**
	 * Default implementation of {@link IBeansConfigEventListener} that handles
	 * events and propagates those to {@link IBeansConfigSet}s and other
	 * {@link IBeansConfig}.
	 * 
	 * @author Christian Dupuis
	 * @since 2.2.5
	 */
	class DefaultBeansConfigEventListener implements IBeansConfigEventListener {

		/**
		 * {@inheritDoc}
		 */
		public void onPostProcessorDetected(IBeansConfig config, IBeansConfigPostProcessor postProcessor) {
			for (IBeansProject project : BeansCorePlugin.getModel().getProjects()) {
				for (IBeansConfigSet configSet : project.getConfigSets()) {
					if (configSet.hasConfig((IFile) config.getElementResource())) {
						for (IBeansConfig configSetConfig : configSet.getConfigs()) {
							if (!configSetConfig.equals(config) && configSetConfig instanceof BeansConfig) {
								((BeansConfig) configSetConfig).addExternalPostProcessor(postProcessor, config);
							}
						}
					}
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void onPostProcessorRemoved(IBeansConfig config, IBeansConfigPostProcessor postProcessor) {
			for (IBeansProject project : BeansCorePlugin.getModel().getProjects()) {
				for (IBeansConfigSet configSet : project.getConfigSets()) {
					if (configSet.hasConfig((IFile) config.getElementResource())) {
						for (IBeansConfig configSetConfig : configSet.getConfigs()) {
							if (!configSetConfig.equals(config) && configSetConfig instanceof BeansConfig) {
								((BeansConfig) configSetConfig).removeExternalPostProcessor(postProcessor, config);
							}
						}
					}
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void onReadEnd(IBeansConfig config) {
		}

		/**
		 * {@inheritDoc}
		 */
		public void onReadStart(IBeansConfig config) {
		}

		/**
		 * {@inheritDoc}
		 */
		public void onReset(IBeansConfig config) {
			for (IBeansProject project : BeansCorePlugin.getModel().getProjects()) {
				for (IBeansConfigSet configSet : project.getConfigSets()) {
					if (configSet.hasConfig((IFile) config.getElementResource())) {
						if (configSet instanceof BeansConfigSet) {
							((BeansConfigSet) configSet).reset();
						}
					}
				}
			}
		}
	}

	public boolean isInitialized() {
		if (!this.modelPopulated) {
			return false;
		}
		try {
			r.lock();
			for (IBeansConfig config : configs.values()) {
				if (!((ILazyInitializedModelElement) config).isInitialized()) {
					return false;
				}
			}
			for (IBeansConfig config : autoDetectedConfigs.values()) {
				if (!((ILazyInitializedModelElement) config).isInitialized()) {
					return false;
				}
			}
			return true;
		} finally {
			r.unlock();
		}
	}

	public boolean isAutoConfigStatePersisted() {
		return this.isAutoConfigStatePersisted;
	}

	public void setAutoConfigStatePersisted(boolean autoConfigPersisted) {
		this.isAutoConfigStatePersisted = autoConfigPersisted;
	}

}
