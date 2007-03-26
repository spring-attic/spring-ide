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
package org.springframework.ide.eclipse.aop.ui.navigator;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;
import org.springframework.ide.eclipse.aop.ui.navigator.model.IReferenceNode;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;

/**
 * {@link ICommonLabelProvider} that just delegates to
 * {@link IReferenceNode#getText()} and {@link IReferenceNode#getImage()} of
 * instances of {@link IReferenceNode}. Otherwise calls
 * {@link BeansModelLabelProvider}.
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
public class AopReferenceModelNavigatorLabelProvider extends
		BeansModelLabelProvider implements ICommonLabelProvider {

	public String getDescription(Object element) {
		// TODO add description here
		return null;
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof IReferenceNode) {
			return ((IReferenceNode) element).getImage();
		}
		return super.getImage(element);
	}

	@Override
	public String getText(Object element) {
		if (element instanceof IReferenceNode) {
			return ((IReferenceNode) element).getText();
		}
		return super.getText(element);
	}

	public void init(ICommonContentExtensionSite config) {
	}

	public void restoreState(IMemento memento) {
	}

	public void saveState(IMemento memento) {
	}
}
