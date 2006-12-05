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

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.springframework.beans.BeanMetadataElement;
import org.springframework.ide.eclipse.core.io.xml.XmlSource;

/**
 * Default implementation of the common protocol for all model elements related
 * to source code.
 * @author Torsten Juergeleit
 */
public abstract class AbstractSourceModelElement extends
		AbstractResourceModelElement implements ISourceModelElement {

	private int startLine;
	private int endLine;

	protected AbstractSourceModelElement(IModelElement parent, String name) {
		super(parent, name);
		this.startLine = -1;
		this.endLine = -1;
	}

	public final void setElementStartLine(int line) {
		this.startLine = line;
	}

	public final int getElementStartLine() {
		return startLine;
	}

    public final void setElementEndLine(int endLine) {
       	this.endLine = endLine;
    }

	public final int getElementEndLine() {
	    return endLine;
	}

	public IResource getElementResource() {
		IResourceModelElement element = getElementSource();
		if (element != null) {
			return element.getElementResource();
		}
		return null;
	}

	public boolean isElementArchived() {
		IResourceModelElement element = getElementSource();
		if (element != null) {
			return element.isElementArchived();
		}
		return false;
	}

	/**
	 * Traverses this model element's parent chain until the first
	 * non-<code>ISourceModelElement</code> and returns the corresponding
	 * model element.
	 */
	public IResourceModelElement getElementSource() {
		for (IModelElement parent = getElementParent(); parent != null;
				parent = parent.getElementParent()) {
			if (!(parent instanceof ISourceModelElement)) {

				// It must be an IResourceModelElement
				return (IResourceModelElement) parent;
			}
		}
		return null;
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

	protected void setSourceRange(BeanMetadataElement metadata) {
		if (metadata.getSource() instanceof XmlSource) {
			XmlSource source = (XmlSource) metadata
					.getSource();
			setElementStartLine(source.getStartLine());
			setElementEndLine(source.getEndLine());
		}
	}

	/**
	 * Overwrite this method if the element's name is not unique.
	 * <p>
	 * This method is called by <code>getElementID()</code>. The default
	 * implementation returns to
	 * <code>getElementName() + "-" + getElementStartLine()</code>.
	 * 
	 * @see #getElementID()
	 */
	protected String getUniqueElementName() {
		return getElementName() + ID_SEPARATOR + getElementStartLine();
	}
}
