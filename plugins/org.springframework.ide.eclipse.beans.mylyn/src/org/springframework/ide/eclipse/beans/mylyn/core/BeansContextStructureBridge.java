/*******************************************************************************
 * Copyright (c) 2007, 2010 Spring IDE Developers
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
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.mylyn.context.core.AbstractContextStructureBridge;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.resources.ui.ResourceStructureBridge;
import org.eclipse.swt.widgets.Display;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.metadata.model.IBeanMetadata;
import org.springframework.ide.eclipse.beans.core.metadata.model.IClassMetadata;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.model.metadata.BeanMetadataNode;
import org.springframework.ide.eclipse.beans.ui.model.metadata.BeanMetadataReference;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.model.ILazyInitializedModelElement;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISpringProject;

/**
 * {@link AbstractContextStructureBridge} extension that integrates the {@link IBeansModel} with Mylyn.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class BeansContextStructureBridge extends AbstractContextStructureBridge {

	public static final String CONTENT_TYPE = "spring/beans";

	@Override
	public boolean acceptsObject(Object object) {
		return object instanceof IModelElement
				// Make this bridge aware of meta data contributions
				|| object instanceof BeanMetadataNode || object instanceof BeanMetadataReference
				|| object instanceof IBeanMetadata || (object instanceof IFile && isBeansConfig((IFile) object));
	}

	@Override
	public boolean canBeLandmark(String handle) {
		return true;
	}

	@Override
	public boolean canFilter(Object obj) {
		if (obj instanceof IModelElement) {
			IModelElement modelElement = (IModelElement) obj;

			// not necessary, context propagation will make all parent elements interesting
			// IModelElement[] children = modelElement.getElementChildren();
			// for (IModelElement child : children) {
			// IInteractionElement node = ContextCore.getContextManager().getElement(
			// getHandleIdentifier(child));
			// if (node != null && node.getInterest().isInteresting()) {
			// return false;
			// }
			// if (!canFilter(child)) {
			// return false;
			// }
			// }

			if (modelElement instanceof ISpringProject) {
				IBeansProject beansProject = BeansModelUtils.getProject(modelElement);
				return canFilter(beansProject);
			}
			if (obj instanceof IBeansConfig) {
				return true;
			}

			IInteractionElement node = ContextCore.getContextManager().getElement(getHandleIdentifier(obj));
			if (node != null && node.getInterest().isInteresting()) {
				return false;
			}
		}
		else if ((obj instanceof IFile && isBeansConfig((IFile) obj))) {
			IInteractionElement node = ContextCore.getContextManager().getElement(getHandleIdentifier(obj));
			if (node != null && node.getInterest().isInteresting()) {
				return false;
			}
		}
		// Support for meta data
		else if (obj instanceof BeanMetadataReference) {
			for (Object child : ((BeanMetadataReference) obj).getChildren()) {
				if (!canFilter(child)) {
					return false;
				}
			}
		}
		else if (obj instanceof BeanMetadataNode) {
			IInteractionElement node = ContextCore.getContextManager().getElement(
					((BeanMetadataNode) obj).getHandleIdentifier());
			if (node != null && node.getInterest().isInteresting()) {
				return false;
			}
			for (Object child : ((BeanMetadataNode) obj).getChildren()) {
				if (!canFilter(child)) {
					return false;
				}
			}
		}
		else if (obj instanceof IClassMetadata) {
			IInteractionElement node = ContextCore.getContextManager().getElement(
					((IBeanMetadata) obj).getHandleIdentifier());
			if (node != null && node.getInterest().isInteresting()) {
				return false;
			}
			for (Object child : ((IClassMetadata) obj).getMethodMetaData()) {
				if (!canFilter(child)) {
					return false;
				}
			}
		}
		else if (obj instanceof IBeanMetadata) {
			IInteractionElement node = ContextCore.getContextManager().getElement(
					((IBeanMetadata) obj).getHandleIdentifier());
			if (node != null && node.getInterest().isInteresting()) {
				return false;
			}
		}

		AbstractContextStructureBridge parentBridge = ContextCore.getStructureBridge(parentContentType);
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
	public String getHandleForOffsetInObject(Object arg0, int arg1) {
		return null;
	}

	@Override
	public String getHandleIdentifier(Object obj) {
		if (obj instanceof IModelElement) {
			return ((IModelElement) obj).getElementID();
		}
		else if (obj instanceof IBeanMetadata) {
			return ((IBeanMetadata) obj).getHandleIdentifier();
		}
		else if ((obj instanceof IFile && isBeansConfig((IFile) obj))) {
			return BeansModelUtils.getResourceModelElement(obj).getElementID();
		}
		return null;
	}

	@Override
	public String getLabel(Object obj) {
		if (obj instanceof IModelElement) {
			return ((IModelElement) obj).getElementName();
		}
		else if ((obj instanceof IFile && isBeansConfig((IFile) obj))) {
			return BeansModelUtils.getResourceModelElement(obj).getElementName();
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
		AbstractContextStructureBridge parentBridge = ContextCore.getStructureBridge(parentContentType);
		if (parentBridge != null) {
			obj = parentBridge.getObjectForHandle(handle);
		}

		return obj;
	}

	@Override
	public String getParentHandle(String handle) {
		Object obj = getObjectForHandle(handle);
		if (obj instanceof IBeansProject) {
			return SpringCore.getModel().getProject(((IBeansProject) obj).getProject()).getElementID();
		}
		else if (obj instanceof ISpringProject) {
			AbstractContextStructureBridge parentBridge = ContextCore.getStructureBridge(parentContentType);
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
			AbstractContextStructureBridge parentBridge = ContextCore.getStructureBridge(parentContentType);

			if (parentBridge != null && parentBridge instanceof ResourceStructureBridge) {
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

	public static boolean isBeansConfig(IFile configFile) {
		return isBeansConfig(configFile, true);
	}

	private static IBeansConfig getBeansConfig(IFile configFile, IBeansProject project, boolean includeImports) {
		for (String suffix : project.getConfigSuffixes()) {
			if (configFile.getName().endsWith(suffix)) {
				if (includeImports && Display.getCurrent() != null && project instanceof ILazyInitializedModelElement
						&& !((ILazyInitializedModelElement) project).isInitialized()) {
					// skip: it's too expensive to load the model on the UI thread
					includeImports = false;
				}
				IBeansConfig bc = project.getConfig(configFile, includeImports);
				if (bc != null) {
					return bc;
				}
			}
		}
		return null;
	}

	private static boolean isBeansConfig(IFile configFile, boolean includeImports) {
		IBeansProject project = BeansCorePlugin.getModel().getProject(configFile.getProject());
		if (project != null) {
			IBeansConfig bc = getBeansConfig(configFile, project, includeImports);
			if (bc != null)
				return true;
		}

		for (IBeansProject p : BeansCorePlugin.getModel().getProjects()) {
			IBeansConfig bc = getBeansConfig(configFile, p, includeImports);
			if (bc != null)
				return true;
		}

		return false;
	}

}
