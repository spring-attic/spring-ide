/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.data.internal.validation;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.internal.model.SpringProject;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.core.model.validation.AbstractValidator;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationElementLifecycleManager;

/**
 * @author Tomasz Zarna
 *
 */
public class SpringDataValidator extends AbstractValidator {

	public Set<IResource> deriveResources(Object object) {
		Set<IResource> resources = new LinkedHashSet<IResource>();
		if (object instanceof ISpringProject) {
			resources.add(((ISpringProject) object).getProject());
		}
		return resources;
	}

	public Set<IResource> getAffectedResources(IResource resource, int kind,
			int deltaKind) throws CoreException {
		if (resource instanceof IFile && resource.getName().endsWith(".java")
				&& JavaCore.create((IFile) resource) != null) {
			return Collections.singleton(resource);
		}
		return Collections.emptySet();
	}

	@Override
	protected IValidationContext createContext(
			IResourceModelElement rootElement,
			IResourceModelElement contextElement) {
		return new SpringDataValidationContext(rootElement, contextElement);
	}

	/**
	 * Creates a {@link IValidationElementLifecycleManager} instance.
	 * <p>
	 * This implementation creates an instance of
	 * {@link SpringValidationElementLifecycleManager}.
	 */
	@Override
	protected IValidationElementLifecycleManager createValidationElementLifecycleManager() {
		return new SpringValidationElementLifecycleManager();
	}

	@Override
	protected boolean supports(IModelElement element) {
		if (element instanceof CompilationUnit) {
			return true;
		}
		return false;
	}

	private static class SpringValidationElementLifecycleManager implements
			IValidationElementLifecycleManager {

		private IResourceModelElement rootElement;

		public void destroy() {
			// Nothing to do here.
		}

		public Set<IResourceModelElement> getContextElements() {
			Set<IResourceModelElement> resources = new LinkedHashSet<IResourceModelElement>();
			resources.add(getRootElement());
			return resources;
		}

		public IResourceModelElement getRootElement() {
			return rootElement;
		}

		public void init(IResource resource) {
			ICompilationUnit cu = getCompilationUnit(resource);
			IModelElement parent = new SpringProject(SpringCore.getModel(),
					resource.getProject());
			String name = resource.getName();
			rootElement = new CompilationUnit(cu, parent, name);
		}

		private ICompilationUnit getCompilationUnit(IResource resource) {
			IJavaProject project = getJavaProject(resource);
			if (project == null) {
				return null;
			}
			if (resource.getType() == IResource.FILE) {
				return (ICompilationUnit)JavaCore.create((IFile) resource);
			}
			return null;
		}

		private  IJavaProject getJavaProject(IResource resource) {
			IProject project = resource.getProject();
			return JavaCore.create(project);
		}
	}
}
