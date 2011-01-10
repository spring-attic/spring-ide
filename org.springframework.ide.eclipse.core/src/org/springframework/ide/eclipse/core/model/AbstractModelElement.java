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

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.util.ObjectUtils;

/**
 * Default implementation of the common protocol for all elements provided by
 * the model.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public abstract class AbstractModelElement implements IModelElement {

	private IModelElement parent;
	private String name;

	protected AbstractModelElement(IModelElement parent, String name) {
		this.parent = parent;
		this.name = name;
	}

	public Object getAdapter(Class adapter) {
		return null;
	}

	public IModelElement getElementParent() {
		return parent;
	}

	public void setElementParent(IModelElement parent) {
		this.parent = parent;
	}

	public IModelElement[] getElementChildren() {
		return NO_CHILDREN;
	}

	public String getElementName() {
		return name;
	}

	public void setElementName(String name) {
		this.name = name;
	}

	public final String getElementID() {
		StringBuffer id = new StringBuffer();
		if (getElementParent() != null) {
			id.append(getElementParent().getElementID());
			id.append(ID_DELIMITER);
		}
		id.append(getElementType());
		id.append(ID_SEPARATOR);
		if (getElementName() != null) {
			id.append(getUniqueElementName());
		} else {
			id.append(super.hashCode());
		}
		return id.toString();
	}

	/**
	 * Overwrite this method if the element's name is not unique.
	 * <p>
	 * This method is called by {@link #getElementID}. The default
	 * implementation returns {@link #getElementName()}>.
	 * 
	 * @see #getElementID()
	 */
	protected String getUniqueElementName() {
		return getElementName();
	}

	/**
	 * Returns the element for the given element ID.
	 *
	 * @param id the element's unique ID
	 */
	public IModelElement getElement(String id) {
		int sepPos = id.indexOf(ID_SEPARATOR);
		if (sepPos > 0) {
			try {
				int type = Integer.valueOf(id.substring(0, sepPos)).intValue();
				if (type == getElementType()) {
					int delPos = id.indexOf(ID_DELIMITER);
					if (delPos > 0) {
						String name = id.substring(sepPos + 1, delPos);
						if (name.equals(getUniqueElementName())) {

							// Ask children for remaining part of id
							id = id.substring(delPos + 1);
							for (IModelElement child : getElementChildren()) {
								if (child instanceof AbstractModelElement) {
									IModelElement element =
											((AbstractModelElement) child)
													.getElement(id);
									if (element != null) {
										return element;
									}
								}
							}
						}
					} else {
						String name = id.substring(sepPos + 1);
						if (name.equals(getUniqueElementName())) {
							return this;
						}
					}
				}
			} catch (NumberFormatException e) {
				// ignore
			}
		}
		return null;
	}

	public void accept(IModelElementVisitor visitor, IProgressMonitor monitor) {

		// Visit only this element
		if (!monitor.isCanceled()) {
			visitor.visit(this, monitor);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AbstractModelElement)) {
			return false;
		}
		AbstractModelElement that = (AbstractModelElement) other;
		return ObjectUtils.nullSafeEquals(this.name, that.name); // ignore parent
	}

	@Override
	public int hashCode() {
		return ObjectUtils.nullSafeHashCode(name); // ignore parent
	}
}
