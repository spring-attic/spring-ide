/*******************************************************************************
 * Copyright (c) 2007, 2013 Spring IDE Developers
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansJavaConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;

/**
 * This {@link ILabelProvider}Êdelegates to the {@link BeansModelLabelProvider}and uses the {@link IBeansConfig}'s name.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.0
 */
public class PropertiesModelLabelProvider extends BeansModelLabelProvider implements IColorProvider {
	
	private Color grayColor = new Color(Display.getDefault(), 150, 150, 150);
	
	@Override
	protected String getText(Object element, Object parentElement) {
		if (element instanceof IBeansConfig) {
			String text = ((IBeansConfig) element).getElementName();
			if (text.startsWith(IBeansConfig.EXTERNAL_FILE_NAME_PREFIX)) {
				text = text.substring(IBeansConfig.EXTERNAL_FILE_NAME_PREFIX.length());
			}
			if (element instanceof BeansJavaConfig) {
				text = ((BeansJavaConfig) element).getConfigClass().getElementName();
			}
			return text;
		}
		return super.getText(element, parentElement);
	}
	
	@Override
	protected Image getImage(Object element, Object parentElement) {
		if (element instanceof BeansJavaConfig) {
			return super.getImage(((BeansJavaConfig) element).getConfigClass());
		}
		else {
			return super.getImage(element, parentElement);
		}
	}

	public Color getBackground(Object element) {
		return null;
	}

	public Color getForeground(Object element) {
		if (element instanceof IBeansConfig
				&& ((IBeansConfig) element).getType() == IBeansConfig.Type.AUTO_DETECTED) {
			return grayColor;
		}
		else if (element instanceof IBeansConfigSet
				&& ((IBeansConfigSet) element).getType() == IBeansConfigSet.Type.AUTO_DETECTED) {
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
