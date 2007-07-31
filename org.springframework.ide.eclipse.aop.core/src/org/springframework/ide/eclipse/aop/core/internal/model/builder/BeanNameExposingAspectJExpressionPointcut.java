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
package org.springframework.ide.eclipse.aop.core.internal.model.builder;

import org.springframework.aop.aspectj.AspectJExpressionPointcut;

/**
 * Subclass of Spring's {@link AspectJExpressionPointcut} that allows to
 * explicitly set the name of the current bean during pointcut matching.
 * <p>
 * This is needed to support the bean pointcut designator.
 * @author Christian Dupuis
 * @since 2.0.1
 */
public class BeanNameExposingAspectJExpressionPointcut extends
		AspectJExpressionPointcut {

	private static final long serialVersionUID = -1624034556102242297L;

	private String beanName;

	@Override
	protected String getCurrentProxiedBeanName() {
		return beanName;
	}

	public void setCurrentProxiedBeanName(String beanName) {
		this.beanName = beanName;
	}
}