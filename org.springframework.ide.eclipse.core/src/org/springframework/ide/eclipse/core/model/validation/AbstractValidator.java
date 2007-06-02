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

import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinition;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinitionFactory;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;

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
				IValidationContext context = createContext(resource);
				Set<ValidationRuleDefinition> ruleDefinitions =
						ValidationRuleDefinitionFactory
								.getEnabledRuleDefinitions(getId(), resource
										.getProject());
				IModelElementVisitor visitor = new ValidationVisitor(context,
						ruleDefinitions);
				for (IModelElement rootElement : context.getRootElements()) {
					context.setCurrentRootElement(rootElement);
					rootElement.accept(visitor, monitor);
					if (subMonitor.isCanceled()) {
						throw new OperationCanceledException();
					}
				}
				for (ValidationProblem problem : context.getProblems()) {
					createProblemMarker(resource, problem);
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
	 * Returns the full-qualified ID of this validator, e.g.
	 * "org.springframework.ide.eclipse.beans.core.validator".
	 */
	protected abstract String getId();

	/**
	 * Returns a newly created {@link IValidationContext}. 
	 */
	protected abstract IValidationContext createContext(IResource resource);

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
