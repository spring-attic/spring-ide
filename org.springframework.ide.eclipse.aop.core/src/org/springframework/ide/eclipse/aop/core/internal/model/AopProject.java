/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.aop.core.logging.AopLog;
import org.springframework.ide.eclipse.aop.core.model.IAopProject;
import org.springframework.ide.eclipse.aop.core.model.IAopReference;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class AopProject implements IAopProject {

	private IJavaProject project;

	private Set<IAopReference> references = new LinkedHashSet<IAopReference>();

	protected final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	protected final Lock r = rwl.readLock();

	protected final Lock w = rwl.writeLock();

	public AopProject(IJavaProject project) {
		this.project = project;
	}

	public void addAopReference(IAopReference reference) {
		AopLog.log(AopLog.BUILDER_MESSAGES, "Created AOP reference [" + reference + "]");
		try {
			w.lock();
			this.references.add(reference);
		}
		finally {
			w.unlock();
		}
	}

	public void clearReferencesForResource(IResource resource) {
		List<IAopReference> toRemove = new ArrayList<IAopReference>();
		try {
			w.lock();
			for (IAopReference reference : this.references) {
				if (resource != null && resource.equals(reference.getDefinition().getResource())) {
					toRemove.add(reference);
				}
			}
			this.references.removeAll(toRemove);
		}
		finally {
			w.unlock();
		}
	}

	public Set<IAopReference> getAllReferences() {
		return this.references;
	}

	public IJavaProject getProject() {
		return this.project;
	}

	public Set<IAopReference> getReferencesForResource(IResource resource) {
		try {
			r.lock();
			Set<IAopReference> list = new LinkedHashSet<IAopReference>();
			for (IAopReference reference : this.references) {
				if (reference.getResource().equals(resource)
						|| reference.getDefinition().getResource().equals(resource)) {
					list.add(reference);
				}
			}
			return list;
		}
		finally {
			r.unlock();
		}
	}
}
