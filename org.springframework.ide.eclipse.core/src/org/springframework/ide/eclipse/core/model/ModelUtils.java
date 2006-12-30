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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.model.xml.XmlSourceLocation;

/**
 * Some model-related helper methods.
 * 
 * @author Torsten Juergeleit
 */
public final class ModelUtils {

	/**
	 * Trys to adapt given element to <code>IModelElement</code>.
	 */
	public static Object adaptToModelElement(Object element) {
		if (!(element instanceof IModelElement)
				&& (element instanceof IAdaptable)) {
			Object modelElement = ((IAdaptable) element)
					.getAdapter(IModelElement.class);
			if (modelElement != null) {
				return modelElement;
			}
		}
		return element;
	}

	/**
	 * Returns <code>true</code> if a given {@link IModelElement element} is
	 * not defined within the specified
	 * {@link IResourceModelElement resource context}.
	 */
	public static boolean isExternal(IModelElement element,
			IModelElement context) {
		if (context != null) {
			if (context instanceof IResourceModelElement
					&& element instanceof IResourceModelElement) {
				IProject contextProject = ((IResourceModelElement) context)
						.getElementResource().getProject();
				IProject elementProject = ((IResourceModelElement) element)
						.getElementResource().getProject();
				if (!elementProject.equals(contextProject)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Creates an <code>IMarker</code> with the information (if any) provided
	 * by a given <code>IResourceModelElement</code> or
	 * <code>ISourceModelElement</code>.
	 */
	public static IMarker createMarker(final IModelElement element) {
		if (element instanceof IResourceModelElement) {
			final IResource resource = ((IResourceModelElement) element)
					.getElementResource();
			if (resource != null) {
				try {
					final IMarker[] markers = new IMarker[1];
					IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
						public void run(IProgressMonitor monitor)
								throws CoreException {
							IMarker marker = resource
									.createMarker(IMarker.TEXT);
							marker.setAttribute(IMarker.MESSAGE, toString());
							if (element instanceof ISourceModelElement) {
								marker.setAttribute(IMarker.LINE_NUMBER,
										((ISourceModelElement) element)
												.getElementStartLine());
								marker.setAttribute(IMarker.LOCATION, "line "
										+ ((ISourceModelElement) element)
												.getElementStartLine());
							}
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
		}
		return null;
	}

	/**
	 * Removes all Spring problem markers (including the inherited ones) from
	 * given <code>IModelElement</code>.
	 */
	public static void deleteProblemMarkers(IModelElement element) {
		if (element instanceof IResourceModelElement) {
			IResource resource = ((IResourceModelElement) element)
					.getElementResource();
			SpringCoreUtils.deleteProblemMarkers(resource);
		}
	}

	/**
	 * Returns the full path of the give element's resource or
	 * <code>null</code> if no <code>IResourceModelElement</code> or resource
	 * found.
	 */
	public static String getResourcePath(IModelElement element) {
		if (element instanceof IResourceModelElement) {
			IResource resource = ((IResourceModelElement) element)
					.getElementResource();
			if (resource != null) {
				return resource.getFullPath().toString();
			}
		}
		return null;
	}

	/**
	 * Returns the {@link XmlSourceLocation} of the give element or
	 * <code>null</code> if no {@link ISourceModelElement} or no
	 * {@link XmlSourceLocation} found.
	 */
	public static XmlSourceLocation getXmlSourceLocation(
			IModelElement element) {
		if (element instanceof ISourceModelElement) {
			IModelSourceLocation location = ((ISourceModelElement) element)
					.getElementSourceLocation();
			if (location instanceof XmlSourceLocation) {
				return (XmlSourceLocation) location;
			} else if (element.getElementParent() != null) {
				return getXmlSourceLocation(element.getElementParent());
			}
		}
		return null;
	}

	/**
	 * Returns the namespace URI for the given element or <code>null</code> if
	 * no {@link XmlSourceLocation} with a valid namespace URI found.
	 */
	public static String getNameSpaceURI(ISourceModelElement element) {
		XmlSourceLocation location = getXmlSourceLocation(element);
		return (location != null ? location.getNamespaceURI() : null);
	}

	/**
	 * Returns the node's local name for the given element or <code>null</code>
	 * if no {@link XmlSourceLocation} with a valid local name found.
	 */
	public static String getLocalName(ISourceModelElement element) {
		XmlSourceLocation location = getXmlSourceLocation(element);
		return (location != null ? location.getLocalName() : null);
	}

	/**
	 * Returns the node name for the given element or <code>null</code>
	 * if no {@link XmlSourceLocation} with a valid node name found.
	 */
	public static String getNodeName(ISourceModelElement element) {
		XmlSourceLocation location = getXmlSourceLocation(element);
		return (location != null ? location.getNodeName() : null);
	}
}
