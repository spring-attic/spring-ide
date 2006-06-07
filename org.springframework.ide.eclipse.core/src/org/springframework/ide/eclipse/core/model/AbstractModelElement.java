/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.core.model;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Default implementation of the common protocol for all elements provided by
 * the model.
 * @author Torsten Juergeleit
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

	/**
	 * Checks for model element equality by comparing the element's unique IDs.
	 */
	public final boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof IModelElement) {
			return getElementID().equals(((IModelElement) obj).getElementID());
		}
		return false;
	}

	/**
	 * Returns the hash code of this element's ID.
	 */
	public final int hashCode() {
		return getElementID().hashCode();
	}

	public final void setElementParent(IModelElement parent) {
		this.parent = parent;
	}

	public final IModelElement getElementParent() {
		return parent;
	}
	public IModelElement[] getElementChildren() {
		return NO_CHILDREN;
	}

	public final void setElementName(String name) {
		this.name = name;
	}

	public final String getElementName() {
		return this.name;
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
			id.append(getElementName());
		} else {
			id.append(this.hashCode());
		}
		return id.toString();
	}

	public void accept(IModelElementVisitor visitor, IProgressMonitor monitor) {
		visitor.visit(this, monitor);
	}

	/**
	 * Returns the element for the given element ID.
	 *
	 * @param id the element's unique ID
	 */
	public final IModelElement getElement(String id) {
		int sepPos = id.indexOf(ID_SEPARATOR);
		if (sepPos > 0) {
			try {
				int type = Integer.valueOf(id.substring(0, sepPos)).intValue();
				if (type == getElementType()) {
					int delPos = id.indexOf(ID_DELIMITER);
					if (delPos > 0) {
						String name = id.substring(sepPos + 1, delPos);
						if (name.equals(getElementName())) {

							// Ask all children for the remaining part of the id
							id = id.substring(delPos + 1);
							IModelElement[] children = getElementChildren();
							for (int i = 0; i < children.length; i++) {
								IModelElement child = children[i];
								if (child instanceof AbstractModelElement) {
									IModelElement element =
													((AbstractModelElement)
														 child).getElement(id);
									if (element != null) {
										return element;
									}
								}
							}
						}
					} else {
						String name = id.substring(sepPos + 1);
						if (name.equals(getElementName())) {
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
}
