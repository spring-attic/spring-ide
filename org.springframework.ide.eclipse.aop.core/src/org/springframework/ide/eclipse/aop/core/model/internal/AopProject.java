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
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.aop.core.logging.AopLog;
import org.springframework.ide.eclipse.aop.core.model.IAopProject;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;

public class AopProject implements IAopProject {

	private List<IAopReference> references = new ArrayList<IAopReference>();

	private IJavaProject project;

	public AopProject(IJavaProject project) {
		this.project = project;
	}

	public void addAopReference(IAopReference reference) {
		AopLog.log(AopLog.BUILDER_MESSAGES, "Created AOP reference ["
				+ reference + "]");
		this.references.add(reference);
	}

	public List<IAopReference> getAllReferences() {
		return this.references;
	}

	public IJavaProject getProject() {
		return this.project;
	}

	public void clearReferencesForResource(IResource resource) {
		List<IAopReference> toRemove = new ArrayList<IAopReference>();
		for (IAopReference reference : this.references) {
			if (reference.getDefinition().getResource().equals(resource)) {
				toRemove.add(reference);
			}
		}
		this.references.removeAll(toRemove);
	}

	public List<IAopReference> getReferencesForResource(IResource resource) {
		List<IAopReference> list = new ArrayList<IAopReference>();
		for (IAopReference reference : this.references) {
			if (reference.getResource().equals(resource)
					|| reference.getDefinition().getResource().equals(resource)) {
				list.add(reference);
			}
		}
		return list;
	}
}
