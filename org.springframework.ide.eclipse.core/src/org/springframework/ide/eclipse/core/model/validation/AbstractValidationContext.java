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
package org.springframework.ide.eclipse.core.model.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.springframework.ide.eclipse.core.MarkerUtils;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.project.IProjectContributorState;
import org.springframework.ide.eclipse.core.project.IProjectContributorStateAware;

/**
 * Base {@link IValidationContext} implementation that handles creation of {@link ValidationProblem} s instances.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public abstract class AbstractValidationContext implements IValidationContext, IProjectContributorStateAware,
		IProjectContributorState {

	private IResourceModelElement contextElement;

	private IProjectContributorState contributorState;

	private String currentRuleId;

	private Set<ValidationProblem> errors;

	private Set<ValidationProblem> infos;

	private IResourceModelElement rootElement;

	private Set<ValidationProblem> warnings;

	public AbstractValidationContext(IResourceModelElement rootElement, IResourceModelElement contextElement) {
		this.rootElement = rootElement;
		this.contextElement = contextElement;
		this.infos = new LinkedHashSet<ValidationProblem>();
		this.warnings = new LinkedHashSet<ValidationProblem>();
		this.errors = new LinkedHashSet<ValidationProblem>();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void addProblems(ValidationProblem... problems) {
		if (problems != null) {
			for (ValidationProblem problem : problems) {
				if (problem.getSeverity() == IMarker.SEVERITY_ERROR) {
					errors.add(problem);
					getProgressReportingState().incrementErrorCount();
				}
				else if (problem.getSeverity() == IMarker.SEVERITY_WARNING) {
					warnings.add(problem);
					getProgressReportingState().incrementWarningCount();
				}
				else if (problem.getSeverity() == IMarker.SEVERITY_INFO) {
					infos.add(problem);
					getProgressReportingState().incrementInfoCount();
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void error(IResourceModelElement element, String problemId, String message,
			ValidationProblemAttribute... attributes) {
		int count = errors.size();
		errors.addAll(createProblems(element, problemId, IValidationProblemMarker.SEVERITY_ERROR, message, attributes));
		getProgressReportingState().incrementErrorCountBy(errors.size() - count);
	}

	/**
	 * {@inheritDoc}
	 */
	public <T> T get(Class<T> clazz) {
		return contributorState.get(clazz);
	}

	/**
	 * {@inheritDoc}
	 */
	public IResourceModelElement getContextElement() {
		return contextElement;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<ValidationProblem> getProblems() {
		Set<ValidationProblem> problems = new HashSet<ValidationProblem>();
		problems.addAll(infos);
		problems.addAll(warnings);
		problems.addAll(errors);
		return problems;
	}

	/**
	 * {@inheritDoc}
	 */
	public IResourceModelElement getRootElement() {
		return rootElement;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hold(Object obj) {
		return contributorState.hold(obj);
	}

	/**
	 * {@inheritDoc}
	 */
	public void info(IResourceModelElement element, String problemId, String message,
			ValidationProblemAttribute... attributes) {
		int count = infos.size();
		infos.addAll(createProblems(element, problemId, IValidationProblemMarker.SEVERITY_INFO, message, attributes));
		getProgressReportingState().incrementInfoCountBy(infos.size() - count);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setCurrentRuleId(String ruleId) {
		currentRuleId = ruleId;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setProjectContributorState(IProjectContributorState contributorState) {
		this.contributorState = contributorState;
	}

	/**
	 * {@inheritDoc}
	 */
	public void warning(IResourceModelElement element, String problemId, String message,
			ValidationProblemAttribute... attributes) {
		int count = warnings.size();
		warnings.addAll(createProblems(element, problemId, IValidationProblemMarker.SEVERITY_WARNING, message,
				attributes));
		getProgressReportingState().incrementWarningCountBy(warnings.size() - count);
	}

	private ValidationProgressState getProgressReportingState() {
		if (contributorState != null && contributorState.get(ValidationProgressState.class) != null) {
			return contributorState.get(ValidationProgressState.class);
		}
		// Fall back
		return new ValidationProgressState();
	}

	/**
	 * Create a single root {@link ValidationProblem} from the provided information.
	 */
	protected final ValidationProblem createProblem(IResourceModelElement element, String problemId, int severity,
			String message, ValidationProblemAttribute... attributes) {

		// Get the line number from the element
		int line = getLineNumber(element);

		// Add the element Id to the list of problem attributes
		String elementId = element.getElementID();
		List<ValidationProblemAttribute> attributeList = new ArrayList<ValidationProblemAttribute>(Arrays
				.asList(attributes));
		attributeList.add(new ValidationProblemAttribute(MarkerUtils.ELEMENT_ID_KEY, elementId));

		return new ValidationProblem(currentRuleId, problemId, severity, message, element.getElementResource(), line,
				attributeList.toArray(new ValidationProblemAttribute[attributeList.size()]));
	}

	/**
	 * Create {@link ValidationProblem}s for the given information.
	 * <p>
	 * Subclasses may override this method to change the creation of validation problems, e.g. to automatically create
	 * markers on other resources for certain error types.
	 * @since 2.0.3
	 */
	protected Set<ValidationProblem> createProblems(IResourceModelElement element, String problemId, int severity,
			String message, ValidationProblemAttribute... attributes) {
		Set<ValidationProblem> problems = new LinkedHashSet<ValidationProblem>(2);
		problems.add(createProblem(element, problemId, severity, message, attributes));
		return problems;
	}

	/**
	 * Retrieve the line number from the given <code>element</code>.
	 */
	protected int getLineNumber(IResourceModelElement element) {
		int line = (element instanceof ISourceModelElement ? ((ISourceModelElement) element).getElementStartLine() : -1);

		// If the current element does not provide a valid line number iterate up the parent
		// hierarchy
		if (line == -1 && element.getElementParent() != null
				&& element.getElementParent() instanceof IResourceModelElement) {
			return getLineNumber((IResourceModelElement) element.getElementParent());
		}

		return line;
	}
	
	/**
	 * Returns the {@link IProjectContributorState} for subclass implementations. 
	 */
	protected IProjectContributorState getProjectContributorState() {
		return contributorState;
	}

}
