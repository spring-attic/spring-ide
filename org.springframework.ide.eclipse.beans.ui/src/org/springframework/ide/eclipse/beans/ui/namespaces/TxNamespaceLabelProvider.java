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
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelImages;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * This class is a label provider which knows about the beans core model's
 * {@link ISourceModelElement source elements} in the namespace
 * <code>"http://www.springframework.org/schema/tx"</code>.
 * 
 * @author Torsten Juergeleit
 */
public class TxNamespaceLabelProvider extends DefaultNamespaceLabelProvider {

	@Override
	public Image getImage(ISourceModelElement element, IModelElement context) {
		if (element instanceof IBean
				&& !BeansModelUtils.isInnerBean((IBean) element)) {
			return BeansModelImages.getDecoratedImage(BeansUIImages
					.getImage(BeansUIImages.IMG_OBJS_TX), element, context);
		}
		return super.getImage(element, context);
	}
}
