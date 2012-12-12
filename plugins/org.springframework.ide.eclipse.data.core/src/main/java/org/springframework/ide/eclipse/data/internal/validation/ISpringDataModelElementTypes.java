/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.data.internal.validation;

import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementTypes;

/**
 * Constants for {@link IModelElement} types defined by the Spring Data model.
 *
 * @author Tomasz Zarna
 */
public interface ISpringDataModelElementTypes extends IModelElementTypes {
	/**
	 * Constant representing a compilation unit. A model element with this type
	 * can be safely cast to {@link CompilationUnit}.
	 */
	int COMPILATION_UNIT_TYPE = 2;
	// starts with 2 because 1 is reserved for the model
}
