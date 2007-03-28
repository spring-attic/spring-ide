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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.aop.core.model.IAopReferenceModel;
import org.springframework.ide.eclipse.aop.core.model.IAopModelChangedListener;
import org.springframework.ide.eclipse.aop.core.model.IAopProject;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;
import org.springframework.ide.eclipse.aop.core.model.builder.AopReferenceModelBuilder;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelUtils;

public class AopReferenceModel implements IAopReferenceModel {

	private Map<IJavaProject, IAopProject> projects = new ConcurrentHashMap<IJavaProject, IAopProject>();

	private List<IAopModelChangedListener> listeners = new LinkedList<IAopModelChangedListener>();

	public void addProject(IJavaProject project, IAopProject aopProject) {
		this.projects.put(project, aopProject);
	}

	public IAopProject getProject(IJavaProject project) {
		
		// currently disabled automatic creation of AOP reference model
		return getProjectWithInitialization(project);
		
		/*if (this.projects.containsKey(project)) {
			return this.projects.get(project);
		} 
		else { 
			createModel(project);
			return this.projects.get(project);
		}*/
	}

	@SuppressWarnings("unused")
	private void createModel(IJavaProject project) {
		Set<IFile> resourcesToBuild = AopReferenceModelUtils
				.getFilesToBuildFromBeansProject(project.getProject());
		AopReferenceModelBuilder.buildAopModel(project.getProject(),
				resourcesToBuild);
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

	public List<IAopReference> getAllReferences(IJavaProject project) {
		List<IAopReference> refs = new ArrayList<IAopReference>();
		for (Map.Entry<IJavaProject, IAopProject> e : projects.entrySet()) {
			refs.addAll(e.getValue().getAllReferences());
		}
		return refs;
	}

	public boolean isAdvised(IJavaElement je) {
		IJavaProject project = je.getJavaProject();
		List<IAopReference> references = getAllReferences(project);

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
}
