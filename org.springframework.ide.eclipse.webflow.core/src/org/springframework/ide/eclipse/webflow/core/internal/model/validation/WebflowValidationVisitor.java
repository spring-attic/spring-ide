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
package org.springframework.ide.eclipse.webflow.core.internal.model.validation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.core.internal.model.validation.ValidationRuleDefinition;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * {@link IModelElementVisitor} implementation that collects and stores
 * {@link WebflowValidationProblem} in an internal list.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class WebflowValidationVisitor implements IModelElementVisitor {

	private java.util.Set<ValidationRuleDefinition> validationRules;

	private IValidationContext<IWebflowModelElement> validationContext;

	public WebflowValidationVisitor(
			IValidationContext<IWebflowModelElement> validationContext,
			java.util.Set<ValidationRuleDefinition> validationRules) {
		this.validationRules = validationRules;
		this.validationContext = validationContext;
	}

	@SuppressWarnings("unchecked")
	public boolean visit(IModelElement modelElement, IProgressMonitor monitor) {
		if (modelElement instanceof IWebflowModelElement) {
			IWebflowModelElement element = (IWebflowModelElement) modelElement;
			for (ValidationRuleDefinition validationRule : this.validationRules) {
				if (validationRule.getRule().supports(element,
						this.validationContext)) {
					validationRule.getRule().validate(element,
							this.validationContext, monitor);
				}
			}
		}
		return true;
	}
}