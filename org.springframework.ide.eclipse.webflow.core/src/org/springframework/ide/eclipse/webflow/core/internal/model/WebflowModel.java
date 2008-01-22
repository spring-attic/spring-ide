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
package org.springframework.ide.eclipse.webflow.core.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.internal.model.resources.SpringResourceChangeListener;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.TrueModelElementVisitor;
import org.springframework.ide.eclipse.webflow.core.internal.model.resources.WebflowResourceChangeListener;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModel;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelListener;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;

public class WebflowModel extends AbstractModelElement implements
		IWebflowModel, IResourceChangeListener {

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	private final Lock r = rwl.readLock();

	private final Lock w = rwl.writeLock();

	private List<IWebflowModelListener> listners = new ArrayList<IWebflowModelListener>();

	private Map<IProject, IWebflowProject> projects;

	private IResourceChangeListener workspaceListener;

	public WebflowModel() {
		this.projects = new ConcurrentHashMap<IProject, IWebflowProject>();
	}

	public boolean hasProject(IProject project) {
		try {
			r.lock();
			if (project != null && project.isAccessible()
					&& projects.containsKey(project)
					&& SpringCoreUtils.isSpringProject(project)) {
				return true;
			}
		}
		finally {
			r.unlock();
		}
		return false;
	}

	public IWebflowProject getProject(IProject project) {
		try {
			r.lock();
			if (hasProject(project)) {
				return (IWebflowProject) projects.get(project);
			}
		}
		finally {
			r.unlock();
		}
		return new WebflowProject(project, this);
	}

	public IWebflowProject getProject(String name) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				name);
		return getProject(project);
	}

	public Collection<IWebflowProject> getProjects() {
		try {
			r.lock();
			return projects.values();
		}
		finally {
			r.unlock();
		}
	}

	public IWebflowConfig getConfig(IFile configFile) {
		if (configFile != null) {
			IWebflowProject project = getProject(configFile.getProject());
			if (project != null) {
				return project.getConfig(configFile);
			}
		}
		return null;
	}

	public void startup() {
		initialize();
		initializeModel();
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(this);
		
		// Add a ResourceChangeListener to the Eclipse Workspace
		workspaceListener = new WebflowResourceChangeListener(
				new WebflowResourceChangeEvents());
		workspace.addResourceChangeListener(workspaceListener,
				SpringResourceChangeListener.LISTENER_FLAGS);
	}
	
	private void initializeModel() {
		Job job = new Job("Initializing Spring Web Flow Model") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				SubProgressMonitor subMonitor = new SubProgressMonitor(monitor,
						IProgressMonitor.UNKNOWN);
				TrueModelElementVisitor visitor = new TrueModelElementVisitor();
				accept(visitor, subMonitor);
				subMonitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.setPriority(Job.SHORT); // process asap
		job.schedule();
	}

	public void shutdown() {
		initialize();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(this);

		// Remove the ResourceChangeListener from the Eclipse Workspace
		workspace.removeResourceChangeListener(workspaceListener);
		workspaceListener = null;

		// Remove all projects
		projects.clear();
	}

	private void initialize() {
		try {
			w.lock();
			this.projects.clear();
			List projects = getBeansProjects();
			if (!projects.isEmpty()) {
				Iterator iter = projects.iterator();
				while (iter.hasNext()) {
					IProject project = (IProject) iter.next();
					this.projects.put(project,
							new WebflowProject(project, this));
				}
			}
		}
		finally {
			w.unlock();
		}
	}

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

	public void removeProject(IProject project) {
		initialize();
		fireModelChangedEvent(null);
	}

	public IModelElement[] getElementChildren() {
		Set<IModelElement> children = new HashSet<IModelElement>();
		try {
			r.lock();
			children.addAll(this.projects.values());
		}
		finally {
			r.unlock();
		}
		return children.toArray(new IModelElement[children.size()]);
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

	public void accept(IModelElementVisitor visitor, IProgressMonitor monitor) {
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {
			for (IWebflowProject project : getProjects()) {
				project.accept(visitor, monitor);
			}
		}
	}

	public String getElementName() {
		return "WebflowModel";
	}

	public IResource getElementResource() {
		return null;
	}

	public boolean isElementArchived() {
		return false;
	}
}
