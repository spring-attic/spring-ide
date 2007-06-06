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

import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * @author Torsten Juergeleit
 * @since 2.0
 */
public abstract class AbstractValidationContext implements IValidationContext {

	private IResourceModelElement rootElement;
	private IResourceModelElement contextElement;
	private String currentRuleId;
	private Set<ValidationProblem> problems;

	public AbstractValidationContext(IResourceModelElement rootElement,
			IResourceModelElement contextElement) {
		this.rootElement = rootElement;
		this.contextElement = contextElement;
		this.problems = new LinkedHashSet<ValidationProblem>();
	}

	public IResourceModelElement getRootElement() {
		return rootElement;
	}

	public IResourceModelElement getContextElement() {
		return contextElement;
	}
	
	public void setCurrentRuleId(String ruleId) {
		currentRuleId = ruleId;
	}

	public Set<ValidationProblem> getProblems() {
		return problems;
	}

	public void warning(IModelElement element, String problemId,
			String message, ValidationProblemAttribute... attributes) {
		problems.add(createProblem(element, message,
				IValidationProblemMarker.SEVERITY_WARNING, problemId, attributes));
	}

	public void error(IModelElement element, String problemId, String message,
			ValidationProblemAttribute... attributes) {
		problems.add(createProblem(element, problemId,
				IValidationProblemMarker.SEVERITY_ERROR, message, attributes));
	}

	protected final ValidationProblem createProblem(IModelElement element,
			String problemId, int severity, String message,
			ValidationProblemAttribute... attributes) {
		int line = (element instanceof ISourceModelElement
				? ((ISourceModelElement) element).getElementStartLine() : -1);
		return new ValidationProblem(currentRuleId, problemId, severity,
				message, line, attributes);
	}

	protected void addProblems(Set<ValidationProblem> problems) {
		problems.addAll(problems);
	}
}
