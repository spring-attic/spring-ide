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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IType;
import org.springframework.binding.convert.ConversionService;
import org.springframework.binding.convert.support.DefaultConversionService;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.core.model.validation.IValidationRule;
import org.springframework.ide.eclipse.webflow.core.internal.model.validation.WebflowValidationContext;

/**
 * Utility class for the use from within other {@link IValidationRule}.
 * @author Christian Dupuis
 * @since 2.0
 */
public abstract class WebflowValidationRuleUtils {

	private static ConversionService conversionService = null;

	protected static final List<String> SCOPE_TYPES;

	static {
		SCOPE_TYPES = new ArrayList<String>();
		SCOPE_TYPES.add("request");
		SCOPE_TYPES.add("flash");
		SCOPE_TYPES.add("flow");
		SCOPE_TYPES.add("conversation");
	}

	protected static IType getJavaType(String className,
			WebflowValidationContext context) {
		IType type = JdtUtils.getJavaType(context.getWebflowConfig()
				.getProject().getProject(), className);
		if (type == null) {
			Class clazz = getConversionService().getClassByAlias(className);
			if (clazz != null) {
				type = JdtUtils.getJavaType(context.getWebflowConfig()
						.getProject().getProject(), clazz.getName());
			}
		}
		return type;
	}

	private static ConversionService getConversionService() {
		if (conversionService == null) {
			conversionService = new DefaultConversionService();
		}
		return conversionService;
	}
}
