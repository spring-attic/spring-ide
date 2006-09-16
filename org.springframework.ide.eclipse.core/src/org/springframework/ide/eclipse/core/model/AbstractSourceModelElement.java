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
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.core.SpringCore;

/**
 * Default implementation of the common protocol for all model elements related
 * to source code.
 * 
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
			return createMarker();
		}
		return super.getAdapter(adapter);
	}

	private IMarker createMarker() {
		final IResource resource = getElementResource();
		if (resource != null) {
			try {
				final IMarker[] markers = new IMarker[1];
				IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor)
														 throws CoreException {
						IMarker marker = resource.createMarker(IMarker.TEXT);
						marker.setAttribute(IMarker.LINE_NUMBER, startLine);
						marker.setAttribute(IMarker.LOCATION, "line " +
											startLine);
						marker.setAttribute(IMarker.MESSAGE, toString());
						markers[0] = marker;
					}
				};
				resource.getWorkspace().run(runnable, null,
												IWorkspace.AVOID_UPDATE, null);
				return markers[0];
			} catch (CoreException e) {
				SpringCore.log(e);
			}
		}
		return null;
	}
}
