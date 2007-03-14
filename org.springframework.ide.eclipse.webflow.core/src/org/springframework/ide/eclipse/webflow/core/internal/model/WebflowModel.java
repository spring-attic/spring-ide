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

package org.springframework.ide.eclipse.webflow.core.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModel;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelListener;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;

/**
 * 
 */
public class WebflowModel implements IWebflowModel, IResourceChangeListener {

	private List<IWebflowModelListener> listners = new ArrayList<IWebflowModelListener>();

	/**
	 * 
	 */
	private Map<IProject, IWebflowProject> projects;

	/**
	 * 
	 */
	public WebflowModel() {
		this.projects = new HashMap<IProject, IWebflowProject>();
	}

	/**
	 * @param project
	 * @return
	 */
	public boolean hasProject(IProject project) {
		if (project != null && project.isAccessible()
				&& projects.containsKey(project)) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.core.internal.model.IWebflowModel#getProject(org.eclipse.core.resources.IProject)
	 */
	public IWebflowProject getProject(IProject project) {
		if (hasProject(project)) {
			return (IWebflowProject) projects.get(project);
		}
		return null;
	}

	/**
	 * @param name
	 * @return
	 */
	public IWebflowProject getProject(String name) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				name);
		return getProject(project);
	}

	/**
	 * Returns a collection of all <code>IBeansProject</code> s defined in
	 * this model.
	 * @return
	 * @see org.springframework.ide.eclipse.web.flow.core.model.IWebFlowProject
	 */
	public Collection getProjects() {
		return projects.values();
	}

	/**
	 * @param configFile
	 * @return
	 */
	public IWebflowConfig getConfig(IFile configFile) {
		if (configFile != null) {
			IWebflowProject project = getProject(configFile.getProject());
			if (project != null) {
				return project.getConfig(configFile);
			}
		}
		return null;
	}

	/**
	 * 
	 */
	public void startup() {
		initialize();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(this);
	}

	/**
	 * 
	 */
	public void shutdown() {
		initialize();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(this);
	}

	/**
	 * 
	 */
	private void initialize() {
		this.projects.clear();
		List projects = getBeansProjects();
		if (!projects.isEmpty()) {
			Iterator iter = projects.iterator();
			while (iter.hasNext()) {
				IProject project = (IProject) iter.next();
				this.projects.put(project, new WebflowProject(project, this));
			}
		}
	}

	/**
	 * Returns a list of all <code>IProject</code> s with the Beans project
	 * nature.
	 * @return list of all <code>IProject</code> s with the Beans project
	 * nature
	 */
	private static List<IProject> getBeansProjects() {
		List<IProject> springProjects = new ArrayList<IProject>();
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
				.getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			if (SpringCoreUtils.isSpringProject(project)) {
				springProjects.add(project);
			}
		}
		return springProjects;
	}

	public void registerModelChangeListener(IWebflowModelListener listener) {
		this.listners.add(listener);
	}

	public void removeModelChangeListener(IWebflowModelListener listener) {
		this.listners.remove(listener);
	}

	public void fireModelChangedEvent(IWebflowProject project) {
		for (IWebflowModelListener listener : this.listners) {
			listener.modelChanged(project);
		}
	}

	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getSource() instanceof IWorkspace) {
			IResourceDelta delta = event.getDelta();
			if (delta != null) {
				try {
					delta.accept(new WebflowProjectVisitor());
				}
				catch (CoreException e) {
				}
			}
		}
	}

	private class WebflowProjectVisitor implements IResourceDeltaVisitor {
		
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {

			case IResourceDelta.REMOVED:
				if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					IWebflowProject wfProject = getProject(file.getProject());
					if (wfProject != null
							&& wfProject.getConfig((IFile) resource) != null) {
						IWebflowConfig config = wfProject
								.getConfig((IFile) resource);
						List<IWebflowConfig> configs = wfProject.getConfigs();
						configs.remove(config);
						wfProject.setConfigs(configs);
					}
					return false;
				}
				break;
			}
			return true;
		}
	}
}