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
package org.springframework.ide.eclipse.beans.mylyn.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.mylyn.context.core.AbstractContextStructureBridge;
import org.eclipse.mylyn.context.core.ContextCorePlugin;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.resources.ui.ResourceStructureBridge;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISpringProject;

/**
 * {@link AbstractContextStructureBridge} extension that integrates the
 * {@link IBeansModel} with Mylyn.
 * @author Christian Dupuis
 * @since 2.0
 */
public class BeansContextStructureBridge extends AbstractContextStructureBridge {

	public static final String CONTENT_TYPE = "spring/beans";

	@Override
	public boolean acceptsObject(Object object) {
		return (object instanceof IModelElement
				|| (object instanceof IResource && BeansCoreUtils
						.isBeansConfig((IResource) object)));
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
				IInteractionElement node = ContextCorePlugin.getContextManager()
						.getElement(getHandleIdentifier(child));
				if (node != null && node.getInterest().isInteresting()) {
					return false;
				}
			}

			if (modelElement instanceof ISpringProject) {
				IBeansProject beansProject = BeansModelUtils
						.getProject(modelElement);
				return canFilter(beansProject);
			}

			IInteractionElement node = ContextCorePlugin.getContextManager()
					.getElement(getHandleIdentifier(obj));
			if (node != null && node.getInterest().isInteresting()) {
				return false;
			}
		}
		else if ((obj instanceof IResource && BeansCoreUtils
				.isBeansConfig((IResource) obj))) {
			return canFilter(BeansModelUtils.getResourceModelElement(obj));
		}
		
		AbstractContextStructureBridge parentBridge = ContextCorePlugin
				.getDefault().getStructureBridge(parentContentType);
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
			IModelElement[] children = ((IModelElement) obj)
					.getElementChildren();
			for (IModelElement child : children) {
				childHandles.add(child.getElementID());
			}
			return childHandles;
		}
		return null;
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
			return ResourceStructureBridge.CONTENT_TYPE;
		}
	}

	@Override
	public String getHandleIdentifier(Object obj) {
		if (obj instanceof IModelElement) {
			return ((IModelElement) obj).getElementID();
		}
		else if ((obj instanceof IResource && BeansCoreUtils
				.isBeansConfig((IResource) obj))) {
			return BeansModelUtils.getResourceModelElement(obj).getElementID();
		}
		return null;
	}

	@Override
	public String getName(Object obj) {
		if (obj instanceof IModelElement) {
			return ((IModelElement) obj).getElementName();
		}
		else if ((obj instanceof IResource && BeansCoreUtils
				.isBeansConfig((IResource) obj))) {
			return BeansModelUtils.getResourceModelElement(obj)
					.getElementName();
		}
		return null;
	}

	@Override
	public Object getObjectForHandle(String handle) {
		Object obj = null;
		if (handle != null) {
			obj = BeansCorePlugin.getModel().getElement(handle);
			if (obj != null) {
				return obj;
			}
			obj = SpringCore.getModel().getElement(handle);
			if (obj != null) {
				return obj;
			}
		}
		AbstractContextStructureBridge parentBridge = ContextCorePlugin
				.getDefault().getStructureBridge(parentContentType);
		if (parentBridge != null) {
			obj = parentBridge.getObjectForHandle(handle);
		}

		return obj;
	}

	@Override
	public String getParentHandle(String handle) {
		Object obj = getObjectForHandle(handle);
		if (obj instanceof IBeansProject) {
			return SpringCore.getModel().getProject(
					((IBeansProject) obj).getProject()).getElementID();
		}
		else if (obj instanceof ISpringProject) {
			AbstractContextStructureBridge parentBridge = ContextCorePlugin
					.getDefault().getStructureBridge(parentContentType);
			if (parentBridge != null
					&& parentBridge instanceof ResourceStructureBridge) {
				return parentBridge.getHandleIdentifier(((ISpringProject) obj)
						.getProject());
			}
		}
		else if (obj != null && obj instanceof IModelElement) {
			IModelElement parent = ((IModelElement) obj).getElementParent();
			if (parent != null) {
				return parent.getElementID();
			}
		}
		else {
			AbstractContextStructureBridge parentBridge = ContextCorePlugin
					.getDefault().getStructureBridge(parentContentType);

			if (parentBridge != null
					&& parentBridge instanceof ResourceStructureBridge) {
				return parentBridge.getParentHandle(handle);
			}
		}
		return null;
	}

	@Override
	public boolean isDocument(String handle) {
		Object obj = getObjectForHandle(handle);
		if (obj != null && obj instanceof IBeansConfig) {
			return true;
		}
		return false;
	}

	@Override
	public String getHandleForOffsetInObject(Object arg0, int arg1) {
		return null;
	}

}
