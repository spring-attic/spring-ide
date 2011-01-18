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
package org.springframework.ide.eclipse.webflow.ui.properties;

import org.eclipse.jface.viewers.ILabelProvider;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;

/**
 * This {@link ILabelProvider} delegates to the {@link BeansModelLabelProvider}and uses the {@link IBeansConfig}'s name.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class BeansConfigLabelProvider extends BeansModelLabelProvider {

	public BeansConfigLabelProvider() {
		super(true);
	}

	@Override
	protected String getText(Object element, Object parentElement) {
		if (element instanceof IBeansConfig) {
			return ((IBeansConfig) element).getElementName();
		}
		return super.getText(element, parentElement);
	}
}
