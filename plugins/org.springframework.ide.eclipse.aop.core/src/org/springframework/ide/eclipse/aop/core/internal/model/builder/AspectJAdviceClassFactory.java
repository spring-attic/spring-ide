/*******************************************************************************
 * Copyright (c) 2007, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.aop.core.internal.model.builder;

import org.springframework.aop.aspectj.AbstractAspectJAdvice;
import org.springframework.ide.eclipse.aop.core.model.IAdvisorDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPE;
import org.springframework.ide.eclipse.core.java.ClassUtils;

/**
 * Factory class that creates {@link Class} instances for {@link AbstractAspectJAdvice} subclass depending of
 * {@link IAspectDefinition}.
 * @author Christian Dupuis
 * @since 2.0
 */
public class AspectJAdviceClassFactory {

	public static Class<?> getAspectJAdviceClass(IAspectDefinition info) throws ClassNotFoundException {

		// special case for advisor definitions
		if (info instanceof IAdvisorDefinition) {
			return null;
		}

		Class<?> aspectJAdviceClass = null;
		if (info.getType() == ADVICE_TYPE.AROUND) {
			aspectJAdviceClass = ClassUtils.loadClass("org.springframework.ide.eclipse.springframework.aop.aspectj.AspectJAroundAdvice");
		}
		else if (info.getType() == ADVICE_TYPE.AFTER) {
			aspectJAdviceClass = ClassUtils.loadClass("org.springframework.ide.eclipse.springframework.aop.aspectj.AspectJAfterAdvice");
		}
		else if (info.getType() == ADVICE_TYPE.AFTER_RETURNING) {
			aspectJAdviceClass = ClassUtils.loadClass("org.springframework.ide.eclipse.springframework.aop.aspectj.AspectJAfterReturningAdvice");
		}
		else if (info.getType() == ADVICE_TYPE.AFTER_THROWING) {
			aspectJAdviceClass = ClassUtils.loadClass("org.springframework.ide.eclipse.springframework.aop.aspectj.AspectJAfterThrowingAdvice");
		}
		else if (info.getType() == ADVICE_TYPE.BEFORE) {
			aspectJAdviceClass = ClassUtils.loadClass("org.springframework.ide.eclipse.springframework.aop.aspectj.AspectJAroundAdvice");
		}
		return aspectJAdviceClass;
	}
}
