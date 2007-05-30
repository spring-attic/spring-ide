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
package org.springframework.ide.eclipse.webflow.core.internal.model.validation.rules;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.DefaultConversionService;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
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
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class VariableValidationRule implements
		IValidationRule<Variable, WebflowValidationContext> {

	private static final List<String> SCOPE_TYPES;

	static {
		SCOPE_TYPES = new ArrayList<String>();
		SCOPE_TYPES.add("request");
		SCOPE_TYPES.add("flash");
		SCOPE_TYPES.add("flow");
		SCOPE_TYPES.add("conversation");
	}

	private ConversionService conversionService = null;

	public boolean supports(IModelElement element, IValidationContext context) {
		return element instanceof Variable
				&& context instanceof WebflowValidationContext;
	}

	public void validate(Variable attribute, WebflowValidationContext context,
			IProgressMonitor monitor) {

		if (!StringUtils.hasText(attribute.getName())) {
			context.error(this, "NO_NAME_ATTRIBUTE", attribute,
					"Element 'var' requires 'name' attribute");
		}
		else {
			if (!StringUtils.hasText(attribute.getBean())
					&& !StringUtils.hasText(attribute.getClazz())
					&& !WebflowModelUtils.isReferencedBeanFound(context
							.getWebflowConfig(), attribute.getName())) {
				context.error(this, "INVALID_BEAN", attribute, MessageUtils
						.format("Referenced bean \"{0}\" cannot be found",
								attribute.getName()));
			}
		}
		if (StringUtils.hasText(attribute.getScope())
				&& !SCOPE_TYPES.contains(attribute.getScope())) {
			context.error(this, "INVALID_SCOPE", attribute, MessageUtils
					.format("Invalid scope \"{0}\" specified", attribute
							.getScope()));
		}
		if (StringUtils.hasText(attribute.getClazz())) {
			IType type = getJavaType(attribute.getClazz(), context);
			if (type == null) {
				context.error(this, "INVALID_SCOPE", attribute, MessageUtils
						.format("Class 'var' \"{0}\" cannot be resolved",
								attribute.getClazz()));
			}
			else
				try {
					if (type.isInterface() || Flags.isAbstract(type.getFlags())) {
						context.error(this, "INVALID_SCOPE", attribute, 
							MessageUtils.format(
								"Class 'var' \"{0}\" is either an Interface or abstract",
										attribute.getClazz()));					}
				}
				catch (JavaModelException e) {
				}
		}
		if (StringUtils.hasText(attribute.getBean())
				&& !WebflowModelUtils.isReferencedBeanFound(context
						.getWebflowConfig(), attribute.getBean())) {
			context.error(this, "INVALID_BEAN", attribute, MessageUtils.format(
					"Referenced bean \"{0}\" cannot be found", attribute
							.getBean()));
		}
	}

	private IType getJavaType(String className, WebflowValidationContext context) {
		IType type = BeansModelUtils.getJavaType(context.getWebflowConfig()
				.getProject().getProject(), className);
		if (type == null) {
			Class clazz = getConversionService().getClassByAlias(className);
			if (clazz != null) {
				type = BeansModelUtils.getJavaType(context.getWebflowConfig()
						.getProject().getProject(), clazz.getName());
			}
		}
		return type;
	}

	private ConversionService getConversionService() {
		if (this.conversionService == null) {
			this.conversionService = new DefaultConversionService();
		}
		return this.conversionService;
	}
}
