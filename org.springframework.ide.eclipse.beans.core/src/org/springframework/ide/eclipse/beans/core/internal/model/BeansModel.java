 /*******************************************************************************
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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.resources.BeansResourceChangeListener;
import org.springframework.ide.eclipse.beans.core.internal.model.resources.IBeansResourceChangeEvents;
import org.springframework.ide.eclipse.beans.core.internal.model.update.BeansModelUpdater;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.IImportedBeansConfig;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.model.AbstractModel;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent.Type;
import org.springframework.util.ObjectUtils;

/**
 * This model manages instances of {@link IBeansProject}s. It's populated from Eclipse's current
 * workspace and receives {@link IResourceChangeEvent}s for workspaces changes.
 * <p>
 * The single instance of {@link IBeansModel} is available from the static method
 * {@link BeansCorePlugin#getModel()}.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeansModel extends AbstractModel implements IBeansModel {

	public static final String DEBUG_OPTION = BeansCorePlugin.PLUGIN_ID + "/model/debug";

	public static final boolean DEBUG = BeansCorePlugin.isDebug(DEBUG_OPTION);

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	private final Lock r = rwl.readLock();

	private final Lock w = rwl.writeLock();

	/**
	 * The table of Spring Beans projects
	 */
	private volatile Map<IProject, IBeansProject> projects;

	private IResourceChangeListener workspaceListener;

	public BeansModel() {
		super(null, IBeansModel.ELEMENT_NAME);
		projects = new ConcurrentHashMap<IProject, IBeansProject>();
	}

	@Override
	public IModelElement[] getElementChildren() {
		return getProjects().toArray(new IModelElement[getProjects().size()]);
	}

	@Override
	public void accept(IModelElementVisitor visitor, IProgressMonitor monitor) {
		// Ask this model's projects
		try {
			r.lock();
			for (IBeansProject project : projects.values()) {
				project.accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}
		}
		finally {
			r.unlock();
		}
	}

	public void startup() {
		if (DEBUG) {
			System.out.println("Beans Model startup");
		}

		// Load all projects
		try {
			w.lock();
			projects.clear();
			for (IProject project : SpringCoreUtils.getSpringProjects()) {
				addProject(new BeansProject(this, project));
			}

			// Check for update actions
			BeansModelUpdater.updateModel(projects.values());
		}
		finally {
			w.unlock();
		}

		// Add a ResourceChangeListener to the Eclipse Workspace
		workspaceListener = new BeansResourceChangeListener(new ResourceChangeEventHandler());
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(workspaceListener,
				BeansResourceChangeListener.LISTENER_FLAGS);
	}

	protected void addProject(IBeansProject project) {
		projects.put(project.getProject(), project);
	}

	public void shutdown() {
		if (DEBUG) {
			System.out.println("Beans Model shutdown");
		}

		// Remove the ResourceChangeListener from the Eclipse Workspace
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(workspaceListener);
		workspaceListener = null;

		try {
			w.lock();
			// Remove all projects
			projects.clear();
		}
		finally {
			w.unlock();
		}
	}

	public IBeansProject getProject(IProject project) {
		try {
			r.lock();
			return projects.get(project);
		}
		finally {
			r.unlock();
		}
	}

	public IBeansProject getProject(String name) {

		// If a config name given then extract project name
		// External config files (with a leading '/') are handled too
		int configNamePos = name.indexOf('/', (name.charAt(0) == '/' ? 1 : 0));
		if (configNamePos > 0) {
			name = name.substring(0, configNamePos);
		}
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(name);
		return getProject(project);
	}

	/**
	 * Returns a collection of all projects defined in this model.
	 */
	public Set<IBeansProject> getProjects() {
		try {
			r.lock();
			return Collections.unmodifiableSet(new HashSet<IBeansProject>(projects.values()));
		}
		finally {
			r.unlock();
		}
	}

	public IBeansConfig getConfig(IFile configFile) {
		return getConfig(configFile, true);
	}

	public IBeansConfig getConfig(IFile configFile, boolean includeImported) {
		if (configFile != null) {
			IBeansProject project = getProject(configFile.getProject());
			if (project != null) {
				IBeansConfig bc = project.getConfig(configFile, includeImported);
				if (bc != null) {
					return bc;
				}
			}

			for (IBeansProject p : getProjects()) {
				IBeansConfig bc = p.getConfig(configFile, includeImported);
				if (bc != null) {
					return bc;
				}
			}
		}
		return null;
	}

	public Set<IBeansConfig> getConfigs(IFile configFile, boolean includeImported) {
		Set<IBeansConfig> beansConfigs = new LinkedHashSet<IBeansConfig>();
		if (configFile != null) {
			for (IBeansProject p : getProjects()) {
				beansConfigs.addAll(p.getConfigs(configFile, includeImported));
			}
		}
		return beansConfigs;
	}

	public IBeansConfig getConfig(String configName) {

		// Extract config name from given full-qualified name
		// External config files (with a leading '/') are handled too
		int configNamePos = configName.indexOf('/', (configName.charAt(0) == '/' ? 1 : 0));
		if (configNamePos > 0) {
			String projectName = configName.substring(1, configNamePos);
			configName = configName.substring(configNamePos + 1);
			IBeansProject project = BeansCorePlugin.getModel().getProject(projectName);
			if (project != null) {
				return project.getConfig(configName);
			}
		}
		return null;
	}

	/**
	 * Returns a list of all configs from this model which contain a bean with given bean class.
	 */
	public Set<IBeansConfig> getConfigs(String className) {
		Set<IBeansConfig> configs = new LinkedHashSet<IBeansConfig>();
		try {
			r.lock();
			for (IBeansProject project : projects.values()) {
				for (IBeansConfig config : project.getConfigs()) {
					if (config.isBeanClass(className)) {
						configs.add(config);
					}
				}
			}
		}
		finally {
			r.unlock();
		}
		return configs;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof BeansModel)) {
			return false;
		}
		try {
			r.lock();
			BeansModel that = (BeansModel) other;
			if (!ObjectUtils.nullSafeEquals(this.projects, that.projects))
				return false;
		}
		finally {
			r.unlock();
		}
		return super.equals(other);
	}

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(projects);
		return getElementType() * hashCode + super.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer text = new StringBuffer("Beans model:\n");
		try {
			r.lock();
			for (IBeansProject project : projects.values()) {
				text.append(" Configs of project '");
				text.append(project.getElementName());
				text.append("':\n");
				for (IBeansConfig config : project.getConfigs()) {
					text.append("  ");
					text.append(config);
					text.append('\n');
					for (IBean bean : config.getBeans()) {
						text.append("   ");
						text.append(bean);
						text.append('\n');
					}
				}
				text.append(" Config sets of project '");
				text.append(project.getElementName());
				text.append("':\n");
				for (IBeansConfigSet configSet : project.getConfigSets()) {
					text.append("  ");
					text.append(configSet);
					text.append('\n');
				}
			}
		}
		finally {
			r.unlock();
		}
		return text.toString();
	}

	/**
	 * Internal resource change event handler.
	 */
	private class ResourceChangeEventHandler implements IBeansResourceChangeEvents {

		public boolean isSpringProject(IProject project, int eventType) {
			return getProject(project) != null;
		}

		public void springNatureAdded(IProject project, int eventType) {
			if (eventType == IResourceChangeEvent.POST_BUILD) {
				if (DEBUG) {
					System.out.println("Spring beans nature added to project '"
							+ project.getName() + "'");
				}
				BeansProject proj = new BeansProject(BeansModel.this, project);
				try {
					w.lock();
					projects.put(project, proj);
				}
				finally {
					w.unlock();
				}
				notifyListeners(proj, Type.CHANGED);

				// Nature added -> run builder and validations on this event
				SpringCoreUtils.buildProject(project);
			}
		}

		public void springNatureRemoved(IProject project, int eventType) {
			if (eventType == IResourceChangeEvent.POST_BUILD) {
				if (DEBUG) {
					System.out.println("Spring beans nature removed from project '"
							+ project.getName() + "'");
				}
				IBeansProject proj = null;
				try {
					w.lock();
					proj = projects.remove(project);
				}
				finally {
					w.unlock();
				}
				if (proj != null) {
					notifyListeners(proj, Type.CHANGED);
				}
			}
		}

		public void projectAdded(IProject project, int eventType) {
			if (eventType == IResourceChangeEvent.POST_BUILD) {
				if (DEBUG) {
					System.out.println("Project '" + project.getName() + "' added");
				}
				BeansProject proj = new BeansProject(BeansModel.this, project);
				try {
					w.lock();
					projects.put(project, proj);
					BeansModelUpdater.updateProject(proj);
				}
				finally {
					w.unlock();
				}
				notifyListeners(proj, Type.ADDED);
			}
		}

		public void projectOpened(IProject project, int eventType) {
			if (eventType == IResourceChangeEvent.POST_BUILD) {
				if (DEBUG) {
					System.out.println("Project '" + project.getName() + "' opened");
				}
				BeansProject proj = new BeansProject(BeansModel.this, project);
				try {
					w.lock();
					projects.put(project, proj);
					BeansModelUpdater.updateProject(proj);
				}
				finally {
					w.unlock();
				}
				notifyListeners(proj, Type.ADDED);
			}
		}

		public void projectClosed(IProject project, int eventType) {
			if (DEBUG) {
				System.out.println("Project '" + project.getName() + "' closed");
			}
			IBeansProject proj = null;
			try {
				w.lock();
				proj = projects.remove(project);
			}
			finally {
				w.unlock();
			}
			if (proj != null) {
				notifyListeners(proj, Type.REMOVED);
			}
		}

		public void projectDeleted(IProject project, int eventType) {
			if (DEBUG) {
				System.out.println("Project '" + project.getName() + "' deleted");
			}
			IBeansProject proj = null;
			try {
				w.lock();
				proj = projects.remove(project);
			}
			finally {
				w.unlock();
			}
			if (proj != null) {
				notifyListeners(proj, Type.REMOVED);
			}
		}

		public void projectDescriptionChanged(IFile file, int eventType) {
			if (eventType == IResourceChangeEvent.POST_BUILD && !SpringCoreUtils.isManifest(file)) {
				if (DEBUG) {
					System.out.println("Project description '" + file.getFullPath() + "' changed");
				}
				buildProject(file, true);
			}
			// Special handling for META-INF/MANIFEST.MF files on PDE
			else if (eventType == IResourceChangeEvent.PRE_BUILD
					&& SpringCoreUtils.isManifest(file)) {
				if (DEBUG) {
					System.out.println("Project manifest '" + file.getFullPath() + "' changed");
				}
				buildProject(file, true);
			}
		}

		private void buildProject(IFile file, boolean build) {
			BeansProject project = null;
			try {
				r.lock();
				project = (BeansProject) projects.get(file.getProject());
			}
			finally {
				r.unlock();
			}
			// project can be null if the model has not been populated
			// correctly before updating the project description
			if (project != null) {
				project.reset();
				notifyListeners(project, Type.CHANGED);
				if (build) {
					// trigger build of project
					SpringCoreUtils.buildProject(project.getProject());
				}
			}
		}

		public void configAdded(IFile file, int eventType, IBeansConfig.Type type) {
			if (eventType == IResourceChangeEvent.POST_BUILD) {
				if (DEBUG) {
					System.out.println("Config '" + file.getFullPath() + "' added");
				}
				BeansProject project = null;
				try {
					r.lock();
					project = (BeansProject) projects.get(file.getProject());
				}
				finally {
					r.unlock();
				}
				if (project.addConfig(file, type)) {
					project.saveDescription();
					// In case this is a auto detected config make sure to refresh the
					// project too, as the project description file will not change
					if (type == IBeansConfig.Type.AUTO_DETECTED) {
						notifyListeners(project, Type.CHANGED);
					}
				}
				IBeansConfig config = project.getConfig(file);
				notifyListeners(config, Type.ADDED);
			}
		}

		public void configAdded(IFile file, int eventType) {
			configAdded(file, eventType, IBeansConfig.Type.MANUAL);
		}

		public void configChanged(IFile file, int eventType) {
			Set<BeansConfig> configs = new LinkedHashSet<BeansConfig>();
			try {
				r.lock();
				Set<IBeansConfig> bcs = getConfigs(file, true);
				for (IBeansConfig bc : bcs) {
					if (bc instanceof IImportedBeansConfig) {
						configs.add(BeansModelUtils.getParentOfClass(bc, BeansConfig.class));
					}
					else {
						configs.add((BeansConfig) bc);
					}
				}
			}
			finally {
				r.unlock();
			}
			if (eventType == IResourceChangeEvent.POST_BUILD) {
				if (DEBUG) {
					System.out.println("Config '" + file.getFullPath() + "' changed");
				}
				for (BeansConfig config : configs) {
					notifyListeners(config, Type.CHANGED);
				}
			}
			else {
				// Reset corresponding BeansConfig BEFORE the project builder
				// starts validating this BeansConfig
				for (BeansConfig config : configs) {
					config.reload();
				}
			}
		}

		public void configRemoved(IFile file, int eventType) {
			if (eventType == IResourceChangeEvent.POST_BUILD) {
				if (DEBUG) {
					System.out.println("Config '" + file.getFullPath() + "' removed");
				}
				BeansProject project = null;
				try {
					r.lock();
					project = (BeansProject) projects.get(file.getProject());
				}
				finally {
					r.unlock();
				}

				// Before removing the config from it's project keep a copy for
				// notifying the listeners
				BeansConfig config = (BeansConfig) project.getConfig(file);
				if (project.removeConfig(file)) {
					project.saveDescription();
				}

				// Remove config from config sets where referenced as external
				// config
				try {
					r.lock();
					for (IBeansProject proj : projects.values()) {
						if (((BeansProject) proj).removeConfig(file)) {
							((BeansProject) proj).saveDescription();
						}
					}
				}
				finally {
					r.unlock();
				}
				if (config != null) {
					notifyListeners(config, Type.REMOVED);
				}
			}
		}

		public void listenedFileChanged(IFile file, int eventType) {
			if (eventType == IResourceChangeEvent.POST_BUILD) {
				if (DEBUG) {
					System.out.println("Watched resource '" + file.getFullPath() + "' changed");
				}
				buildProject(file, false);
			}
		}

	}

}
