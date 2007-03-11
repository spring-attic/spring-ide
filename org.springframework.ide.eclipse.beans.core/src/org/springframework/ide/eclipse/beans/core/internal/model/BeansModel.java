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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.resources.BeansResourceChangeListener;
import org.springframework.ide.eclipse.beans.core.internal.model.resources.IBeansResourceChangeEvents;
import org.springframework.ide.eclipse.beans.core.internal.project.BeansProjectValidator;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.model.AbstractModel;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent.Type;
import org.springframework.util.ObjectUtils;

/**
 * This model manages instances of {@link IBeansProject}s. It's populated from
 * Eclipse's current workspace and receives {@link IResourceChangeEvent}s for
 * workspaces changes.
 * <p>
 * The single instance of {@link IBeansModel} is available from the static
 * method {@link BeansCorePlugin#getModel()}.
 * 
 * @author Torsten Juergeleit
 */
public class BeansModel extends AbstractModel implements IBeansModel {

	public static final String DEBUG_OPTION = BeansCorePlugin.PLUGIN_ID
			+ "/model/debug";

	public static boolean DEBUG = BeansCorePlugin.isDebug(DEBUG_OPTION);

	/**
	 * The table of Spring Beans projects (synchronized for concurrent access)
	 */
	protected Map<IProject, IBeansProject> projects;

	private IResourceChangeListener workspaceListener;

	public BeansModel() {
		super(null, IBeansModel.ELEMENT_NAME);
		projects = Collections
				.synchronizedMap(new LinkedHashMap<IProject, IBeansProject>());
	}

	@Override
	public IModelElement[] getElementChildren() {
		return getProjects().toArray(
				new IModelElement[getProjects().size()]);
	}

	@Override
	public void accept(IModelElementVisitor visitor,
			IProgressMonitor monitor) {
		// Ask this model's projects
		synchronized (projects) {
			for (IBeansProject project : projects.values()) {
				project.accept(visitor, monitor);
				if (monitor.isCanceled()) {
					return;
				}
			}
		}
	}

	public void startup() {
		if (DEBUG) {
			System.out.println("Beans Model startup");
		}

		// Load all projects
		synchronized (projects) {
			projects.clear();
			for (IProject project : getSpringProjects()) {
				projects.put(project, new BeansProject(this, project));
			}
		}

		// Add a ResourceChangeListener to the Eclipse Workspace
		workspaceListener = new BeansResourceChangeListener(
				new ResourceChangeEventHandler());
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(workspaceListener,
				BeansResourceChangeListener.LISTENER_FLAGS);
	}

	public void shutdown() {
		if (DEBUG) {
			System.out.println("Beans Model shutdown");
		}

		// Remove the ResourceChangeListener from the Eclipse Workspace
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(workspaceListener);
		workspaceListener = null;

		// Remove all projects
		projects.clear();
	}

	public IBeansProject getProject(IProject project) {
		return projects.get(project);
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
		return Collections.unmodifiableSet(new HashSet<IBeansProject>(projects
				.values()));
	}

	public IBeansConfig getConfig(IFile configFile) {
		if (configFile != null) {
			IBeansProject project = getProject(configFile.getProject());
			if (project != null) {
				return project.getConfig(configFile);
			}
		}
		return null;
	}

	public IBeansConfig getConfig(String configName) {

		// Extract config name from given full-qualified name
		// External config files (with a leading '/') are handled too
		int configNamePos = configName.indexOf('/',
				(configName.charAt(0) == '/' ? 1 : 0));
		if (configNamePos > 0) {
			String projectName = configName.substring(1, configNamePos);
			configName = configName.substring(configNamePos + 1);
			IBeansProject project = BeansCorePlugin.getModel().getProject(
					projectName);
			if (project != null) {
				return project.getConfig(configName);
			}
		}
		return null;
	}

	/**
	 * Returns a list of all configs from this model which contain a bean with
	 * given bean class.
	 */
	public Set<IBeansConfig> getConfigs(String className) {
		Set<IBeansConfig> configs = new LinkedHashSet<IBeansConfig>();
		synchronized (projects) {
			for (IBeansProject project : projects.values()) {
				for (IBeansConfig config : project.getConfigs()) {
					if (config.isBeanClass(className)) {
						configs.add(config);
					}
				}
			}
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
		BeansModel that = (BeansModel) other;
		if (!ObjectUtils.nullSafeEquals(this.projects, that.projects))
			return false;
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
		synchronized (projects) {
			for (IBeansProject project : projects.values()) { 
				text.append(" Configs of project '");
				text.append(project.getElementName());
				text.append("':\n");
				for (IBeansConfig config : project.getConfigs()) {
					text.append("  ");
					text.append(config);
					text.append('\n');
					for (IBean bean :config.getBeans()) {
						text.append("   ");
						text.append(bean);
						text.append('\n');
					}
				}
				text.append(" Config sets of project '");
				text.append(project.getElementName());
				text.append("':\n");
				for (IBeansConfigSet configSet :project.getConfigSets()) {
					text.append("  ");
					text.append(configSet);
					text.append('\n');
				}
			}
		}
		return text.toString();
	}

	/**
	 * Returns a list of all projects with the Spring project nature.
	 */
	private static Set<IProject> getSpringProjects() {
		Set<IProject> springProjects = new LinkedHashSet<IProject>();
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot()
				.getProjects()) {
			if (SpringCoreUtils.isSpringProject(project)) {
				springProjects.add(project);
			}
		}
		return springProjects;
	}

	/**
	 * Internal resource change event handler.
	 */
	private class ResourceChangeEventHandler implements
			IBeansResourceChangeEvents {

		public boolean isSpringProject(IProject project, int eventType) {
			return getProject(project) != null;
		}

		public void springNatureAdded(IProject project, int eventType) {
			if (eventType == IResourceChangeEvent.POST_BUILD) {
				if (DEBUG) {
					System.out.println("Spring beans nature added to "
							+ "project '" + project.getName() + "'");
				}
				BeansProject proj = new BeansProject(BeansModel.this, project);
				projects.put(project, proj);
				notifyListeners(proj, Type.ADDED);
			}
		}

		public void springNatureRemoved(IProject project, int eventType) {
			if (eventType == IResourceChangeEvent.POST_BUILD) {
				if (DEBUG) {
					System.out.println("Spring beans nature removed from "
							+ "project '" + project.getName() + "'");
				}
				IBeansProject proj = projects.remove(project);
				notifyListeners(proj, Type.REMOVED);
			}
		}

		public void projectAdded(IProject project, int eventType) {
			if (eventType == IResourceChangeEvent.POST_BUILD) {
				if (DEBUG) {
					System.out.println("Project '" + project.getName()
							+ "' added");
				}
				BeansProject proj = new BeansProject(BeansModel.this, project);
				projects.put(project, proj);
				notifyListeners(proj, Type.ADDED);
			}
		}

		public void projectOpened(IProject project, int eventType) {
			if (eventType == IResourceChangeEvent.POST_BUILD) {
				if (DEBUG) {
					System.out.println("Project '" + project.getName()
							+ "' opened");
				}
				BeansProject proj = new BeansProject(BeansModel.this, project);
				projects.put(project, proj);
				notifyListeners(proj, Type.ADDED);
			}
		}

		public void projectClosed(IProject project, int eventType) {
			if (DEBUG) {
				System.out.println("Project '" + project.getName()
						+ "' closed");
			}
			IBeansProject proj = projects.remove(project);
			notifyListeners(proj, Type.REMOVED);
		}

		public void projectDeleted(IProject project, int eventType) {
			if (DEBUG) {
				System.out.println("Project '" + project.getName()
						+ "' deleted");
			}
			IBeansProject proj = projects.remove(project);
			notifyListeners(proj, Type.REMOVED);
		}

		public void projectDescriptionChanged(IFile file, int eventType) {
			if (eventType == IResourceChangeEvent.POST_BUILD) {
				if (DEBUG) {
					System.out.println("Project description '"
							+ file.getFullPath() + "' changed");
				}
				BeansProject project = (BeansProject) projects.get(file
						.getProject());
				project.reset();
				notifyListeners(project, Type.CHANGED);
			}
		}

		public void configAdded(IFile file, int eventType) {
			if (eventType == IResourceChangeEvent.POST_BUILD) {
				if (DEBUG) {
					System.out.println("Config '" + file.getFullPath()
							+ "' added");
				}
				BeansProject project = (BeansProject) projects.get(file
						.getProject());
				if (project.addConfig(file)) {
					project.saveDescription();
				}
				IBeansConfig config = project.getConfig(file);
				notifyListeners(config, Type.ADDED);
			}
		}

		public void configChanged(IFile file, int eventType) {
			IBeansProject project = projects.get(file
					.getProject());
			BeansConfig config = (BeansConfig) project.getConfig(file);
			if (eventType == IResourceChangeEvent.POST_BUILD) {
				if (DEBUG) {
					System.out.println("Config '" + file.getFullPath()
							+ "' changed");
				}
				notifyListeners(config, Type.CHANGED);
			} else {
				// Reset corresponding BeansConfig BEFORE the project builder
				// starts validating this BeansConfig
				config.reload();
			}
		}

		public void configRemoved(IFile file, int eventType) {
			if (eventType == IResourceChangeEvent.POST_BUILD) {
				if (DEBUG) {
					System.out.println("Config '" + file.getFullPath()
							+ "' removed");
				}
				IBeansProject project = projects.get(file.getProject());

				// Before removing the config from it's project keep a copy for
				// notifying the listeners
				BeansConfig config = (BeansConfig) project.getConfig(file);
				if (((BeansProject) project).removeConfig(file)) {
					((BeansProject) project).saveDescription();
				}

				// Remove config from config sets where referenced as external
				// config
				synchronized (projects) {
					for (IBeansProject proj : projects.values()) {
						if (((BeansProject) proj).removeConfig(file)) {
							((BeansProject) proj).saveDescription();
						}
					}
				}
				notifyListeners(config, Type.REMOVED);
			}
		}

		public void beanClassChanged(String className,
				Set<IBeansConfig> configs, int eventType) {
			if (eventType == IResourceChangeEvent.POST_BUILD) {
				if (DEBUG) {
					System.out.println("Bean class '" + className
							+ "' changed");
				}
				IProgressMonitor monitor = new NullProgressMonitor();
				for (IBeansConfig config : configs) {
					IFile file = BeansModelUtils.getFile(config);
					if (file != null) {
						BeansProjectValidator.validate(file, monitor);
					}
				}
			}
		}
	}
}
