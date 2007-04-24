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
package org.springframework.ide.eclipse.beans.core.internal.model.process;

import org.springframework.beans.factory.parsing.ProblemReporter;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.ide.eclipse.beans.core.model.process.IBeansConfigPostProcessingContext;
import org.springframework.ide.eclipse.beans.core.model.process.IBeansConfigRegistrationSupport;

/**
 * Default implementation of {@link IBeansConfigPostProcessingContext} that just
 * delegates to given {@link BeanNameGenerator}, {@link ProblemReporter} and
 * {@link IBeansConfigRegistrationSupport}.
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeansConfigPostProcessingContext implements
		IBeansConfigPostProcessingContext {

	private final BeanNameGenerator beanNameGenerator;

	private final ProblemReporter problemReporter;

	private final IBeansConfigRegistrationSupport beansConfigRegistrationSupport;

	public BeansConfigPostProcessingContext(
			final BeanNameGenerator beanNameGenerator,
			final ProblemReporter problemReporter,
			final IBeansConfigRegistrationSupport beansConfigRegistrationSupport) {
		this.beanNameGenerator = beanNameGenerator;
		this.problemReporter = problemReporter;
		this.beansConfigRegistrationSupport = beansConfigRegistrationSupport;
	}

	public BeanNameGenerator getBeanNameGenerator() {
		return this.beanNameGenerator;
	}

	public IBeansConfigRegistrationSupport getBeansConfigRegistrySupport() {
		return this.beansConfigRegistrationSupport;
	}

	public ProblemReporter getProblemReporter() {
		return this.problemReporter;
	}
}
