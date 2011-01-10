/*******************************************************************************
 * Copyright (c) 2009, 2011 Spring IDE Developers
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
 * Package-level no op implementation of {@link IInjectionMetadataProviderProblemReporter}.
 * @author Christian Dupuis
 * @author Terry Hon
 * @since 2.2.7
 */
class PassThroughProblemReporter implements IInjectionMetadataProviderProblemReporter {

	public void error(String message, Member member, ValidationProblemAttribute... attributes) {
	}

	public void error(String message, DependencyDescriptor descriptor, ValidationProblemAttribute... attributes) {
	}

}
