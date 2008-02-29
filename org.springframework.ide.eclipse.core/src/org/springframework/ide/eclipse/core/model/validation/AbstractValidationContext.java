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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.ide.eclipse.core.MarkerUtils;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * Base {@link IValidationContext} implementation that handles creation of
 * {@link ValidationProblem}s instances.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
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

	/**
	 * Create {@link ValidationProblem} of severity info with the given
	 * information.
	 * @since 2.0.2
	 */
	public void info(IResourceModelElement element, String problemId,
			String message, ValidationProblemAttribute... attributes) {
		problems.addAll(createProblems(element, problemId,
				IValidationProblemMarker.SEVERITY_INFO, message, attributes));
	}

	/**
	 * Create {@link ValidationProblem} of severity warning with the given
	 * information.
	 */
	public void warning(IResourceModelElement element, String problemId,
			String message, ValidationProblemAttribute... attributes) {
		problems
				.addAll(createProblems(element, problemId,
						IValidationProblemMarker.SEVERITY_WARNING, message,
						attributes));
	}

	/**
	 * Create {@link ValidationProblem} of severity error with the given
	 * information.
	 */
	public void error(IResourceModelElement element, String problemId,
			String message, ValidationProblemAttribute... attributes) {
		problems.addAll(createProblems(element, problemId,
				IValidationProblemMarker.SEVERITY_ERROR, message, attributes));
	}

	/**
	 * Create {@link ValidationProblem}s for the given information.
	 * <p>
	 * Subclasses may override this method to change the creation of validation
	 * problems, e.g. to automatically create markers on other resources for
	 * certain error types.
	 * @since 2.0.3
	 */
	protected Set<ValidationProblem> createProblems(
			IResourceModelElement element, String problemId, int severity,
			String message, ValidationProblemAttribute... attributes) {
		Set<ValidationProblem> problems = new LinkedHashSet<ValidationProblem>(
				2);
		problems.add(createProblem(element, problemId, severity, message,
				attributes));
		return problems;
	}

	/**
	 * Create a single root {@link ValidationProblem} from the provided
	 * information.
	 */
	protected final ValidationProblem createProblem(
			IResourceModelElement element, String problemId, int severity,
			String message, ValidationProblemAttribute... attributes) {
		int line = (element instanceof ISourceModelElement ? ((ISourceModelElement) element)
				.getElementStartLine()
				: -1);

		// Add the element Id to the list of problem attributes
		String elementId = element.getElementID();
		List<ValidationProblemAttribute> attributeList = new ArrayList<ValidationProblemAttribute>(
				Arrays.asList(attributes));
		attributeList.add(new ValidationProblemAttribute(
				MarkerUtils.ELEMENT_ID_KEY, elementId));

		return new ValidationProblem(currentRuleId, problemId, severity,
				message, element.getElementResource(), line, attributeList
						.toArray(new ValidationProblemAttribute[attributeList
								.size()]));
	}

	protected void addProblems(Set<ValidationProblem> problems) {
		problems.addAll(problems);
	}

}
