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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.springframework.util.ObjectUtils;

/**
 * Default implementation of the common protocol for all {@link IModelElement}s
 * related to source code.
 * 
 * @author Torsten Juergeleit
 */
public abstract class AbstractSourceModelElement extends
		AbstractResourceModelElement implements ISourceModelElement {

	private IModelSourceLocation location;

	protected AbstractSourceModelElement(IModelElement parent, String name,
			IModelSourceLocation location) {
		super(parent, name);
		this.location = location;
	}

	/**
	 * Traverses this model element's parent chain until the first
	 * non-{@link ISourceModelElement} and returns this
	 * {@link IResourceModelElement}.
	 */
	public IResourceModelElement getElementSourceElement() {
		for (IModelElement parent = getElementParent(); parent != null;
				parent = parent.getElementParent()) {
			if (!(parent instanceof ISourceModelElement)) {

				// It must be an IResourceModelElement
				return (IResourceModelElement) parent;
			}
		}
		return null;
	}

	public IResource getElementResource() {
		IResourceModelElement element = getElementSourceElement();
		if (element != null) {
			return element.getElementResource();
		}
		return null;
	}

	public boolean isElementArchived() {
		IResourceModelElement element = getElementSourceElement();
		if (element != null) {
			return element.isElementArchived();
		}
		return false;
	}

	public final IModelSourceLocation getElementSourceLocation() {
		if (location != null) {
			return location;
		}

		// Traverses this model element's parent chain until the first source
		// location is found
		for (IModelElement parent = getElementParent(); parent != null;
				parent = parent.getElementParent()) {
			if (parent instanceof ISourceModelElement) {
				IModelSourceLocation location = ((ISourceModelElement) parent)
						.getElementSourceLocation();
				if (location != null) {
					return location;
				}
			} else {
				break;
			}
		}
		return null;
	}

	public int getElementStartLine() {
		IModelSourceLocation location = getElementSourceLocation();
		return (location != null ? location.getStartLine() : -1);
	}

	public int getElementEndLine() {
		IModelSourceLocation location = getElementSourceLocation();
		return (location != null ? location.getEndLine() : -1);
	}

	/**
	 * Returns an adapter for <code>IMarker.class</code>.
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IMarker.class) {
			return ModelUtils.createMarker(this);
		}
		return super.getAdapter(adapter);
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AbstractSourceModelElement)) {
			return false;
		}
		AbstractSourceModelElement that = (AbstractSourceModelElement) other;
		if (!ObjectUtils.nullSafeEquals(this.location, that.location))
			return false;
		return super.equals(other);
	}

	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(location);
		return getElementType() * hashCode + super.hashCode();
	}

	public String toString() {
		StringBuffer text = new StringBuffer(getElementName());
		text.append(" (");
		text.append(getElementSourceLocation().getStartLine());
		text.append(')');
		return text.toString();
	}

	/**
	 * Overwrite this method if the element's name is not unique.
	 * <p>
	 * This method is called by <code>getElementID()</code>. The default
	 * implementation returns to
	 * <code>getElementName() + "-" + location.getStartLine()</code>.
	 * 
	 * @see #getElementID()
	 */
	protected String getUniqueElementName() {
		return (location != null ? getElementName() : getElementName()
				+ ID_SEPARATOR + location.getStartLine());
	}
}
