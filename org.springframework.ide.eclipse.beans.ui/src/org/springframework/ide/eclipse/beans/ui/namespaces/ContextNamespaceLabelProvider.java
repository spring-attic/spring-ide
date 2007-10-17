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

import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelImages;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * This class is a label provider which knows about the beans core model's
 * {@link ISourceModelElement source elements} in the namespace
 * <code>"http://www.springframework.org/schema/context"</code>.
 * 
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class ContextNamespaceLabelProvider extends DefaultNamespaceLabelProvider {

	@Override
	public Image getImage(ISourceModelElement element, IModelElement context, boolean isDecorating) {
		if (element instanceof IBean
				&& !BeansModelUtils.isInnerBean((IBean) element)) {
			return getNamespaceImage(element, context, isDecorating);
		}
		else if (element instanceof IBeansComponent) {
			return getNamespaceImage(element, context, isDecorating); 
		}
		return super.getImage(element, context, isDecorating);
	}

	private Image getNamespaceImage(ISourceModelElement element,
			IModelElement context, boolean isDecorating) {
		if (isDecorating) {
			return BeansModelImages.getDecoratedImage(BeansUIImages
					.getImage(BeansUIImages.IMG_OBJS_CONTEXT), element,	context);
		}
		else {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CONTEXT);
		}
	}
}
