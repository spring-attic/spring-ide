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
 * Constants for {@link IModelElement} types defined by the Spring model.
 * 
 * @author Torsten Juergeleit
 */
public interface ISpringModelElementTypes {

	/**
	 * Constant representing a Spring project.
	 * A model element with this type can be safely cast to
	 * {@link ISpringProject}.
	 */
	int PROJECT_TYPE = 2; // starts with 2 because 1 is reserved for the model
}
