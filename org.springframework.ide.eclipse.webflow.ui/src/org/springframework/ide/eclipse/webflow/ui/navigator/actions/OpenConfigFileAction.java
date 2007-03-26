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
package org.springframework.ide.eclipse.webflow.ui.navigator.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;

/**
 * Open {@link IWebflowConfig} in the standard Eclipse editor
 * @author Christian Dupuis
 * @since 2.0
 */
public class OpenConfigFileAction extends Action {

	private ICommonActionExtensionSite site;

	private IWebflowConfig element;

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
				Object sElement = sSelection.getFirstElement();
				if (sElement instanceof IWebflowConfig) {
					element = (IWebflowConfig) sElement;
					return true;
				}
				else if (sElement instanceof IFile) {
					if (WebflowModelUtils.isWebflowConfig((IFile) sElement)) {
						element = WebflowModelUtils.getWebflowConfig((IFile) sElement);
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void run() {
		if (isEnabled()) {
			SpringUIUtils.openInEditor(element.getResource(), -1);
		}
	}
}
