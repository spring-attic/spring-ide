/*******************************************************************************
 * Copyright (c) 2012,2015 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.validation;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.corext.buildpath.ClasspathModifier;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.internal.model.SpringProject;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.core.model.validation.AbstractValidator;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationElementLifecycleManager;

public class SpringBootValidator extends AbstractValidator {

	public SpringBootValidator() {
	}

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
		return new SpringBootValidationContext(rootElement, contextElement);
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
		if (element instanceof SpringCompilationUnit) {
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
			if (getRootElement()!=null) {
				Set<IResourceModelElement> resources = new LinkedHashSet<IResourceModelElement>();
				resources.add(getRootElement());
				return resources;
			}
			return Collections.emptySet();
		}

		public IResourceModelElement getRootElement() {
			return rootElement;
		}

		public void init(IResource resource) {
			ICompilationUnit cu = getCompilationUnit(resource);
			if (cu!=null) {
				IModelElement parent = new SpringProject(SpringCore.getModel(),
						resource.getProject());
				String name = resource.getName();
				rootElement = new SpringCompilationUnit(cu, parent, name);
			}
		}

		@SuppressWarnings("restriction")
		private ICompilationUnit getCompilationUnit(IResource resource) {
			try {
				if (resource.getType() == IResource.FILE) {
					IJavaProject project = getJavaProject(resource);
					if (project.exists()) {
						IPackageFragmentRoot pfr = ClasspathModifier.getFragmentRoot(resource, project, null);
						if (pfr!=null && !ClasspathModifier.isExcluded(resource, project)) {
							return (ICompilationUnit)JavaCore.create((IFile) resource);
						}
					}
				}
			} catch (Exception e) {
				BootActivator.log(e);
			}
			return null;
		}

		private  IJavaProject getJavaProject(IResource resource) {
			IProject project = resource.getProject();
			return JavaCore.create(project);
		}
	}

}