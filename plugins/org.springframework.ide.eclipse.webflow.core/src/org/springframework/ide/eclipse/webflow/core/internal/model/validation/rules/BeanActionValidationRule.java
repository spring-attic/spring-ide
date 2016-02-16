/*******************************************************************************
 * Copyright (c) 2007, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.internal.model.validation.rules;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.ide.eclipse.core.MessageUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springframework.ide.eclipse.webflow.core.internal.model.BeanAction;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.validation.WebflowValidationContext;
import org.springframework.util.StringUtils;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class BeanActionValidationRule implements IValidationRule<BeanAction, WebflowValidationContext> {

	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof BeanAction && context instanceof WebflowValidationContext;
	}

	public void validate(BeanAction action, WebflowValidationContext context, IProgressMonitor monitor) {
		if (!StringUtils.hasText(action.getBean())) {
			context.error(action, "NO_BEAN_ATTRIBUTE", "Element 'bean-action' requires bean attribute");
		}
		else if (!WebflowModelUtils.isReferencedBeanFound(context.getWebflowConfig(), action.getBean())) {
			context.error(action, "INVALID_BEAN", MessageUtils.format("Referenced bean \"{0}\" cannot be found", action
					.getBean()));
		}
		if (!StringUtils.hasText(action.getMethod())) {
			context.error(action, "NO_METHOD_ATTRIBUTE", "Element 'bean-action' requires method attribute");
		}
		else if (!JdtUtils.doesImplement(context.getWebflowConfig().getElementResource(), WebflowModelUtils
				.getActionType(context.getWebflowConfig(), action.getNode()), FactoryBean.class.getName())) {
			Set<IMethod> methods = WebflowModelUtils.getActionMethods(context.getWebflowConfig(), action.getNode());
			boolean found = false;
			for (IMethod method : methods) {
				if (method.getElementName().equals(action.getMethod())) {
					found = true;
					break;
				}
			}
			if (!found) {
				context.error(action, "INVALID_ACTION_METHOD", MessageUtils.format(
						"Referenced action method \"{0}\" cannot be found or is not a valid action method", action
								.getMethod()));
			}
		}
	}
}
