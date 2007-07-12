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
package org.springframework.ide.eclipse.beans.ui.properties.model;

import org.eclipse.jface.viewers.ILabelProvider;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;

/**
 * This {@link ILabelProvider}Êdelegates to the {@link BeansModelLabelProvider}
 * and uses the {@link IBeansConfig}'s name.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class PropertiesModelLabelProvider extends BeansModelLabelProvider {

	@Override
	protected String getText(Object element, Object parentElement) {
		if (element instanceof IBeansConfig) {
			return ((IBeansConfig) element).getElementName();
		}
		return super.getText(element, parentElement);
	}
}
