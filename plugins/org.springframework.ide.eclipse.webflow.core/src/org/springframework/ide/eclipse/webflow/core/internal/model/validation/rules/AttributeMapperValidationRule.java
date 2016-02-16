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
import org.springframework.ide.eclipse.webflow.core.internal.model.AttributeMapper;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.validation.WebflowValidationContext;
import org.springframework.util.StringUtils;

/**
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class AttributeMapperValidationRule implements
		IValidationRule<AttributeMapper, WebflowValidationContext> {

	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof AttributeMapper
				&& context instanceof WebflowValidationContext;
	}

	public void validate(AttributeMapper state,
			WebflowValidationContext context, IProgressMonitor monitor) {
		if (StringUtils.hasText(state.getBean())
				&& !WebflowModelUtils.isReferencedBeanFound(context
						.getWebflowConfig(), state.getBean())) {
			context.error(state, "INVALID_BEAN_REFERENCE", MessageUtils
					.format("Referenced bean \"{0}\" cannot be found", state
							.getBean()));
		}
	}
}
