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
import org.springframework.ide.eclipse.core.MarkerUtils;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinition;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinitionFactory;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;

/**
 * Base {@link IValidator} implementation that abstracts model visiting and
 * provides implementation hooks for sub classes.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public abstract class AbstractValidator implements IValidator {

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
					for (ValidationRuleDefinition ruleDefinition : ruleDefinitions) {
						if (subMonitor.isCanceled()) {
							throw new OperationCanceledException();
						}
						subMonitor.subTask("Validating element '"
								+ element.getElementName() + "' with rule '"
								+ ruleDefinition.getName() + "'");
						IValidationRule rule = ruleDefinition.getRule();
						if (rule.supports(element, context)) {
							context.setCurrentRuleId(ruleDefinition.getId());
							rule.validate(element, context, monitor);
						}
						subMonitor.worked(1);
					}
				}
				finally {
					subMonitor.done();
				}
				return true;
			}
			return false;
		}
	}

	/**
	 * unique id that should be used to identify the markers created by this
	 * validator
	 */
	private String markerId;
	
	/** unique id for this validator */ 
	private String validatorId;

	public void cleanup(IResource resource, IProgressMonitor monitor)
			throws CoreException {
		MarkerUtils.deleteMarkers(resource, getMarkerId());
	}

	/**
	 * Returns a newly created {@link IValidationContext} for the given
	 * {@link IResourceModelElement root element} and it's
	 * {@link IResourceModelElement context element}.
	 */
	protected abstract IValidationContext createContext(
			IResourceModelElement rootElement,
			IResourceModelElement contextElement);

	/**
	 * Returns {@link IValidationElementLifecycleManager}.
	 */
	protected abstract IValidationElementLifecycleManager createValidationElementLifecycleManager();

	/**
	 * Returns the ID of this validator's
	 * {@link IMarker validation problem marker} ID.
	 */
	protected final String getMarkerId() {
		return markerId;
	}

	/**
	 * Returns the list of enabled {@link ValidationRuleDefinition}s for this
	 * validator.
	 */
	protected Set<ValidationRuleDefinition> getRuleDefinitions(
			IResource resource) {
		return ValidationRuleDefinitionFactory.getEnabledRuleDefinitions(
				getValidatorId(), resource.getProject());
	}
	
	/**
	 * Returns the validator id
	 */
	protected final String getValidatorId() {
		return validatorId;
	}

	private IValidationElementLifecycleManager initValidationElementCallback(
			IResource resource) {
		IValidationElementLifecycleManager callback = createValidationElementLifecycleManager();
		callback.init(resource);
		return callback;
	}

	public void setMarkerId(String markerId) {
		this.markerId = markerId;
	}

	public void setValidatorId(String validatorId) {
		this.validatorId = validatorId;
	}

	/**
	 * Returns <code>true</code> if this validator is able to validate the
	 * given element.
	 */
	protected abstract boolean supports(IModelElement element);

	private Set<ValidationProblem> validate(
			IValidationElementLifecycleManager callback,
			Set<ValidationRuleDefinition> ruleDefinitions,
			SubProgressMonitor subMonitor) {
		Set<ValidationProblem> problems = new LinkedHashSet<ValidationProblem>();
		for (IResourceModelElement contextElement : callback
				.getContextElements()) {
			IValidationContext context = createContext(callback
					.getRootElement(), contextElement);
			if (context != null) {
				IModelElementVisitor visitor = new ValidationVisitor(context,
						ruleDefinitions);
				callback.getRootElement().accept(visitor, subMonitor);
				problems.addAll(context.getProblems());
			}
			if (subMonitor.isCanceled()) {
				throw new OperationCanceledException();
			}
		}
		return problems;
	}

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

				IValidationElementLifecycleManager callback = initValidationElementCallback(resource);

				IResourceModelElement rootElement = callback.getRootElement();
				Set<ValidationRuleDefinition> ruleDefinitions = getRuleDefinitions(resource);
				if (rootElement != null && ruleDefinitions != null
						&& ruleDefinitions.size() > 0) {
					Set<ValidationProblem> problems = validate(callback,
							ruleDefinitions, subMonitor);
					ValidationUtils.createProblemMarkers(resource, problems,
							getMarkerId());
				}

				// call close on callback to execute any required resource
				// cleanup in template
				callback.destory();

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
}
