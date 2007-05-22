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
package org.springframework.ide.eclipse.webflow.core.internal.model;

import org.springframework.ide.eclipse.webflow.core.model.IPersistableWebflowModelElement;

public abstract class AbstractPersistableWebflowModelElement implements
		IPersistableWebflowModelElement {

	/** Character used for delimiting nodes within an element's unique id */
	char ID_DELIMITER = '|';

	/**
	 * Character used separate an element's type and name within an element's
	 * unique id
	 */
	char ID_SEPARATOR = ':';

	public final String getElementID() {
		StringBuffer id = new StringBuffer();
		if (getPersistableElementParent() != null) {
			id.append(getPersistableElementParent().getElementID());
			id.append(ID_DELIMITER);
		}
		id.append(getElementType());
		id.append(ID_SEPARATOR);
		if (getElementName() != null) {
			id.append(getElementName());
		}
		else {
			id.append(super.hashCode());
		}
		return id.toString();
	}

	/**
	 * Returns the element for the given element ID.
	 * 
	 * @param id the element's unique ID
	 */
	public IPersistableWebflowModelElement getElement(String id) {
		int sepPos = id.indexOf(ID_SEPARATOR);
		if (sepPos > 0) {
			try {
				int type = Integer.valueOf(id.substring(0, sepPos)).intValue();
				if (type == getElementType()) {
					int delPos = id.indexOf(ID_DELIMITER);
					if (delPos > 0) {
						String name = id.substring(sepPos + 1, delPos);
						if (name.equals(getElementName())) {

							// Ask children for remaining part of id
							id = id.substring(delPos + 1);
							for (IPersistableWebflowModelElement child : getElementChildren()) {
								if (child instanceof AbstractPersistableWebflowModelElement) {
									IPersistableWebflowModelElement element = 
										((AbstractPersistableWebflowModelElement) child)
											.getElement(id);
									if (element != null) {
										return element;
									}
								}
							}
						}
					}
					else {
						String name = id.substring(sepPos + 1);
						if (name.equals(getElementName())) {
							return this;
						}
					}
				}
			}
			catch (NumberFormatException e) {
				// ignore
			}
		}
		return null;
	}
}
