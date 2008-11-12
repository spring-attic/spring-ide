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
package org.springframework.ide.eclipse.beans.core.internal.model.validation.rules;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.validation.IBeansValidationContext;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblem;

/**
 * {@link IValidationRule} that reads {@link ValidationProblem}s that got stored during parsing of
 * the {@link IBeansConfig}.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class SpringParsingProblemsRule implements
		IValidationRule<BeansConfig, IBeansValidationContext> {

	/** Regular expressions to that must be ignored and not reported to the user */
	private static final List<Pattern> IGNORABLE_ERROR_MESSAGE_PATTERNS = Arrays.asList(new Pattern[] {
		Pattern.compile("Failed to import bean definitions from relative location \\[(.*)\\]:.*"),
		Pattern.compile("Failed to import bean definitions from URL location \\[(.*)\\]:.*") });

	/**
	 * Checks if the this rule supports given {@link IModelElement} and {@link IValidationContext}.
	 * @return true if element is a {@link BeansConfig} and context is
	 * {@link IBeansValidationContext}
	 */
	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof BeansConfig && context instanceof IBeansValidationContext;
	}

	/**
	 * Pass all {@link ValidationProblem}s that are stored in given {@link BeansConfig} into the
	 * <code>context</code>.
	 */
	public void validate(BeansConfig element, IBeansValidationContext context,
			IProgressMonitor monitor) {
		for (ValidationProblem problem : element.getProblems()) {
			if (!isMessageIgnorable(problem.getMessage())) {
				context.getProblems().add(problem);
			}
		}
	}

	/**
	 * Checks if the given problem message string should be displayed. 
	 */
	private boolean isMessageIgnorable(String message) {
		for (Pattern pattern : IGNORABLE_ERROR_MESSAGE_PATTERNS) {
			if (pattern.matcher(message).matches()) {
				return true;
			}
		}
		return false;
	}
}
