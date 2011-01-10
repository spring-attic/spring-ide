/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
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
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.core.model.ModelUtils;

/**
 * This class is a label provider which knows about the beans core model's
 * {@link ISourceModelElement source elements} in the namespace
 * <code>"http://www.springframework.org/schema/util"</code>.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class UtilNamespaceLabelProvider extends DefaultNamespaceLabelProvider {

	@Override
	public Image getImage(ISourceModelElement element, IModelElement context, boolean isDecorating) {
		if (element instanceof IBean
				&& !BeansModelUtils.isInnerBean((IBean) element)) {
			String localName = ModelUtils.getLocalName((IBean) element);
			if ("constant".equals(localName)) {
				return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_CONSTANT);
			}
			if ("property-path".equals(localName)) {
				return BeansUIImages
						.getImage(BeansUIImages.IMG_OBJS_PROPERTY_PATH);
			}
			if ("list".equals(localName)) {
				return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_LIST);
			}
			if ("set".equals(localName)) {
				return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_LIST);
			}
			if ("map".equals(localName)) {
				return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_MAP);
			}
			if ("properties".equals(localName)) {
				return BeansUIImages
						.getImage(BeansUIImages.IMG_OBJS_PROPERTIES);
			}
		}
		return super.getImage(element, context, isDecorating);
	}
}
