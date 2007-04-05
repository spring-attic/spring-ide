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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;

/**
 * @author Christian Dupuis
 */
public class DbLabelProvider extends LabelProvider {

	@Override
	public Image getImage(Object element) {
		return BeansModelLabelProvider.DEFAULT_NAMESPACE_LABEL_PROVIDER
				.getImage(element);
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IBean) {
			IBean bean = (IBean) element;
			return bean.getElementName();
		}
		else {
			return BeansModelLabelProvider.DEFAULT_NAMESPACE_LABEL_PROVIDER
					.getText(element);
		}
	}
}
