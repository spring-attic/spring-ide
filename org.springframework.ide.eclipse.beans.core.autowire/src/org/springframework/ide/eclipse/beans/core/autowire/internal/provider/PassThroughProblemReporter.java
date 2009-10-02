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

/**
 * Package-level no op implementation of {@link IInjectionMetadataProviderProblemReporter}.
 * @author Christian Dupuis
 * @since 2.2.7
 */
class PassThroughProblemReporter implements IInjectionMetadataProviderProblemReporter {

	@Override
	public void error(String message, Member member) {
	}

	@Override
	public void error(String message, DependencyDescriptor descriptor) {
	}

}
