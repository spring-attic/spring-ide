/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.properties.model;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;

/**
 * This {@link ILabelProvider}Êdelegates to the {@link BeansModelLabelProvider}
 * and uses the {@link IBeansConfig}'s name.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class PropertiesModelLabelProvider extends BeansModelLabelProvider implements IColorProvider {
	
	private Color grayColor = new Color(Display.getDefault(), 150, 150, 150);
	
	@Override
	protected String getText(Object element, Object parentElement) {
		if (element instanceof IBeansConfig) {
			return ((IBeansConfig) element).getElementName();
		}
		return super.getText(element, parentElement);
	}

	public Color getBackground(Object element) {
		return null;
	}

	public Color getForeground(Object element) {
		if (element instanceof IBeansConfig
				&& ((IBeansConfig) element).getType() == IBeansConfig.Type.AUTO_DETECTED) {
			return grayColor;
		}
		return null;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		grayColor.dispose();
	}
}
