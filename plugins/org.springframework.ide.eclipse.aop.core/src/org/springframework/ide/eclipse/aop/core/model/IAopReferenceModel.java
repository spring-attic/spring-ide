/*******************************************************************************
 * Copyright (c) 2006, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.core.model;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.beans.core.model.IBean;

/**
 * @author Christian Dupuis
 */
public interface IAopReferenceModel {
	
	void start();

	void shutdown();

	void removeProject(IJavaProject project);

	void addProject(IJavaProject project, IAopProject aopProject);

	void fireModelChanged();

	List<IAopReference> getAdviceDefinition(IJavaElement je);

	List<IAopReference> getAllReferences();
	
	List<IAopReference> getAllReferencesForResource(IResource resource);

	IAopProject getProject(IJavaProject project);
	
	Collection<IAopProject> getProjects();

	boolean isAdvice(IJavaElement je);

	boolean isAdvised(IJavaElement je);
	
	boolean isAdvised(IBean bean);

	void registerAopModelChangedListener(IAopModelChangedListener listener);

	void unregisterAopModelChangedListener(IAopModelChangedListener listener);
	
	void clearProjects();

}
