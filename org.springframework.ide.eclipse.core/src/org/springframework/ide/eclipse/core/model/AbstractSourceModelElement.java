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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.core.io.ExternalFile;
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
	
	public boolean isExternal() {
		return getElementResource() instanceof ExternalFile;
	}

	public final void setElementSourceLocation(
			IModelSourceLocation location) {
		this.location = location;
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
	@Override
	public Object getAdapter(Class adapter) {
		if (adapter == IMarker.class) {
			return ModelUtils.createMarker(this);
		}
		return super.getAdapter(adapter);
	}

	@Override
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

	@Override
	public int hashCode() {
		int hashCode = ObjectUtils.nullSafeHashCode(location);
		return getElementType() * hashCode + super.hashCode();
	}

	@Override
	public String toString() {
		StringBuffer text = new StringBuffer(getElementName());
		text.append(" (");
		text.append(getElementStartLine());
		text.append(')');
		return text.toString();
	}

	/**
	 * Overwrite this method if the element's name is not unique.
	 * <p>
	 * This method is called by {@link #getElementID()}. The default
	 * implementation returns
	 * <code>getElementName() + ":" + getElementStartLine()</code>.
	 * 
	 * @see #getElementID()
	 */
	@Override
	protected String getUniqueElementName() {
		return getElementName() + ID_SEPARATOR + getElementStartLine();
	}
}
