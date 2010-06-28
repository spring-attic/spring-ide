/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.core.internal.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.aop.core.model.IAopModelChangedListener;
import org.springframework.ide.eclipse.aop.core.model.IAopProject;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.IAopReferenceModel;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.internal.model.resources.SpringResourceChangeListener;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class AopReferenceModel implements IAopReferenceModel {

	private List<IAopModelChangedListener> listeners = new LinkedList<IAopModelChangedListener>();

	private AopReferenceModelPeristence persistence;

	private Map<IJavaProject, IAopProject> projects = new ConcurrentHashMap<IJavaProject, IAopProject>();

	private IResourceChangeListener workspaceListener;

	// private IModelChangeListener modelChangeListener;

	public void addProject(IJavaProject project, IAopProject aopProject) {
		this.projects.put(project, aopProject);
	}

	public void fireModelChanged() {
		for (IAopModelChangedListener listener : listeners) {
			listener.changed();
		}
	}

	public List<IAopReference> getAdviceDefinition(IJavaElement je) {
		List<IAopReference> advices = new LinkedList<IAopReference>();
		for (IAopReference reference : getAllReferences()) {
			if (reference.getSource() != null && reference.getSource().equals(je)) {
				advices.add(reference);
			}
		}
		return advices;
	}

	public List<IAopReference> getAllReferences() {
		List<IAopReference> refs = new ArrayList<IAopReference>();
		for (Map.Entry<IJavaProject, IAopProject> e : projects.entrySet()) {
			refs.addAll(e.getValue().getAllReferences());
		}
		return refs;
	}

	public List<IAopReference> getAllReferencesForResource(IResource resource) {
		List<IAopReference> references = new ArrayList<IAopReference>();
		for (IAopReference ref : getAllReferences()) {
			if ((ref.getResource() != null && ref.getResource().equals(resource))
					|| (AopReferenceModelUtils.getBeanFromElementId(ref.getTargetBeanId()) != null && resource
							.equals(AopReferenceModelUtils.getBeanFromElementId(ref.getTargetBeanId())
									.getElementResource()))
					|| (ref.getSource() != null && resource.equals(ref.getSource().getResource()))
					|| (ref.getTarget() != null && resource.equals(ref.getTarget().getResource()))
					|| (ref.getDefinition().getResource().equals(resource))) {
				references.add(ref);
			}
		}
		return references;
	}

	public IAopProject getProject(IJavaProject project) {
		return getProjectWithInitialization(project);
	}

	public Collection<IAopProject> getProjects() {
		return this.projects.values();
	}

	public IAopProject getProjectWithInitialization(IJavaProject project) {
		if (this.projects.containsKey(project)) {
			return this.projects.get(project);
		}
		else {
			IAopProject aopProject = new AopProject(project);
			addProject(project, aopProject);
			return aopProject;
		}
	}

	public boolean isAdvice(IJavaElement je) {
		return getAdviceDefinition(je).size() > 0;
	}

	public boolean isAdvised(IJavaElement je) {
		if (je != null) {
			List<IAopReference> references = getAllReferences();
			for (IAopReference reference : references) {
				if (reference.getTarget().equals(je)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isAdvised(IBean bean) {
		if (bean != null) {
			String beanId = bean.getElementID();
			List<IAopReference> references = getAllReferences();
			for (IAopReference reference : references) {
				if (reference.getTargetBeanId().equals(beanId)) {
					return true;
				}
			}
		}
		return false;

	}

	public void registerAopModelChangedListener(IAopModelChangedListener listener) {
		this.listeners.add(listener);
	}

	public synchronized void removeProject(IJavaProject project) {
		for (IJavaProject jp : projects.keySet()) {
			if (jp.equals(project)) {
				projects.remove(jp);
				fireModelChanged();
				break;
			}
		}
	}

	public synchronized void clearProjects() {
		this.projects.clear();
	}

	public void shutdown() {
		// Remove the ResourceChangeListener from the Eclipse Workspace
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(workspaceListener);
		workspaceListener = null;

		// Persist model
		persistence.saveReferenceModel();

		// Remove all projects
		projects.clear();

		// BeansCorePlugin.getModel().removeChangeListener(modelChangeListener);
	}

	public void start() {
		// Add a ResourceChangeListener to the Eclipse Workspace
		workspaceListener = new SpringResourceChangeListener(new AopResourceChangeEvents());
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(workspaceListener, SpringResourceChangeListener.LISTENER_FLAGS);

		persistence = new AopReferenceModelPeristence();
		persistence.loadReferenceModel();

		// modelChangeListener = new AopBeansModelListener();
		// BeansCorePlugin.getModel().addChangeListener(modelChangeListener);
	}

	public void unregisterAopModelChangedListener(IAopModelChangedListener listener) {
		this.listeners.remove(listener);
	}

	/*
	 * private class AopBeansModelListener implements IModelChangeListener {
	 * 
	 * public void elementChanged(ModelChangeEvent event) { if (event.getType() == ModelChangeEvent.Type.REMOVED &&
	 * event.getSource() instanceof IResource) { IResource resource = (IResource) event.getSource(); IJavaProject jp =
	 * JdtUtils.getJavaProject(resource.getProject()); if (jp != null) { IAopProject ap = getProject(jp); if (ap !=
	 * null) { ap.clearReferencesForResource(resource); AopReferenceModelMarkerUtils.deleteProblemMarkers(resource); } }
	 * } } }
	 */
}
