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
package org.springframework.ide.eclipse.core.model;

/**
 * Constants for {@link IModelElement} types defined by the core model.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public interface IModelElementTypes {

	/**
	 * Constant representing a model (workspace level object). A
	 * {@link IModelElement model element} with this type can be safely cast to
	 * {@link IModel}.
	 */
	int MODEL_TYPE = 1;
}
