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
package org.springframework.ide.eclipse.core.internal.model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.internal.model.resources.ISpringResourceChangeEvents;
import org.springframework.ide.eclipse.core.internal.model.resources.SpringResourceChangeListener;
import org.springframework.ide.eclipse.core.model.AbstractModel;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISpringModel;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent.Type;
import org.springframework.util.ObjectUtils;

/**
 * This model manages instances of {@link IProject}s. It's populated from
 * Eclipse's current workspace and receives {@link IResourceChangeEvent}s for
 * workspaces changes.
 * <p>
 * The single instance of {@link ISpringModel} is available from the static
 * method {@link SpringCore#getModel()}.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class SpringModel extends AbstractModel implements ISpringModel {

	/**
	 * The table of Spring projects (synchronized for concurrent access)
	 */
	protected Map<IProject, ISpringProject> projects;

	private IResourceChangeListener workspaceListener;

	public SpringModel() {
		super(null, ISpringModel.ELEMENT_NAME);
		projects = Collections
				.synchronizedMap(new HashMap<IProject, ISpringProject>());
	}

	@Override
	public IModelElement[] getElementChildren() {
		Collection<ISpringProject> elements = projects.values();
		return elements.toArray(new IModelElement[elements.size()]);
	}

	public void startup() {

		// Load all projects
		synchronized (projects) {
			projects.clear();
			for (IProject project : SpringCoreUtils.getSpringProjects()) {
				ISpringProject proj = new SpringProject(this, project);
				projects.put(project, proj);
			}
		}

		// Add a ResourceChangeListener to the Eclipse Workspace
		workspaceListener = new SpringResourceChangeListener(
				new ResourceChangeEventHandler());
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(workspaceListener,
				SpringResourceChangeListener.LISTENER_FLAGS);
	}

	public void shutdown() {

		// Remove the ResourceChangeListener from the Eclipse Workspace
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(workspaceListener);
		workspaceListener = null;

		// Remove all projects
		projects.clear();
	}

	public boolean hasProject(IProject project) {
		return projects.containsKey(project);
	}

	public ISpringProject getProject(IProject project) {
		return projects.get(project);
	}

	public Set<ISpringProject> getProjects() {
		return Collections.unmodifiableSet(new HashSet<ISpringProject>(projects
				.values()));
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
	private class ResourceChangeEventHandler implements
			ISpringResourceChangeEvents {

		public boolean isSpringProject(IProject project, int eventType) {
			return projects.get(project) != null;
		}

		public void springNatureAdded(IProject project, int eventType) {
			if (eventType == IResourceChangeEvent.POST_BUILD) {
				ISpringProject proj = new SpringProject(SpringModel.this,
						project);
				projects.put(project, proj);

				// Need ADD here because for the SpringExplorer the accoring
				// ISpringProject node has to be appear in the CommonNavigator
				notifyListeners(proj, Type.ADDED);
			}
		}

		public void springNatureRemoved(IProject project, int eventType) {
			if (eventType == IResourceChangeEvent.POST_BUILD) {
				ISpringProject proj = projects.remove(project);

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
				ISpringProject proj = new SpringProject(SpringModel.this,
						project);
				projects.put(project, proj);
				notifyListeners(proj, Type.ADDED);
			}
		}

		public void projectOpened(IProject project, int eventType) {
			if (eventType == IResourceChangeEvent.POST_BUILD) {
				ISpringProject proj = new SpringProject(SpringModel.this,
						project);
				projects.put(project, proj);
				notifyListeners(proj, Type.ADDED);
			}
		}

		public void projectClosed(IProject project, int eventType) {
			ISpringProject proj = projects.remove(project);
			if (proj != null) {
				notifyListeners(proj, Type.REMOVED);
			}
		}

		public void projectDeleted(IProject project, int eventType) {
			ISpringProject proj = projects.remove(project);
			if (proj != null) {
				notifyListeners(proj, Type.REMOVED);
			}
		}
	}
}
