/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.data.beans.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansTypedString;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.beans.ui.namespaces.DefaultNamespaceLabelProvider;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.data.SpringDataUtils;

/**
 * Tweaks bean text to reflect the actual repositories interface rather than the factory. Applies the defaults for
 * properties, too, as they are not reflected at this parsing stage.
 * 
 * @author Oliver Gierke
 */
public class RepositoriesNamespaceLabelProvider extends DefaultNamespaceLabelProvider {

	private static final Map<String, Object> DEFAULTS;

	static {
		DEFAULTS = new HashMap<String, Object>();
		DEFAULTS.put("queryLookupStrategy", QueryLookupStrategy.Key.CREATE_IF_NOT_FOUND);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.beans.ui.namespaces.DefaultNamespaceLabelProvider#getText(org.springframework.ide.eclipse.core.model.ISourceModelElement, org.springframework.ide.eclipse.core.model.IModelElement, boolean)
	 */
	@Override
	public String getText(ISourceModelElement element, IModelElement context, boolean isDecorating) {

		if (element instanceof IBean) {
			return SpringDataUtils.asText((IBean) element);
		}

		if (element instanceof IBeansTypedString) {

			String name = element.getElementParent().getElementName();
			String text = super.getText(element, context, isDecorating);

			boolean hasDefault = DEFAULTS.containsKey(name);
			boolean isNull = "null".equals(text);

			return hasDefault && isNull ? DEFAULTS.get(name).toString() : text;

		}

		return super.getText(element, context, isDecorating);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.beans.ui.namespaces.DefaultNamespaceLabelProvider#getImage(org.springframework.ide.eclipse.core.model.ISourceModelElement, org.springframework.ide.eclipse.core.model.IModelElement, boolean)
	 */
	@Override
	public Image getImage(ISourceModelElement element, IModelElement context, boolean isDecorating) {

		if (element instanceof IBean) {
			return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_BEAN);
		} else {
			return super.getImage(element, context, isDecorating);
		}
	}
}
