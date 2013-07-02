/*******************************************************************************
 * Copyright (c) 2007, 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.internal.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.internal.model.resources.ISpringResourceChangeEvents;
import org.springframework.ide.eclipse.core.internal.model.resources.SpringResourceChangeListener;
import org.springframework.ide.eclipse.core.internal.project.SpringProjectContributionManager;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.AbstractModel;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISpringModel;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent.Type;
import org.springframework.util.ObjectUtils;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;

/**
 * This model manages instances of {@link IProject}s. It's populated from Eclipse's current workspace and receives
 * {@link IResourceChangeEvent}s for workspaces changes.
 * <p>
 * The single instance of {@link ISpringModel} is available from the static method {@link SpringCore#getModel()}.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.0
 */
public class SpringModel extends AbstractModel implements ISpringModel {

	private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	private final Lock r = rwl.readLock();

	private final Lock w = rwl.writeLock();

	/**
	 * The table of Spring projects (synchronized for concurrent access)
	 */
	protected Map<IProject, ISpringProject> projects;

	private IResourceChangeListener workspaceListener;

	private IElementChangedListener classpathListener;

	public SpringModel() {
		super(null, ISpringModel.ELEMENT_NAME);
		projects = new ConcurrentHashMap<IProject, ISpringProject>();
	}

	@Override
	public IModelElement[] getElementChildren() {
		try {
			r.lock();
			Collection<ISpringProject> elements = projects.values();
			return elements.toArray(new IModelElement[elements.size()]);
		}
		finally {
			r.unlock();
		}
	}

	public void startup() {
		try {
			w.lock();
			// Load all projects
			projects.clear();
			for (IProject project : SpringCoreUtils.getSpringProjects()) {
				ISpringProject proj = new SpringProject(this, project);
				projects.put(project, proj);
			}
		}
		finally {
			w.unlock();
		}

		// Add a ResourceChangeListener to the Eclipse Workspace
		workspaceListener = new SpringResourceChangeListener(new ResourceChangeEventHandler());
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(workspaceListener, SpringResourceChangeListener.LISTENER_FLAGS);

		classpathListener = new ClasspathChangedListener();
		JavaCore.addElementChangedListener(classpathListener);
	}

	public void shutdown() {

		// Remove the ResourceChangeListener from the Eclipse Workspace
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(workspaceListener);
		workspaceListener = null;

		JavaCore.removeElementChangedListener(classpathListener);
		classpathListener = null;

		try {
			w.lock();
			// Remove all projects
			projects.clear();
		}
		finally {
			w.unlock();
		}
	}

	public boolean hasProject(IProject project) {
		try {
			r.lock();
			return projects.containsKey(project);
		}
		finally {
			r.unlock();
		}
	}

	public ISpringProject getProject(IProject project) {
		try {
			r.lock();
			return projects.get(project);
		}
		finally {
			r.unlock();
		}

	}

	public Set<ISpringProject> getProjects() {
		try {
			r.lock();
			return Collections.unmodifiableSet(new HashSet<ISpringProject>(projects.values()));
		}
		finally {
			r.unlock();
		}

	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SpringModel)) {
			return false;
		}
		SpringModel that = (SpringModel) other;
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
		StringBuffer text = new StringBuffer("Spring model:\n");
		synchronized (projects) {
			int count = projects.size();
			for (ISpringProject project : projects.values()) {
				text.append(project.getElementName());
				if (--count > 0) {
					text.append(", ");
				}
			}
		}
		return text.toString();
	}

	/**
	 * Internal resource change event handler.
	 */
	private class ResourceChangeEventHandler implements ISpringResourceChangeEvents {

		public boolean isSpringProject(IProject project, int eventType) {
			try {
				r.lock();
				return projects.get(project) != null;
			}
			finally {
				r.unlock();
			}
		}

		public void springNatureAdded(IProject project, int eventType) {
			if (eventType == IResourceChangeEvent.POST_BUILD) {
				ISpringProject proj = new SpringProject(SpringModel.this, project);
				try {
					w.lock();
					projects.put(project, proj);
				}
				finally {
					w.unlock();
				}

				// Need ADD here because for the SpringExplorer the according
				// ISpringProject node has to be appear in the CommonNavigator
				notifyListeners(proj, Type.ADDED);
			}
		}

		public void springNatureRemoved(IProject project, int eventType) {
			if (eventType == IResourceChangeEvent.POST_BUILD) {
				ISpringProject proj = null;
				try {
					w.lock();
					proj = projects.remove(project);
				}
				finally {
					w.unlock();
				}

				// Need REMOVE here because for the SpringExplorer the according
				// ISpringProject node has to be disappear in the
				// CommonNavigator
				if (proj != null) {
					notifyListeners(proj, Type.REMOVED);
				}
			}
		}

		public void projectAdded(IProject project, int eventType) {
			if (eventType == IResourceChangeEvent.POST_BUILD) {
				ISpringProject proj = new SpringProject(SpringModel.this, project);
				try {
					w.lock();
					projects.put(project, proj);
				}
				finally {
					w.unlock();
				}
				notifyListeners(proj, Type.ADDED);
			}
		}

		public void projectOpened(IProject project, int eventType) {
			if (eventType == IResourceChangeEvent.POST_BUILD) {
				ISpringProject proj = new SpringProject(SpringModel.this, project);
				try {
					w.lock();
					projects.put(project, proj);
				}
				finally {
					w.unlock();
				}
				notifyListeners(proj, Type.ADDED);
			}
		}

		public void projectClosed(IProject project, int eventType) {
			ISpringProject proj = null;
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
			ISpringProject proj = null;
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
	}

	/**
	 * {@link IElementChangedListener} that listens for changes to the resolved classpath and triggers a project
	 * re-build
	 */
	private class ClasspathChangedListener implements IElementChangedListener {

		public void elementChanged(ElementChangedEvent event) {
			for (IJavaElementDelta delta : event.getDelta().getAffectedChildren()) {
				if ((delta.getFlags() & IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED) != 0
						|| (delta.getFlags() & IJavaElementDelta.F_CLASSPATH_CHANGED) != 0) {
					boolean addedOrRemoved = false;
					
					for (IJavaElementDelta classpathDelta : delta.getAffectedChildren()) {
						if ((classpathDelta.getFlags() & IJavaElementDelta.F_ADDED_TO_CLASSPATH) != 0
								|| (classpathDelta.getFlags() & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) != 0
								|| (classpathDelta.getKind() & IJavaElementDelta.ADDED) != 0
								|| (classpathDelta.getKind() & IJavaElementDelta.REMOVED) != 0) {
							addedOrRemoved = true;
							break;
						}
					}
					if (addedOrRemoved) {
						SpringProjectContributionManager.classpathChanged(delta.getElement().getJavaProject().getProject().getName());
						
						for (ISpringProject project : SpringCore.getModel().getProjects()) {
							IJavaProject javaProject = JdtUtils.getJavaProject(project.getProject());
							if (javaProject != null
									&& (javaProject.equals(delta.getElement().getJavaProject()) || javaProject
											.isOnClasspath(delta.getElement()))) {
								SpringProjectContributionManager.classpathChanged(project.getProject().getName());
//								SpringCoreUtils.buildProject(project.getProject());
								
								// workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=375365
								SpringCoreUtils.buildProject(project.getProject(), "org.eclipse.wst.validation.validationbuilder");
							}
						}
					}
				}
			}
		}
	}

}
