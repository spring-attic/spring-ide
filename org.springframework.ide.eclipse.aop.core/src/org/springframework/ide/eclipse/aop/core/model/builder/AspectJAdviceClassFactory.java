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
package org.springframework.ide.eclipse.aop.core.model.builder;

import org.springframework.aop.aspectj.AbstractAspectJAdvice;
import org.springframework.aop.aspectj.AspectJAfterAdvice;
import org.springframework.aop.aspectj.AspectJAfterReturningAdvice;
import org.springframework.aop.aspectj.AspectJAfterThrowingAdvice;
import org.springframework.aop.aspectj.AspectJAroundAdvice;
import org.springframework.aop.aspectj.AspectJMethodBeforeAdvice;
import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.IAopReference.ADVICE_TYPES;

/**
 * Factory class that creates {@link Class} instances for
 * {@link AbstractAspectJAdvice} subclass depending of {@link IAspectDefinition}.
 * @author Christian Dupuis
 * @since 2.0
 */
public class AspectJAdviceClassFactory {

	public static Class<?> getAspectJAdviceClass(IAspectDefinition info)
			throws ClassNotFoundException {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Class<?> aspectJAdviceClass = null;
		if (info.getType() == ADVICE_TYPES.AROUND) {
			aspectJAdviceClass = loader.loadClass(AspectJAroundAdvice.class
					.getName());
		}
		else if (info.getType() == ADVICE_TYPES.AFTER) {
			aspectJAdviceClass = loader.loadClass(AspectJAfterAdvice.class
					.getName());
		}
		else if (info.getType() == ADVICE_TYPES.AFTER_RETURNING) {
			aspectJAdviceClass = loader
					.loadClass(AspectJAfterReturningAdvice.class.getName());
		}
		else if (info.getType() == ADVICE_TYPES.AFTER_THROWING) {
			aspectJAdviceClass = loader
					.loadClass(AspectJAfterThrowingAdvice.class.getName());
		}
		else if (info.getType() == ADVICE_TYPES.BEFORE) {
			aspectJAdviceClass = loader
					.loadClass(AspectJMethodBeforeAdvice.class.getName());
		}
		return aspectJAdviceClass;
	}
}
