/*******************************************************************************
 * Copyright (c) 2008, 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.internal.model.validation;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.core.model.validation.AbstractValidator;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationElementLifecycleManager;
import org.springframework.ide.eclipse.core.model.validation.IValidator;

/**
 * {@link IValidator} that validates {@link ISpringProject}.
 * <p>
 * This validator has no knowledge about the beans.core model and should
 * therefore only be used to validate very basic and general aspects in a Spring
 * project.
 * @author Christian Dupuis
 * @since 2.0.3
 */
public class SpringValidator extends AbstractValidator {

	/**
	 * Creates the {@link IValidationContext} used to validate the spring.core
	 * model.
	 * <p>
	 * This implementation creates an instance of
	 * {@link SpringValidationContext}.
	 */
	@Override
	protected IValidationContext createContext(
			IResourceModelElement rootElement,
			IResourceModelElement contextElement) {
		return new SpringValidationContext(rootElement, contextElement);
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

	/**
	 * This validator only supports {@link ISpringProject}.
	 */
	@Override
	protected boolean supports(IModelElement element) {
		return element instanceof ISpringProject;
	}
	
	/**
	 * Returns the corresponding {@link IProject} for the passed in object.
	 */
	public Set<IResource> deriveResources(Object object) {
		Set<IResource> resources = new LinkedHashSet<IResource>();
		if (object instanceof IResource) {
			resources.add(((IResource) object).getProject());
		}
		else if (object instanceof ISpringProject) {
			resources.add(((ISpringProject) object).getProject());
		}
		return resources;
	}

	/**
	 * For every {@link IResource} that is passed in the corresponding
	 * {@link IProject} will be returned.
	 */
	public Set<IResource> getAffectedResources(IResource resource, int kind, int deltaKind)
			throws CoreException {
		Set<IResource> resources = new LinkedHashSet<IResource>();
		resources.add(resource.getProject());
		return resources;
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
			rootElement = SpringCore.getModel().getProject(
					resource.getProject());
		}

	}

}
