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
package org.springframework.ide.eclipse.beans.ui.search.jdt;

import org.eclipse.jface.viewers.ILabelProvider;
import org.springframework.ide.eclipse.beans.ui.BeansUILabels;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabels;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * This {@link ILabelProvider}Êdelegates to the {@link BeansModelLabelProvider}
 * and appends the {@link IModelElement}'s path to the label.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class BeansJavaSearchLabelProvider extends BeansModelLabelProvider {

	public BeansJavaSearchLabelProvider(boolean isDecorating) {
		super(isDecorating);
	}	

	@Override
	public String getText(Object element) {
		if (element instanceof IModelElement) {
			StringBuffer buffer = new StringBuffer(super.getText(element));
			buffer.append(BeansUILabels.CONCAT_STRING);
			BeansModelLabels.appendElementPathLabel((IModelElement) element,
					BeansUILabels.DESCRIPTION, buffer);
			return buffer.toString();
		}
		return super.getText(element);
	}
}
