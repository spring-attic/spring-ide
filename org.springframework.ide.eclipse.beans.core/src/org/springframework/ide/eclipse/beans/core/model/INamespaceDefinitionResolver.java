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
package org.springframework.ide.eclipse.beans.core.model;

import java.util.Set;

import org.springframework.beans.factory.xml.NamespaceHandlerResolver;

/**
 * Resolver to obtain references to {@link INamespaceDefinition}s. 
 * <p>
 * Clients should not cache any returned {@link INamespaceDefinition} instances as they may become
 * invalid if the hosting bundle goes down. 
 * @author Christian Dupuis
 * @since 2.2.5
 */
public interface INamespaceDefinitionResolver extends NamespaceHandlerResolver {

	/**
	 * Resolve a {@link INamespaceDefinition} for the given <code>namespaceUri</code>. May return
	 * <code>null</code> if no {@link INamespaceDefinition} is registered.
	 */
	INamespaceDefinition resolveNamespaceDefinition(String namespaceUri);
	
	/**
	 * Returns all registered {@link INamespaceDefinition}s.
	 */
	Set<INamespaceDefinition> getNamespaceDefinitions();
}
