/*******************************************************************************
 * Copyright (c) 2007, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.namespaces;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreePathLabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.navigator.IDescriptionProvider;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IProfileAwareBeansComponent;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUILabels;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelImages;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * This class is a label provider which knows about the beans core model's {@link ISourceModelElement source elements}which belong to a namespace.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class DefaultNamespaceLabelProvider implements INamespaceLabelProvider, ITreePathLabelProvider,
		IDescriptionProvider {

	public Image getImage(ISourceModelElement element, IModelElement context, boolean isDecorating) {
		String namespaceUri = NamespaceUtils.getNameSpaceURI(element);

		if (element instanceof IBean && !NamespaceUtils.DEFAULT_NAMESPACE_URI.equals(namespaceUri)
				&& !BeansModelUtils.isInnerBean((IBean) element)) {
			Image image = null;

			org.springframework.ide.eclipse.beans.core.model.INamespaceDefinition namespaceDefinition = BeansCorePlugin
					.getNamespaceDefinitionResolver(BeansModelUtils.getProject(element).getProject())
					.resolveNamespaceDefinition(namespaceUri);
			if (namespaceDefinition != null && namespaceDefinition.getIconPath() != null) {
				image = NamespaceUtils.getImage(namespaceDefinition);
			}
			else {
				image = BeansUIImages.getImage(BeansUIImages.IMG_OBJS_NAMESPACE_BEAN);
			}
			if (isDecorating) {
				image = BeansModelImages.getDecoratedImage(image, element, context);
			}
			return image;
		}
		else if (element instanceof IProfileAwareBeansComponent) {
			Image image = BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CONFIG);
			if (isDecorating) {
				image = BeansModelImages.getDecoratedImage(image, element, context);
			}
			return image;
		}
		else if (element instanceof IBeansComponent) {
			Image image = null;

			org.springframework.ide.eclipse.beans.core.model.INamespaceDefinition namespaceDefinition = BeansCorePlugin
					.getNamespaceDefinitionResolver(BeansModelUtils.getProject(element).getProject())
					.resolveNamespaceDefinition(namespaceUri);
			if (namespaceDefinition != null && namespaceDefinition.getIconPath() != null) {
				image = NamespaceUtils.getImage(namespaceDefinition);
			}
			else {
				image = BeansUIImages.getImage(BeansUIImages.IMG_OBJS_NAMESPACE_COMPONENT);
			}
			if (isDecorating) {
				image = BeansModelImages.getDecoratedImage(image, element, context);
			}
			return image;
		}
		return BeansModelImages.getImage(element, context, isDecorating);
	}

	public String getText(ISourceModelElement element, IModelElement context, boolean isDecorating) {
		return getElementLabel(element, 0);
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void removeListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return true;
	}

	public void updateLabel(ViewerLabel label, TreePath elementPath) {
		Object element = elementPath.getLastSegment();
		if (element instanceof ISourceModelElement && elementPath.getSegmentCount() > 1) {
			Object parent = elementPath.getParentPath().getLastSegment();
			IModelElement context = (parent instanceof IModelElement ? (IModelElement) parent : null);
			// TODO CD revise
			label.setImage(getImage((ISourceModelElement) element, context, false));
			label.setText(getText((ISourceModelElement) element, context, false));
		}
	}

	public String getDescription(Object element) {
		if (element instanceof ISourceModelElement) {
			return getElementLabel((ISourceModelElement) element, BeansUILabels.APPEND_PATH | BeansUILabels.DESCRIPTION);
		}
		return null;
	}

	protected String getElementLabel(ISourceModelElement element, int flags) {
		return DefaultNamespaceLabels.getElementLabel(element, flags);
	}
}
