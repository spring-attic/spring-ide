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
package org.springframework.ide.eclipse.webflow.core.model;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public interface ICloneableModelElement<T extends IWebflowModelElement> {

	/**
	 * Clone model element.
	 * 
	 * @return the T
	 */
	T cloneModelElement();

	/**
	 * Apply clone values.
	 * 
	 * @param element the element
	 */
	void applyCloneValues(T element);

}
