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
package org.springframework.ide.eclipse.aop.core.model.internal;

import java.util.ArrayList;
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
import org.springframework.ide.eclipse.core.internal.model.resources.SpringResourceChangeListener;

public class AopReferenceModel implements IAopReferenceModel {

	private Map<IJavaProject, IAopProject> projects = new ConcurrentHashMap<IJavaProject, IAopProject>();

	private List<IAopModelChangedListener> listeners = new LinkedList<IAopModelChangedListener>();

	private IResourceChangeListener workspaceListener;

	public void addProject(IJavaProject project, IAopProject aopProject) {
		this.projects.put(project, aopProject);
	}

	public synchronized void removeProject(IJavaProject project) {
		for (IJavaProject jp : projects.keySet()) {
			if (jp.equals(project)) {
				projects.remove(jp);
				fireModelChanged();
			}
			break;
		}
	}

	public IAopProject getProject(IJavaProject project) {
		return getProjectWithInitialization(project);
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

	public List<IAopProject> getProjects() {
		return null;
	}

	@Deprecated
	public List<IAopReference> getAllReferences(IJavaProject project) {
		return getAllReferences();
	}

	public List<IAopReference> getAllReferences() {
		List<IAopReference> refs = new ArrayList<IAopReference>();
		for (Map.Entry<IJavaProject, IAopProject> e : projects.entrySet()) {
			refs.addAll(e.getValue().getAllReferences());
		}
		return refs;
	}

	public boolean isAdvised(IJavaElement je) {
		List<IAopReference> references = getAllReferences();

		for (IAopReference reference : references) {
			if (reference.getTarget().equals(je)) {
				return true;
			}
		}
		return false;
	}

	public boolean isAdvice(IJavaElement je) {
		return getAdviceDefinition(je).size() > 0;
	}

	public void registerAopModelChangedListener(
			IAopModelChangedListener listener) {
		this.listeners.add(listener);
	}

	public void unregisterAopModelChangedListener(
			IAopModelChangedListener listener) {
		this.listeners.remove(listener);
	}

	public List<IAopReference> getAdviceDefinition(IJavaElement je) {
		List<IAopReference> advices = new LinkedList<IAopReference>();
		IJavaProject project = je.getJavaProject();

		List<IAopReference> references = getAllReferences(project);
		for (IAopReference reference : references) {
			if (reference.getSource() != null
					&& reference.getSource().equals(je)) {
				advices.add(reference);
			}
		}
		return advices;
	}

	public void fireModelChanged() {
		for (IAopModelChangedListener listener : listeners) {
			listener.changed();
		}
	}

	public void shutdown() {
		// Remove the ResourceChangeListener from the Eclipse Workspace
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(workspaceListener);
		workspaceListener = null;

		// Remove all projects
		projects.clear();
	}

	public void startup() {
		// Add a ResourceChangeListener to the Eclipse Workspace
		workspaceListener = new SpringResourceChangeListener(
				new AopResourceChangeEvents());
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(workspaceListener,
				SpringResourceChangeListener.LISTENER_FLAGS);
	}

	public List<IAopReference> getAllReferencesForResource(IResource resource) {
		List<IAopReference> references = new ArrayList<IAopReference>();
		for (IAopReference ref : getAllReferences()) {
			if ((ref.getResource() != null && ref.getResource()
					.equals(resource))
					|| (AopReferenceModelUtils.getBeanFromElementId(ref
							.getTargetBeanId()) != null && resource
							.equals(AopReferenceModelUtils
									.getBeanFromElementId(ref.getTargetBeanId())
									.getElementResource()))
					|| (ref.getSource() != null && resource.equals(ref
							.getSource().getResource()))
					|| (ref.getTarget() != null && resource.equals(ref
							.getTarget().getResource()))
					|| (ref.getDefinition().getResource().equals(resource))) {
				references.add(ref);
			}
		}
		return references;
	}
}
