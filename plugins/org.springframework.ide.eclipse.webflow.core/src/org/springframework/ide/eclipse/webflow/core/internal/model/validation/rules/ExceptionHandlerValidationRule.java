/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
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
import org.springframework.ide.eclipse.core.MessageUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springframework.ide.eclipse.webflow.core.internal.model.ExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.validation.WebflowValidationContext;
import org.springframework.util.StringUtils;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class ExceptionHandlerValidationRule implements
		IValidationRule<ExceptionHandler, WebflowValidationContext> {

	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof ExceptionHandler
				&& context instanceof WebflowValidationContext;
	}

	public void validate(ExceptionHandler handler,
			WebflowValidationContext context, IProgressMonitor monitor) {

		if (!StringUtils.hasText(handler.getBean())) {
			context.error(handler, "NO_BEAN_ATTRIBUTE",
					"Element 'exception-handler' requires 'bean' attribute");
		}
		else if (!WebflowModelUtils.isReferencedBeanFound(context
				.getWebflowConfig(), handler.getBean())) {
			context.error(handler, "INVALID_BEAN", MessageUtils
					.format("Referenced bean \"{0}\" cannot be found", handler
							.getBean()));
		}
	}
}
