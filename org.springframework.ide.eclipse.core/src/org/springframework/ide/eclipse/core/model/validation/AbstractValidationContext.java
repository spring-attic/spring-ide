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
package org.springframework.ide.eclipse.core.model.validation;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinition;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * @author Torsten Juergeleit
 * @since 2.0
 */
public abstract class AbstractValidationContext {

	private IResource resource;
	private Set<ValidationProblem> problems;

	public AbstractValidationContext(IResource resource) {
		this.resource = resource;
		this.problems = new LinkedHashSet<ValidationProblem>();
	}
	public IResource getResource() {
		return resource;
	}

	public Set<ValidationProblem> getProblems() {
		return problems;
	}

	public void warning(IValidationRule rule, String errorId,
			IModelElement element, String message,
			ValidationProblemAttribute... attributes) {
		problems.add(createProblem(rule, errorId, element,
				IMarker.SEVERITY_WARNING, message, attributes));
	}

	public void error(IValidationRule rule, String errorId,
			IModelElement element, String message,
			ValidationProblemAttribute... attributes) {
		problems.add(createProblem(rule, errorId, element,
				IMarker.SEVERITY_ERROR, message, attributes));
	}

	private ValidationProblem createProblem(IValidationRule rule, String errorId,
			IModelElement element, int severity, String message,
			ValidationProblemAttribute... attributes) {
		String ruleID = (rule instanceof ValidationRuleDefinition
				? ((ValidationRuleDefinition) rule).getID() : null);
		IResource resource = (element instanceof IResourceModelElement
				? ((IResourceModelElement) element).getElementResource()
						: null);
		int line = (element instanceof ISourceModelElement
				? ((ISourceModelElement) element).getElementStartLine() : -1);
		return new ValidationProblem(ruleID, errorId, severity, message,
				resource, line, attributes);
	}
}
