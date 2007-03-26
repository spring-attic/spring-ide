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
package org.springframework.ide.eclipse.webflow.core.model;

import java.util.EventObject;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowModelChangedEvent extends EventObject {

	/**
	 * The Constant serialVersionUID.
	 */
	private static final long serialVersionUID = -8568748940583679520L;

	/**
	 * The Constant ADDED.
	 */
	public static final int ADDED = 1;

	/**
	 * The Constant REMOVED.
	 */
	public static final int REMOVED = 2;

	/**
	 * The Constant CHANGED.
	 */
	public static final int CHANGED = 3;

	/**
	 * The type.
	 */
	private int type;

	/**
	 * The Constructor.
	 * 
	 * @param element the element
	 * @param type the type
	 */
	public WebflowModelChangedEvent(IWebflowModelElement element, int type) {
		super(element);
		this.type = type;
	}

	/**
	 * Gets the element.
	 * 
	 * @return the element
	 */
	public IWebflowModelElement getElement() {
		return (IWebflowModelElement) getSource();
	}

	/**
	 * Gets the type.
	 * 
	 * @return the type
	 */
	public int getType() {
		return type;
	}
}
