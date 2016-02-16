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
import org.springframework.ide.eclipse.webflow.core.internal.model.Set;
import org.springframework.ide.eclipse.webflow.core.internal.model.validation.WebflowValidationContext;
import org.springframework.util.StringUtils;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class SetValidationRule implements IValidationRule<Set, WebflowValidationContext> {

	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof Set && context instanceof WebflowValidationContext;
	}

	public void validate(Set set, WebflowValidationContext context, IProgressMonitor monitor) {

		if (context.isVersion1()) {
			if (!StringUtils.hasText(set.getAttribute())) {
				context.error(set, "NO_ATTRIBUTE_ATTRIBUTE",
						"Element 'set' requires 'attribute' attribute");
			}
			if (!StringUtils.hasText(set.getValue())) {
				context
						.error(set, "NO_VALUE_ATTRIBUTE",
								"Element 'set' requires 'value' attribute");
			}
			if (StringUtils.hasText(set.getScope())
					&& !WebflowValidationRuleUtils.SCOPE_TYPES.contains(set.getScope())) {
				context.error(set, "NO_ATTRIBUTE_ATTRIBUTE",
						"Element 'set' requires 'attribute' attribute");
				context.error(set, "NO_VALUE_ATTRIBUTE", MessageUtils.format(
						"Element 'set' requires 'value' attribute", set.getScope()));
			}
		}
		else {
			if (!StringUtils.hasText(set.getName())) {
				context.error(set, "NO_NAME_ATTRIBUTE", "Element 'set' requires 'name' attribute");
			}
			if (!StringUtils.hasText(set.getValue())) {
				context
						.error(set, "NO_VALUE_ATTRIBUTE",
								"Element 'set' requires 'value' attribute");
			}
			if (StringUtils.hasText(set.getSetType())) {
				IType type = WebflowValidationRuleUtils.getJavaType(set.getSetType(), context);
				if (type == null) {
					context.error(set, "INVALID_TYPE", MessageUtils.format(
							"Set 'type' \"{0}\" cannot be resolved", set.getSetType()));
				}
				else {
					try {
						if (type.isInterface() || Flags.isAbstract(type.getFlags())) {
							context.error(set, "INVALID_TYPE", MessageUtils.format(
									"Set 'type' \"{0}\" is either an Interface or abstract", set
											.getSetType()));
						}
					}
					catch (JavaModelException e) {
					}
				}
			}
		}
	}
}
