/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.namespaces;

import org.springframework.beans.factory.parsing.ComponentDefinition;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * This interface defines a protocol used by the extension point
 * "org.springframework.ide.eclipse.beans.core.namespaces" to convert a
 * {@link ComponentDefinition} into a {@link ISourceModelElement}.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public interface IModelElementProvider {

	/**
	 * Returns the corresponding {@link ISourceModelElement} for the given
	 * {@link ComponentDefinition}.
	 * 
	 * @param config  the config the requested model element belongs to
	 * @param definition  the Spring component the model element is created from
	 */
	ISourceModelElement getElement(IBeansConfig config,
			ComponentDefinition definition);
}
