/*******************************************************************************
 * Copyright (c) 2009, 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.autowire.internal.validation.rules;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.beans.core.autowire.internal.provider.AutowireDependencyProvider;
import org.springframework.ide.eclipse.beans.core.internal.model.XMLBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModelElement;
import org.springframework.ide.eclipse.beans.core.model.validation.IBeansValidationContext;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblem;

/**
 * {@link IValidationRule} that reads {@link ValidationProblem}s that got stored during processing of autowiring
 * configurations.
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.2.7
 */
public class AutowireDependencyProblemsRule implements IValidationRule<IBeansConfig, IBeansValidationContext> {

	/**
	 * Checks if the this rule supports given {@link IModelElement} and {@link IValidationContext}.
	 * @return true if element is a {@link XMLBeansConfig} and context is {@link IBeansValidationContext}
	 */
	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof IBeansConfig && context instanceof IBeansValidationContext;
	}

	/**
	 * Pass all {@link ValidationProblem}s that are stored in given {@link XMLBeansConfig} into the <code>context</code>.
	 */
	public void validate(IBeansConfig element, IBeansValidationContext context, IProgressMonitor monitor) {
		AutowireDependencyProvider provider = new AutowireDependencyProvider(element, (IBeansModelElement) context
				.getContextElement());
		provider.setProjectClassLoaderSupport(context.getProjectClassLoaderSupport());
		
		provider.resolveAutowiredDependencies();
		for (ValidationProblem problem : provider.getValidationProblems()) {
			context.addProblems(problem);
		}
	}

}
