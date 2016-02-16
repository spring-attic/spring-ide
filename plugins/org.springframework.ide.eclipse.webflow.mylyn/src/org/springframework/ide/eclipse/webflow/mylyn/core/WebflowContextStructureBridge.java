/*******************************************************************************
 * Copyright (c) 2007, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.mylyn.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.mylyn.context.core.AbstractContextStructureBridge;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.resources.ui.ResourceStructureBridge;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModel;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;

/**
 * {@link AbstractContextStructureBridge} extension that integrates the {@link IWebflowModel} with
 * Mylyn.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class WebflowContextStructureBridge extends AbstractContextStructureBridge {

	public static final String CONTENT_TYPE = "spring/webflow";

	@Override
	public boolean acceptsObject(Object object) {
		return (object instanceof IWebflowModelElement || object instanceof IWebflowProject
				|| object instanceof ISpringProject || (object instanceof IResource && WebflowModelUtils
				.isWebflowConfig((IResource) object)));
	}

	@Override
	public boolean canBeLandmark(String handle) {
		return true;
	}

	@Override
	public boolean canFilter(Object obj) {
		if (obj instanceof IModelElement) {
			IModelElement modelElement = (IModelElement) obj;

			IModelElement[] children = modelElement.getElementChildren();
			for (IModelElement child : children) {
				IInteractionElement node = ContextCore.getContextManager().getElement(
						getHandleIdentifier(child));
				if (node != null && node.getInterest().isInteresting()) {
					return false;
				}
			}

			if (modelElement instanceof ISpringProject) {
				IProject project = ((ISpringProject) modelElement).getProject();
				IWebflowProject webflowProject = Activator.getModel().getProject(project);
				return canFilter(webflowProject);
			}

			IInteractionElement node = ContextCore.getContextManager().getElement(
					getHandleIdentifier(obj));
			if (node != null && node.getInterest().isInteresting()) {
				return false;
			}
		}
		else if ((obj instanceof IFile && WebflowModelUtils.isWebflowConfig((IResource) obj))) {
			return canFilter(WebflowModelUtils.getWebflowConfig((IFile) obj));
		}

		AbstractContextStructureBridge parentBridge = ContextCore
				.getStructureBridge(parentContentType);
		if (parentBridge != null && !parentBridge.canFilter(obj)) {
			return false;
		}

		return true;
	}

	@Override
	public List<String> getChildHandles(String handle) {
		Object obj = getObjectForHandle(handle);
		if (obj != null && obj instanceof IModelElement) {
			List<String> childHandles = new ArrayList<String>();
			IModelElement[] children = ((IModelElement) obj).getElementChildren();
			for (IModelElement child : children) {
				childHandles.add(child.getElementID());
			}
			return childHandles;
		}
		return Collections.emptyList();
	}

	@Override
	public String getContentType() {
		return CONTENT_TYPE;
	}

	@Override
	public String getContentType(String handle) {
		Object obj = getObjectForHandle(handle);
		if (obj instanceof IModelElement) {
			return CONTENT_TYPE;
		}
		else {
			return ContextCore.CONTENT_TYPE_RESOURCE;
		}
	}

	@Override
	public String getHandleIdentifier(Object obj) {

		if (obj instanceof IModelElement) {
			return ((IModelElement) obj).getElementID();
		}
		else if (obj instanceof IFile && WebflowModelUtils.isWebflowConfig((IFile) obj)) {
			return WebflowModelUtils.getWebflowConfig((IFile) obj).getElementID();
		}
		return null;
	}

	@Override
	public String getLabel(Object obj) {
		if (obj instanceof IModelElement) {
			return ((IModelElement) obj).getElementName();
		}
		else if (obj instanceof IFile && WebflowModelUtils.isWebflowConfig((IFile) obj)) {
			return WebflowModelUtils.getWebflowConfig((IFile) obj).getElementName();
		}
		return null;
	}

	@Override
	public Object getObjectForHandle(String handle) {
		Object obj = null;
		if (handle != null) {
			obj = Activator.getModel().getElement(handle);
			if (obj != null) {
				return obj;
			}
			obj = SpringCore.getModel().getElement(handle);
			if (obj != null) {
				return obj;
			}
		}
		AbstractContextStructureBridge parentBridge = ContextCore
				.getStructureBridge(parentContentType);
		if (parentBridge != null) {
			obj = parentBridge.getObjectForHandle(handle);
		}

		return null;
	}

	@Override
	public String getParentHandle(String handle) {
		Object obj = getObjectForHandle(handle);
		if (obj instanceof IWebflowProject) {
			ISpringProject project = SpringCore.getModel().getProject(
					((IWebflowProject) obj).getProject());
			if (project != null) {
				return project.getElementID();
			}
		}
		else if (obj instanceof ISpringProject) {
			AbstractContextStructureBridge parentBridge = ContextCore
					.getStructureBridge(parentContentType);
			if (parentBridge != null && parentBridge instanceof ResourceStructureBridge) {
				return parentBridge.getHandleIdentifier(((ISpringProject) obj).getProject());
			}
		}
		else if (obj != null && obj instanceof IModelElement) {
			IModelElement parent = ((IModelElement) obj).getElementParent();
			if (parent != null) {
				return parent.getElementID();
			}
		}
		else {
			AbstractContextStructureBridge parentBridge = ContextCore
					.getStructureBridge(parentContentType);

			if (parentBridge != null && parentBridge instanceof ResourceStructureBridge) {
				return parentBridge.getParentHandle(handle);
			}
		}
		return null;
	}

	@Override
	public boolean isDocument(String handle) {
		Object obj = getObjectForHandle(handle);
		if (obj != null && obj instanceof IWebflowConfig) {
			return true;
		}
		return false;
	}

	@Override
	public String getHandleForOffsetInObject(Object arg0, int arg1) {
		return null;
	}

}
