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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.resources.BeansResourceChangeListener;
import org.springframework.ide.eclipse.beans.core.internal.model.resources.IBeansResourceChangeEvents;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.model.AbstractModel;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent;
import org.springframework.ide.eclipse.core.model.ModelUtils;

/**
 * The <code>BeansModel</code> manages instances of <code>IBeansProject</code>s.
 * <code>IModelChangeListener</code>s register with the <code>BeansModel</code>,
 * and receive <code>ModelChangeEvent</code>s for all changes.
 * <p>
 * The single instance of <code>IBeansModel</code> is available from
 * the static method <code>BeansCorePlugin.getModel()</code>.
 *
 * @see org.springframework.ide.eclipse.core.model.IModelChangeListener
 * @see org.springframework.ide.eclipse.core.model.ModelChangeEvent
 *
 * @author Torsten Juergeleit
 */
public class BeansModel extends AbstractModel implements IBeansModel {

	public static final String DEBUG_OPTION = BeansCorePlugin.PLUGIN_ID +
																 "/model/debug";
	public static boolean DEBUG = BeansCorePlugin.isDebug(DEBUG_OPTION);

	/** The table of Spring Beans projects (synchronized for concurrent
	 * access) */
	private Hashtable projects;
	private IResourceChangeListener workspaceListener;

	public BeansModel() {
		super(null, IBeansModel.ELEMENT_NAME);
		this.projects = new Hashtable();
		this.workspaceListener = new BeansResourceChangeListener(
											 new ResourceChangeEventHandler());
	}

	public IModelElement[] getElementChildren() {
		return (IModelElement[]) getProjects().toArray(
									  new IModelElement[getProjects().size()]);
	}

	public void accept(IModelElementVisitor visitor, IProgressMonitor monitor) {

		// Ask this model's projects
		synchronized (projects) {
			Iterator iter = projects.values().iterator();
			while (iter.hasNext()) {
				IModelElement element = (IModelElement) iter.next();
				element.accept(visitor, monitor);
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
		initialize();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(workspaceListener,
									BeansResourceChangeListener.LISTENER_FLAGS);
	}

	public void shutdown() {
		if (DEBUG) {
			System.out.println("Beans Model shutdown");
		}
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(workspaceListener);
		workspaceListener = null;
		projects.clear();
	}

	public boolean hasProject(IProject project) {
		if (project != null  && project.isAccessible() &&
												projects.containsKey(project)) {
			return true;
		}
		return false;
	}

	public IBeansProject getProject(IProject project) {
		return (IBeansProject) projects.get(project);
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
	 * Returns a collection of all <code>IBeansProject</code>s defined in this
	 * model.
	 * @see org.springframework.ide.eclipse.beans.core.model.IBeansProject
	 */
	public Collection getProjects() {
		return projects.values();
	}

	public boolean hasConfig(IFile configFile) {
		if (configFile != null) {
			IBeansProject project = getProject(configFile.getProject());
			if (project != null) {
				return project.hasConfig(configFile);
			}
		}
		return false;
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
	 * Returns a list of all <code>IBeanConfig</code>s from this model which
	 * contain a bean with given bean class.
	 * @see org.springframework.ide.eclipse.beans.core.model.IBeansConfig
	 */
	public Collection getConfigs(String className) {
		List configs = new ArrayList();
		Iterator iter = getProjects().iterator();
		while (iter.hasNext()) {
			IBeansProject project = (IBeansProject) iter.next();
			Iterator iter2 = project.getConfigs().iterator();
			while (iter2.hasNext()) {
				IBeansConfig config = (IBeansConfig) iter2.next();
				if (config.isBeanClass(className)) {
					configs.add(config);
				}
			}
		}
		return configs;
	}

	public String toString() {
		StringBuffer text = new StringBuffer("Beans model:\n");
		synchronized (projects) {
			Iterator projs = projects.values().iterator();
			while (projs.hasNext()) {
				IBeansProject project = (IBeansProject) projs.next();
				text.append(" Configs of project '");
				text.append(project.getElementName());
				text.append("':\n");
				Iterator configs = project.getConfigs().iterator();
				while (configs.hasNext()) {
					IBeansConfig config = (IBeansConfig) configs.next();
					text.append("  ");
					text.append(config);
					text.append('\n');
					Iterator beans = config.getBeans().iterator();
					while (beans.hasNext()) {
						IBean bean = (IBean) beans.next();
						text.append("   ");
						text.append(bean);
						text.append('\n');
					}
				}
				text.append(" Config sets of project '");
				text.append(project.getElementName());
				text.append("':\n");
				Iterator configSets = project.getConfigSets().iterator();
				while (configSets.hasNext()) {
					IBeansConfigSet configSet = (IBeansConfigSet)
															 configSets.next();
					text.append("  ");
					text.append(configSet);
					text.append('\n');
				}
			}
		}
		return text.toString();
	}

	private void initialize() {
		if (DEBUG) {
			System.out.println("Initializing model - loading all projects");
		}
		synchronized (projects) {
			this.projects.clear();
			List projects = getBeansProjects();
			if (!projects.isEmpty()) {
				Iterator iter = projects.iterator();
				while (iter.hasNext()) {
					IProject project = (IProject) iter.next();
					this.projects.put(project, new BeansProject(project));
				}
			}
		}
	}

	/**
	 * Returns a list of all <code>IProject</code>s with the Beans project nature.
	 * @return list of all <code>IProject</code>s with the Beans project nature
	 */
	private static List getBeansProjects() {
		List springProjects = new ArrayList();
		IProject[] projects =
						 ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			if (SpringCoreUtils.isSpringProject(project)) {
				springProjects.add(project);
			}
		}
		return springProjects;
	}

	/**
	 * Internal resource change event handler.
	 */
	private class ResourceChangeEventHandler implements IBeansResourceChangeEvents {

		public boolean isSpringProject(IProject project) {
			return getProject(project) != null;
		}

		public void springNatureAdded(IProject project) {
			if (DEBUG) {
				System.out.println("Spring beans nature added to project '" +
								   project.getName() + "'");
			}
			BeansProject proj = new BeansProject(project);
			projects.put(project, proj);
			notifyListeners(proj, ModelChangeEvent.ADDED);
		}

		public void springNatureRemoved(IProject project) {
			if (DEBUG) {
				System.out.println("Spring beans nature removed from " +
								   "project '" + project.getName() + "'");
			}
			IBeansProject proj = (IBeansProject) projects.remove(project);
			notifyListeners(proj, ModelChangeEvent.REMOVED);
		}

		public void projectAdded(IProject project) {
			if (DEBUG) {
				System.out.println("Project '" + project.getName() + "' added");
			}
			BeansProject proj = new BeansProject(project);
			projects.put(project, proj);
			notifyListeners(proj, ModelChangeEvent.ADDED);
		}
	
		public void projectOpened(IProject project) {
			if (DEBUG) {
				System.out.println("Project '" + project.getName() +
								   "' opened");
			}
			BeansProject proj = new BeansProject(project);
			projects.put(project, proj);
			notifyListeners(proj, ModelChangeEvent.ADDED);
		}
	
		public void projectClosed(IProject project) {
			if (DEBUG) {
				System.out.println("Project '" + project.getName() +
								   "' closed");
			}
			IBeansProject proj = (IBeansProject) projects.remove(project);
			notifyListeners(proj, ModelChangeEvent.REMOVED);
		}
	
		public void projectDeleted(IProject project) {
			if (DEBUG) {
				System.out.println("Project '" + project.getName() +
								   "' deleted");
			}
			IBeansProject proj = (IBeansProject) projects.remove(project);
			notifyListeners(proj, ModelChangeEvent.REMOVED);
		}
	
		public void projectDescriptionChanged(IFile file) {
			if (DEBUG) {
				System.out.println("Project description '" +
								   file.getFullPath() +
								   "' changed");
			}
			BeansProject project = (BeansProject)
												projects.get(file.getProject());
			project.reset();
			notifyListeners(project, ModelChangeEvent.CHANGED);
		}

		public void configAdded(IFile file) {
			if (DEBUG) {
				System.out.println("Config '" + file.getFullPath() +
								   "' added");
			}
			BeansProject project = (BeansProject)
												projects.get(file.getProject());
			project.addConfig(file, true);
			IBeansConfig config = project.getConfig(file);
			notifyListeners(config, ModelChangeEvent.ADDED);
		}
	
		public void configChanged(IFile file) {
			if (DEBUG) {
				System.out.println("Config '" + file.getFullPath() +
								   "' changed");
			}
			IBeansProject project = (IBeansProject)
												projects.get(file.getProject());
			BeansConfig config = (BeansConfig) project.getConfig(file);

			// There is no need to reset this config again if it's already done
			// by the BeansConfigValidator
			if (!config.isReset()) {
				config.reset();
			}
			notifyListeners(config, ModelChangeEvent.CHANGED);
		}
	
		public void configRemoved(IFile file) {
			if (DEBUG) {
				System.out.println("Config '" + file.getFullPath() +
								   "' removed");
			}
			BeansProject project = (BeansProject)
												projects.get(file.getProject());
			// Before removing the config from it's project keep a copy for
			// notifying the listeners
			BeansConfig config = (BeansConfig) project.getConfig(file);
			project.removeConfig(file, true);

			// Remove config from config sets where referenced as external
			// config
			Iterator iter = projects.values().iterator();
			while (iter.hasNext()) {
				project = (BeansProject) iter.next();
				project.removeConfig(file, true);
				
			}
			notifyListeners(config, ModelChangeEvent.REMOVED);
		}

		public void beanClassChanged(String className, Collection configs) {
			if (DEBUG) {
				System.out.println("Bean class '" + className + "' changed");
			}
			BeansConfigValidator validator = new BeansConfigValidator();
			Iterator iter = configs.iterator();
			while (iter.hasNext()) {
				IBeansConfig config = (IBeansConfig) iter.next();

				// Delete all problem markers created by Spring IDE
				ModelUtils.deleteProblemMarkers(config);
				validator.validate(config, new NullProgressMonitor());
			}
		}
	}
}
