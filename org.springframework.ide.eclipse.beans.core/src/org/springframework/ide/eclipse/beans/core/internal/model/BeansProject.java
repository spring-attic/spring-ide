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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SafeRunner;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.project.BeansProjectDescriptionReader;
import org.springframework.ide.eclipse.beans.core.internal.project.BeansProjectDescriptionWriter;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansImport;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElementTypes;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig.Type;
import org.springframework.ide.eclipse.beans.core.model.locate.BeansConfigLocatorDefinition;
import org.springframework.ide.eclipse.beans.core.model.locate.BeansConfigLocatorFactory;
import org.springframework.ide.eclipse.core.MarkerUtils;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.model.AbstractResourceModelElement;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.util.ObjectUtils;

/**
 * This class holds information for a Spring Beans project. The information is lazily read from the
 * corresponding project description XML file defined in {@link IBeansProject#DESCRIPTION_FILE}.
 * <p>
 * The information can be persisted by calling the method {@link #saveDescription()}.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeansProject extends AbstractResourceModelElement implements IBeansProject {

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	private final Lock r = rwl.readLock();

	private final Lock w = rwl.writeLock();

	protected volatile boolean modelPopulated = false;

	private final IProject project;

	protected volatile Set<String> configSuffixes;

	/** the internal flag to specify if import processing is enabled */
	protected volatile boolean isImportsEnabled = DEFAULT_IMPORTS_ENABLED;

	/** Internal version number; intentionally set to lower value */
	protected volatile String version = "2.0.0";

	protected volatile Map<String, IBeansConfig> configs;

	protected volatile Map<String, IBeansConfig> autoDetectedConfigs;

	protected volatile Map<String, Set<String>> autoDetectedConfigsByLocator;

	protected volatile Map<String, String> locatorByAutoDetectedConfig;

	protected volatile Map<String, IBeansConfigSet> configSets;

	protected volatile Map<String, IBeansConfigSet> autoDetectedConfigSets;

	protected volatile Map<String, String> autoDetectedConfigSetsByLocator;

	public BeansProject(IBeansModel model, IProject project) {
		super(model, project.getName());
		this.project = project;
	}

	public int getElementType() {
		return IBeansModelElementTypes.PROJECT_TYPE;
	}

	@Override
	public IModelElement[] getElementChildren() {
		Set<IModelElement> children = new LinkedHashSet<IModelElement>(getConfigs());
		children.addAll(getConfigSets());
		return children.toArray(new IModelElement[children.size()]);
	}

	public IResource getElementResource() {
		return project;
	}

	public boolean isElementArchived() {
		return false;
	}

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

	public IProject getProject() {
		return project;
	}

	/**
	 * Updates the list of config suffixes belonging to this project.
	 * <p>
	 * The modified project description has to be saved to disk by calling
	 * {@link #saveDescription()}.
	 * @param suffixes list of config suffixes
	 */
	public void setConfigSuffixes(Set<String> suffixes) {
		if (!this.modelPopulated) {
			populateModel();
		}
		try {
			w.lock();
			configSuffixes.clear();
			configSuffixes.addAll(suffixes);
		}
		finally {
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
			}
			finally {
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
		}
		finally {
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
		}
		finally {
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
	 * Updates the list of configs (by name) belonging to this project. From all removed configs the
	 * Spring IDE problem markers are deleted.
	 * <p>
	 * The modified project description has to be saved to disk by calling
	 * {@link #saveDescription()}.
	 * @param configNames list of config names
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
				configs.put(configName, new BeansConfig(this, configName, Type.MANUAL));
			}
		}
		finally {
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
	 * @param file the config file to add
	 * @return <code>true</code> if config file was added to this project
	 */
	public boolean addConfig(IFile file, IBeansConfig.Type type) {
		return addConfig(getConfigName(file), type);
	}

	/**
	 * Adds the given beans config to the list of configs.
	 * <p>
	 * The modified project description has to be saved to disk by calling
	 * {@link #saveDescription()}.
	 * @param configName the config name to add
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
					configs.put(configName, new BeansConfig(this, configName, type));
					if (autoDetectedConfigs.containsKey(configName)) {
						autoDetectedConfigs.remove(configName);
						String locatorId = locatorByAutoDetectedConfig.remove(configName);
						if (locatorId != null
								&& autoDetectedConfigsByLocator.containsKey(locatorId)) {
							autoDetectedConfigsByLocator.get(locatorId).remove(configName);
						}
					}
					return true;
				}
				else if (type == IBeansConfig.Type.AUTO_DETECTED) {
					autoDetectedConfigs.put(configName, new BeansConfig(this, configName, type));
					return true;
				}
			}
		}
		finally {
			w.unlock();
		}
		return false;
	}

	/**
	 * Removes the given beans config from the list of configs and from all config sets.
	 * <p>
	 * The modified project description has to be saved to disk by calling
	 * {@link #saveDescription()}.
	 * @param file the config file to remove
	 * @return <code>true</code> if config was removed to this project
	 */
	public boolean removeConfig(IFile file) {
		if (file.getProject().equals(project)) {
			return removeConfig(file.getProjectRelativePath().toString());
		}

		// External configs only remove from all config sets
		return removeConfigFromConfigSets(file.getFullPath().toString());
	}

	/**
	 * Removes the given beans config from the list of configs and from all config sets.
	 * <p>
	 * The modified project description has to be saved to disk by calling
	 * {@link #saveDescription()}.
	 * @param configName the config name to remove
	 * @return <code>true</code> if config was removed to this project
	 */
	public boolean removeConfig(String configName) {
		if (hasConfig(configName)) {
			try {
				w.lock();
				configs.remove(configName);
				autoDetectedConfigs.remove(configName);
				String locatorId = locatorByAutoDetectedConfig.remove(configName);
				if (locatorId != null && autoDetectedConfigsByLocator.containsKey(locatorId)) {
					autoDetectedConfigsByLocator.get(locatorId).remove(configName);
				}
			}
			finally {
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
		}
		finally {
			r.unlock();
		}
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
		IBeansConfig beansConfig = getConfig(file);
		if (beansConfig != null) {
			beansConfigs.add(beansConfig);
		}
		if (includeImported && configs != null) {
			try {
				r.lock();
				for (IBeansConfig bc : configs.values()) {
					checkForImportedBeansConfig(file, bc, beansConfigs);
				}
			}
			finally {
				r.unlock();
			}
		}
		return beansConfigs;
	}

	private void checkForImportedBeansConfig(IFile file, IBeansConfig bc,
			Set<IBeansConfig> beansConfigs) {
		if (bc.getElementResource().equals(file)) {
			beansConfigs.add(bc);
		}
		for (IBeansImport bi : bc.getImports()) {
			for (IBeansConfig importedBc : bi.getImportedBeansConfigs()) {
				if (importedBc.getElementResource().equals(file)) {
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

	public IBeansConfig getConfig(IFile file) {
		return getConfig(getConfigName(file));
	}

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
			}
			else if (autoDetectedConfigs.containsKey(configName)) {
				return autoDetectedConfigs.get(configName);
			}
			return null;
		}
		finally {
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
		}
		finally {
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
		}
		finally {
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
		}
		finally {
			r.unlock();
		}
	}

	public Set<IBeansConfig> getConfigs() {
		if (!this.modelPopulated) {
			populateModel();
		}
		try {
			r.lock();
			Set<IBeansConfig> beansConfigs = new LinkedHashSet<IBeansConfig>(configs.values());
			beansConfigs.addAll(autoDetectedConfigs.values());
			return beansConfigs;
		}
		finally {
			r.unlock();
		}
	}

	/**
	 * Updates the {@link BeansConfigSet}s defined within this project.
	 * <p>
	 * The modified project description has to be saved to disk by calling
	 * {@link #saveDescription()}.
	 * @param configSets list of {@link BeansConfigSet} instances
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
		}
		finally {
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
		}
		finally {
			r.unlock();
		}
		return false;
	}

	public void removeConfigSet(String configSetName) {
		try {
			w.lock();
			configSets.remove(configSetName);
		}
		finally {
			w.unlock();
		}
	}

	public boolean hasConfigSet(String configSetName) {
		if (!this.modelPopulated) {
			populateModel();
		}
		try {
			r.lock();
			return configSets.containsKey(configSetName);
		}
		finally {
			r.unlock();
		}
	}

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
		}
		finally {
			r.unlock();
		}
	}

	public Set<IBeansConfigSet> getConfigSets() {
		if (!this.modelPopulated) {
			populateModel();
		}
		try {
			r.lock();
			Set<IBeansConfigSet> configSets = new LinkedHashSet<IBeansConfigSet>(this.configSets
					.values());
			configSets.addAll(autoDetectedConfigSets.values());
			return configSets;
		}
		finally {
			r.unlock();
		}
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
	 * Writes the current project description to the corresponding XML file defined in
	 * {@link IBeansProject#DESCRIPTION_FILE}.
	 */
	public void saveDescription() {

		// We can't acquire the write lock here - otherwise this may be
		// interfering with a ResourceChangeListener referring to this beans
		// project
		BeansProjectDescriptionWriter.write(this);
	}

	/**
	 * Resets the internal data. Any further access to the data of this instance of
	 * {@link BeansProject} leads to reloading of this beans project's config description file.
	 */
	public void reset() {
		try {
			w.lock();
			this.modelPopulated = false;
			configSuffixes = null;
			configs = null;
			configSets = null;
			autoDetectedConfigs = null;
			autoDetectedConfigsByLocator = null;
			locatorByAutoDetectedConfig = null;
			autoDetectedConfigSets = null;
			autoDetectedConfigSetsByLocator = null;
		}
		finally {
			w.unlock();
		}
	}

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

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(project);
		return getElementType() * hashCode + super.hashCode();
	}

	@Override
	public String toString() {
		try {
			r.lock();
			return "Project=" + getElementName() + ", ConfigExtensions=" + configSuffixes
					+ ", Configs=" + configs.values() + ", ConfigsSets=" + configSets;
		}
		finally {
			r.unlock();
		}
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
		}
		finally {
			r.unlock();
		}
		return hasRemoved;
	}

	/**
	 * Returns the config name from given file. If the file belongs to this project then the config
	 * name is the project-relative path of the given file otherwise it's the workspace-relative
	 * path with a leading '/'.
	 */
	private String getConfigName(IFile file) {
		String configName;
		if (file.getProject().equals(project.getProject())) {
			configName = file.getProjectRelativePath().toString();
		}
		else {
			configName = file.getFullPath().toString();
		}
		return configName;
	}

	/**
	 * Populate the project's model with the information read from project description (an XML file
	 * defined in {@link ISpringProject.DESCRIPTION_FILE}).
	 */
	private void populateModel() {
		try {
			w.lock();
			if (this.modelPopulated) {
				return;
			}
			// Initialize the model's data structures and read the project
			// description file
			configSuffixes = new LinkedHashSet<String>();
			configs = new LinkedHashMap<String, IBeansConfig>();
			configSets = new LinkedHashMap<String, IBeansConfigSet>();
			autoDetectedConfigs = new LinkedHashMap<String, IBeansConfig>();
			autoDetectedConfigsByLocator = new LinkedHashMap<String, Set<String>>();
			locatorByAutoDetectedConfig = new LinkedHashMap<String, String>();
			autoDetectedConfigSets = new LinkedHashMap<String, IBeansConfigSet>();
			autoDetectedConfigSetsByLocator = new LinkedHashMap<String, String>();

			this.modelPopulated = true;
			BeansProjectDescriptionReader.read(this);

			// Remove all invalid configs from this project
			for (IBeansConfig config : getConfigs()) {
				if (config.getElementResource() == null) {
					removeConfig(config.getElementName());
				}
			}

			// Add auto detected beans configs
			for (final BeansConfigLocatorDefinition locator : BeansConfigLocatorFactory
					.getBeansConfigLocatorDefinitions()) {
				if (locator.isEnabled(getProject())
						&& locator.getBeansConfigLocator().supports(getProject())) {
					final Map<String, IBeansConfig> detectedConfigs = new HashMap<String, IBeansConfig>();
					final String[] configSetName = new String[1];

					// Prevent extension contribution from crashing the model creation
					SafeRunner.run(new ISafeRunnable() {

						public void handleException(Throwable exception) {
							// nothing to handle here
						}

						public void run() throws Exception {
							Set<IFile> files = locator.getBeansConfigLocator().locateBeansConfigs(
									getProject(), null);
							for (IFile file : files) {
								BeansConfig config = new BeansConfig(BeansProject.this, file
										.getProjectRelativePath().toString(), Type.AUTO_DETECTED);
								String configName = getConfigName(file);
								if (!hasConfig(configName)) {
									detectedConfigs.put(configName, config);
								}
							}
							if (files.size() > 1) {
								String configSet = locator.getBeansConfigLocator()
										.getBeansConfigSetName(files);
								if (configSet.length() > 0) {
									configSetName[0] = configSet;
								}
							}
						}
					});

					if (detectedConfigs.size() > 0) {
						Set<String> configNamesByLocator = new LinkedHashSet<String>();

						for (Map.Entry<String, IBeansConfig> detectedConfig : detectedConfigs
								.entrySet()) {
							autoDetectedConfigs.put(detectedConfig.getKey(), detectedConfig
									.getValue());
							configNamesByLocator.add(getConfigName((IFile) detectedConfig
									.getValue().getElementResource()));
							locatorByAutoDetectedConfig.put(getConfigName((IFile) detectedConfig
									.getValue().getElementResource()), locator.getNamespaceUri()
									+ "." + locator.getId());
						}
						autoDetectedConfigsByLocator.put(locator.getNamespaceUri() + "."
								+ locator.getId(), configNamesByLocator);
						
						// Create a config set for auto detected configs if desired by the extension
						if (configSetName[0] != null && configSetName[0].length() > 0) {
							autoDetectedConfigSets.put(configSetName.toString(),
									new BeansConfigSet(this, configSetName.toString(),
											configNamesByLocator,
											IBeansConfigSet.Type.AUTO_DETECTED));
							autoDetectedConfigSetsByLocator.put(locator.getNamespaceUri() + "."
									+ locator.getId(), configSetName.toString());
						}
					}
				}
			}

			// Remove all invalid config names from from this project's config
			// sets
			IBeansModel model = BeansCorePlugin.getModel();
			for (IBeansConfigSet configSet : configSets.values()) {
				for (String configName : configSet.getConfigNames()) {
					if (!hasConfig(configName) && model.getConfig(configName) == null) {
						((BeansConfigSet) configSet).removeConfig(configName);
					}
				}
			}

		}
		finally {
			w.unlock();
		}
	}

	public boolean isImportsEnabled() {
		if (!this.modelPopulated) {
			populateModel();
		}
		try {
			r.lock();
			return isImportsEnabled;
		}
		finally {
			r.unlock();
		}
	}

	public void setImportsEnabled(boolean importEnabled) {
		this.isImportsEnabled = importEnabled;
	}

	public String getVersion() {
		if (!this.modelPopulated) {
			populateModel();
		}
		try {
			r.lock();
			return version;
		}
		finally {
			r.unlock();
		}
	}

	public void setVersion(String version) {
		this.version = version;
	}

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
					MarkerUtils.deleteAllMarkers(getConfig(configName).getElementResource(),
							SpringCore.MARKER_ID);

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
		}
		finally {
			w.unlock();
		}
	}

}
