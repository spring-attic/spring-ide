/*
 * Copyright 2002-2007 the original author or authors.
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
import org.springframework.util.ObjectUtils;

/**
 * Default implementation of the common protocol for all elements provided by
 * the model.
 * 
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
						if (name.equals(getElementName())) {

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
