/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.model.metadata;

import org.springframework.ide.eclipse.core.model.IModelSourceLocation;

/**
 * Base interface to be implemented by different types of Spring meta data.
 * <p>
 * This interface is totally independent from the type of meta data.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public interface IBeanMetadata {

	/**
	 * Returns the globally unique handle that this meta data is coming from.
	 */
	String getHandleIdentifier();

	/**
	 * The key of this meta data.
	 */
	String getKey();

	/**
	 * The value of this meta data.
	 */
	Object getValue();

	/**
	 * Return a String-based representation of the value of this meta data.
	 */
	String getValueAsText();
	
	/**
	 * The location that this meta data wants to navigate to. 
	 */
	IModelSourceLocation getElementSourceLocation();

}
