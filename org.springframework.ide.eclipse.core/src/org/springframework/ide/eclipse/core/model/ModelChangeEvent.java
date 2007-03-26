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

import java.util.EventObject;

import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * An element changed event describes a change to an element of the model.
 * <p>
 * This class is not intended to be instantiated or subclassed by clients.
 * Instances of this class are automatically created by the model.
 * </p>
 * 
 * @author Torsten Juergeleit
 * @see IModelChangeListener
 */
public class ModelChangeEvent extends EventObject {

	private static final long serialVersionUID = 1L;

	public enum Type { ADDED, REMOVED, CHANGED };

	private Type type;

	/**
	 * Creates an new element change event.
	 *
	 * @param element  the changed model element
	 * @param type  the type of modification (ADDED, REMOVED, CHANGED) this
	 * 				event contains
	 */
	public ModelChangeEvent(IModelElement element, Type type) {
		super(element);
		this.type = type;
	}

	/**
	 * Returns the modified element.
	 */
	public IModelElement getElement() {
		return (IModelElement) getSource();
	}

	/**
	 * Returns the type of modification.
	 */
	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		StringBuffer text = new StringBuffer("Model element '");
		text.append(getElement().getElementName()).append("' (");
		text.append(getElement().getClass().getName()).append(") ");
		text.append(getType());
		return text.toString();
	}
}
