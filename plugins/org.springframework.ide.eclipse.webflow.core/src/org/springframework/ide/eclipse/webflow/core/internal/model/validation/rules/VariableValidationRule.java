/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.internal.model.validation.rules;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.core.MessageUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springframework.ide.eclipse.webflow.core.internal.model.Variable;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.validation.WebflowValidationContext;
import org.springframework.util.StringUtils;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class VariableValidationRule implements IValidationRule<Variable, WebflowValidationContext> {

	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof Variable && context instanceof WebflowValidationContext;
	}

	public void validate(Variable attribute, WebflowValidationContext context,
			IProgressMonitor monitor) {

		if (context.isVersion1()) {
			if (!StringUtils.hasText(attribute.getName())) {
				context.error(attribute, "NO_NAME_ATTRIBUTE",
						"Element 'var' requires 'name' attribute");
			}
			else {
				if (!StringUtils.hasText(attribute.getBean())
						&& !StringUtils.hasText(attribute.getClazz())
						&& !WebflowModelUtils.isReferencedBeanFound(context.getWebflowConfig(),
								attribute.getName())) {
					context.error(attribute, "INVALID_BEAN", MessageUtils.format(
							"Referenced bean \"{0}\" cannot be found", attribute.getName()));
				}
			}
			if (StringUtils.hasText(attribute.getScope())
					&& !WebflowValidationRuleUtils.SCOPE_TYPES.contains(attribute.getScope())) {
				context.error(attribute, "INVALID_SCOPE", MessageUtils.format(
						"Invalid scope \"{0}\" specified", attribute.getScope()));
			}
			checkClassAttribute(attribute, context);
			if (StringUtils.hasText(attribute.getBean())
					&& !WebflowModelUtils.isReferencedBeanFound(context.getWebflowConfig(),
							attribute.getBean())) {
				context.error(attribute, "INVALID_BEAN", MessageUtils.format(
						"Referenced bean \"{0}\" cannot be found", attribute.getBean()));
			}
		}
		else {
			checkNameAttribute(attribute, context);
			checkClassAttribute(attribute, context);
		}
	}

	private void checkNameAttribute(Variable attribute, WebflowValidationContext context) {
		if (!StringUtils.hasText(attribute.getName())) {
			context.error(attribute, "NO_NAME_ATTRIBUTE",
					"Element 'var' requires 'name' attribute");
		}
	}

	private void checkClassAttribute(Variable attribute, WebflowValidationContext context) {
		if (StringUtils.hasText(attribute.getClazz())) {
			IType type = WebflowValidationRuleUtils.getJavaType(attribute.getClazz(), context);
			if (type == null) {
				context.error(attribute, "INVALID_TYPE", MessageUtils.format(
						"Class 'var' \"{0}\" cannot be resolved", attribute.getClazz()));
			}
			else
				try {
					if (type.isInterface() || Flags.isAbstract(type.getFlags())) {
						context.error(attribute, "INVALID_TYPE", MessageUtils.format(
								"Class 'var' \"{0}\" is either an Interface or abstract",
								attribute.getClazz()));
					}
				}
				catch (JavaModelException e) {
				}
		}
	}
}
