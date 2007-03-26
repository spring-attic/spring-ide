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
package org.springframework.ide.eclipse.aop.ui.navigator.action;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.springframework.ide.eclipse.aop.ui.navigator.model.IRevealableReferenceNode;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;


/**
 * Opens the file for currently selected {@link IBeansConfig}.
 * 
 * @author Christian Dupuis
 * @since 2.0
 * 
 */
public class OpenConfigFileAction extends Action {

	private ICommonActionExtensionSite site;

	private IRevealableReferenceNode element;

	public OpenConfigFileAction(ICommonActionExtensionSite site) {
		this.site = site;
		setText("Op&en"); // TODO externalize text
	}

	@Override
	public boolean isEnabled() {
		ISelection selection = site.getViewSite().getSelectionProvider()
				.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sSelection = (IStructuredSelection) selection;
			if (sSelection.size() == 1) {
				if (sSelection.getFirstElement() instanceof IRevealableReferenceNode) {
					element = (IRevealableReferenceNode) sSelection
							.getFirstElement();
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void run() {
		if (isEnabled()) {
			element.openAndReveal();
		}
	}
}
