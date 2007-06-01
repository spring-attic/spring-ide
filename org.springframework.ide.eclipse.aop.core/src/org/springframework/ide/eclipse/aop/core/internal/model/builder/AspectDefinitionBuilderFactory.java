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

import java.util.HashSet;
import java.util.Set;

import org.springframework.ide.eclipse.aop.core.model.IAspectDefinition;
import org.springframework.ide.eclipse.aop.core.model.builder.IAspectDefinitionBuilder;

/**
 * Factory class that creates *fresh* instances of non-threadsafe
 * {@link IAspectDefinitionBuilder}.
 * <p>
 * TODO CD create extension point for contributing custom
 * {@link IAspectDefinition}
 * @author Christian Dupuis
 * @since 2.0
 */
public abstract class AspectDefinitionBuilderFactory {

	public static Set<IAspectDefinitionBuilder> getAspectDefinitionBuilder() {
		Set<IAspectDefinitionBuilder> builders = new HashSet<IAspectDefinitionBuilder>();
		builders.add(new XmlAspectDefinitionBuilder());
		builders.add(new AnnotationAspectDefinitionBuilder());
		return builders;
	}
}
