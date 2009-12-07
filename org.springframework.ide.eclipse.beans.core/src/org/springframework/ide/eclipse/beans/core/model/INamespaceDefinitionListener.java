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

/**
 * Implementations of this interface will receive notifications when {@link INamespaceDefinition} are registered and
 * unregistered.
 * @author Christian Dupuis
 * @since 2.3.0
 */
public interface INamespaceDefinitionListener {

	/**
	 * Event notifying about a processed registration of a {@link INamespaceDefinition}. 
	 */
	void onNamespaceDefinitionRegistered(INamespaceDefinition namespaceDefinition);

	/**
	 * Event notifying about a processed unregistration of a {@link INamespaceDefinition}. 
	 */
	void onNamespaceDefinitionUnregistered(INamespaceDefinition namespaceDefinition);

}
