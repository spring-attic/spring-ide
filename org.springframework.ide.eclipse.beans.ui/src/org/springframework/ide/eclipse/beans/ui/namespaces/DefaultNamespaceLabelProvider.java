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
package org.springframework.ide.eclipse.beans.ui.namespaces;

import org.eclipse.jface.viewers.ITreePathLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.navigator.IDescriptionProvider;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.BeansUILabels;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelImages;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;

/**
 * This class is a label provider which knows about the beans core model's
 * {@link ISourceModelElement source elements} which belong to a namespace.
 * 
 * @author Torsten Juergeleit
 */
public class DefaultNamespaceLabelProvider extends LabelProvider implements
		ITreePathLabelProvider, IDescriptionProvider {

	@Override
	public Image getImage(Object element) {
		if (element instanceof ISourceModelElement) {
			return getImage((ISourceModelElement) element, null);
		}
		return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_SPRING);
	}

	@Override
	public String getText(Object element) {
		if (element instanceof ISourceModelElement) {
			return getElementLabel((ISourceModelElement) element, 0);
		}
		return null;
	}

	public void updateLabel(ViewerLabel label, TreePath elementPath) {
		Object element = ModelUtils.adaptToModelElement(elementPath
				.getLastSegment());
		if (element instanceof ISourceModelElement
				&& elementPath.getSegmentCount() > 1) {
			Object parent = elementPath.getParentPath().getLastSegment();
			IModelElement parentElement = (parent instanceof IModelElement
					? (IModelElement) parent : null);
			label.setImage(getImage((ISourceModelElement) element,
					parentElement));
			label.setText(getText(element));
		}
	}

	public String getDescription(Object element) {
		if (element instanceof ISourceModelElement) {
			return getElementLabel((ISourceModelElement) element,
					BeansUILabels.APPEND_PATH
							| BeansUILabels.DESCRIPTION);
		}
		return null;
	}

	protected Image getImage(ISourceModelElement element, IModelElement context) {
		if (element instanceof IBean) {
			if (!NamespaceUtils.DEFAULT_NAMESPACE_URI.equals(NamespaceUtils
					.getNameSpaceURI(element))) {
				return BeansModelImages.getDecoratedImage(BeansUIImages
						.getImage(BeansUIImages.IMG_OBJS_NAMESPACE_BEAN),
						element, context);
			}
		}
		else if (element instanceof IBeansComponent) {
			return BeansModelImages.getDecoratedImage(BeansUIImages
					.getImage(BeansUIImages.IMG_OBJS_NAMESPACE_COMPONENT),
					element, context);
		}
		return BeansModelImages.getImage(element, context);
	}

	protected String getElementLabel(ISourceModelElement element, int flags) {
		return DefaultNamespaceLabels.getElementLabel(element, flags);
	}
}
