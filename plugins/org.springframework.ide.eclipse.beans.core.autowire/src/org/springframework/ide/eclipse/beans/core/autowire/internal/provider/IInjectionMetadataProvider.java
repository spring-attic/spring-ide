/*******************************************************************************
 * Copyright (c) 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.autowire.internal.provider;

/**
 * Implementations of this interface are capable of locating {@link InjectionMetadata} instances on the given
 * {@link Class} instance.
 * @author Christian Dupuis
 * @since 2.2.7
 */
public interface IInjectionMetadataProvider {

	/**
	 * Return located {@link InjectionMetadata} meta data instances. 
	 */
	InjectionMetadata findAutowiringMetadata(Class<?> clazz);

}
