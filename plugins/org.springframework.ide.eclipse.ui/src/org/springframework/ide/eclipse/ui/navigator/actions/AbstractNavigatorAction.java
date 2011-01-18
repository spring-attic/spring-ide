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
package org.springframework.ide.eclipse.ui.navigator.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

/**
 * Abstract base class for {@link CommonNavigator} actions.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public abstract class AbstractNavigatorAction extends Action {

	private ICommonActionExtensionSite actionSite;

	public AbstractNavigatorAction(ICommonActionExtensionSite actionSite) {
		this.actionSite = actionSite;
	}

	public AbstractNavigatorAction(ICommonActionExtensionSite actionSite,
			String text, ImageDescriptor image) {
		super(text, image);
		this.actionSite = actionSite;
	}

	@Override
	public final boolean isEnabled() {
		ISelection selection = actionSite.getViewSite().getSelectionProvider()
				.getSelection();
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			return isEnabled((IStructuredSelection) selection);
		}
		return false;
	}

	protected abstract boolean isEnabled(IStructuredSelection selection);

	protected final ICommonActionExtensionSite getActionSite() {
		return actionSite;
	}
}
