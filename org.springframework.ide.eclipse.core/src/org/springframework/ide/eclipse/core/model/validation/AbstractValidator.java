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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinition;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;

/**
 * @author Torsten Juergeleit
 * @since 2.0
 */
public abstract class AbstractValidator implements IValidator {

	public final void validate(Set<IResource> affectedResources,
			IProgressMonitor monitor) throws CoreException {
		SubProgressMonitor subMonitor = new SubProgressMonitor(monitor,
				affectedResources.size());
		try {
			for (IResource resource : affectedResources) {
				subMonitor.subTask("Validating resource '"
						+ resource.getFullPath().toString() + "'");
				cleanup(resource, subMonitor);
				if (subMonitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				IResourceModelElement rootElement = getRootElement(resource);
				Set<ValidationRuleDefinition> ruleDefinitions =
						getRuleDefinitions(resource);
				if (rootElement != null && ruleDefinitions != null
						&& ruleDefinitions.size() > 0) {
					Set<ValidationProblem> problems =
						new LinkedHashSet<ValidationProblem>();
					for (IResourceModelElement contextElement
							: getContextElements(rootElement)) {
						IValidationContext context = createContext(rootElement,
								contextElement);
						if (context != null) {
							IModelElementVisitor visitor =
									new ValidationVisitor(
											context, ruleDefinitions);
							rootElement.accept(visitor, monitor);
							problems.addAll(context.getProblems());
						}
						if (subMonitor.isCanceled()) {
							throw new OperationCanceledException();
						}
					}
					for (ValidationProblem problem : problems) {
						createProblemMarker(resource, problem);
					}
				}
				subMonitor.worked(1);
				if (subMonitor.isCanceled()) {
					throw new OperationCanceledException();
				}
			}
		}
		finally {
			subMonitor.done();
		}
	}

	/**
	 * Returns the list of enabled {@link ValidationRuleDefinition}s for this
	 * validator.
	 */
	protected abstract Set<ValidationRuleDefinition> getRuleDefinitions(
			IResource resource);

	/**
	 * Returns the {@link IResourceModelElement root element} for
	 * the given {@link IResource} which should be visited by the validator.
	 */
	protected abstract IResourceModelElement getRootElement(IResource resource);

	/**
	 * Returns a list of {@link IResourceModelElement context element}s for the
	 * given {@link IResourceModelElement root element} which should be used
	 * during validation.
	 */
	protected abstract Set<IResourceModelElement> getContextElements(
			IResourceModelElement rootElement);

	/**
	 * Returns a newly created {@link IValidationContext} for the given
	 * {@link IResourceModelElement root element} and it's
	 * {@link IResourceModelElement context element}.
	 */
	protected abstract IValidationContext createContext(
			IResourceModelElement rootElement,
			IResourceModelElement contextElement);

	/**
	 * Returns <code>true</code> if this validator is able to validate the given
	 * element.
	 */
	protected abstract boolean supports(IModelElement element);

	/**
	 * Creates an {@link IMarker} on the specified resource for the given
	 * validation problem.
	 */
	protected abstract void createProblemMarker(IResource resource,
			ValidationProblem problem);

	/**
	 * {@link IModelElementVisitor} implementation that validates a specified
	 * model tree.
	 */
	protected final class ValidationVisitor implements IModelElementVisitor {

		private Set<ValidationRuleDefinition> ruleDefinitions;
		private IValidationContext context;

		public ValidationVisitor(IValidationContext context,
				Set<ValidationRuleDefinition> ruleDefinitions) {
			this.ruleDefinitions = ruleDefinitions;
			this.context = context;
		}

		@SuppressWarnings("unchecked")
		public boolean visit(IModelElement element, IProgressMonitor monitor) {
			if (supports(element)) {
				SubProgressMonitor subMonitor = new SubProgressMonitor(monitor,
						ruleDefinitions.size());
				try {
					for (ValidationRuleDefinition ruleDefinition
							: ruleDefinitions) {
						if (subMonitor.isCanceled()) {
							throw new OperationCanceledException();
						}
						subMonitor.subTask("Validating element '"
								+ element.getElementName() + "' with rule '"
								+ ruleDefinition.getName() + "'");
						IValidationRule rule = ruleDefinition.getRule();
						if (rule.supports(element, context)) {
							context.setCurrentRuleId(ruleDefinition.getID());
							rule.validate(element, context, monitor);
						}
						subMonitor.worked(1);
					}
				} finally {
					subMonitor.done();
				}
			}
			return true;
		}
	}
}
