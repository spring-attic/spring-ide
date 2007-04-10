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
 * The <code>IModel</code> manages instances of <code>IModelElement</code>s.
 * <code>IModelChangedListener</code>s register with the <code>IModel</code>,
 * and receive <code>ModelChangedEvent</code>s for all changes.
 * 
 * @author Torsten Juergeleit
 */
public interface IModel extends IModelElement {

	/**
	 * Returns the element for the given element ID.
	 *
	 * @param id the element's unique ID
	 */
	IModelElement getElement(String id);

	void addChangeListener(IModelChangeListener listener);

	void removeChangeListener(IModelChangeListener listener);
}
