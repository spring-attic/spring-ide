/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.autowire.internal.provider;

import java.lang.reflect.Member;

import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblemAttribute;

/**
 * Problem reporter SPI for {@link IInjectionMetadataProvider} to report errors during resolution of
 * {@link InjectionMetadata}.
 * <p>
 * @author Christian Dupuis
 * @since 2.2.7
 */
public interface IInjectionMetadataProviderProblemReporter {
	
	/**
	 * Report an error with the given <code>message</code> on the given {@link Member}. 
	 */
	void error(String message, Member member, ValidationProblemAttribute... attributes);

	/**
	 * Report an error with the given <code>message</code> on the given {@link DependencyDescriptor}. 
	 */
	void error(String message, DependencyDescriptor descriptor, ValidationProblemAttribute... attributes);
}
