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
package org.springframework.ide.eclipse.beans.core.internal.model.validation.rules;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.validation.BeansValidationContext;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblem;

/**
 * {@link IValidationRule} that reads {@link ValidationProblem}s that got
 * stored during parsing of the {@link IBeansConfig}.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class SpringParsingProblemsRule implements
		IValidationRule<BeansConfig, BeansValidationContext> {

	/**
	 * Checks if the this rule supports given {@link IModelElement} and
	 * {@link IValidationContext}.
	 * @return true if element is a {@link BeansConfig} and context is
	 * {@link BeansValidationContext}
	 */
	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof BeansConfig
				&& context instanceof BeansValidationContext;
	}

	/**
	 * Pass all {@link ValidationProblem}s that are stored in given
	 * {@link BeansConfig} into the <code>context</code>.
	 */
	public void validate(BeansConfig element, BeansValidationContext context,
			IProgressMonitor monitor) {
		context.getProblems().addAll(element.getProblems());
	}
}
