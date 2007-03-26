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
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;

/**
 * {@link Action} implementation that wraps {@link OpenConfigFileAction} and
 * {@link OpenWebflowGraphAction} and gets installed as global double click
 * action.
 * @author Christian Dupuis
 * @since 2.0
 */
public class OpenActionWrapperAction extends Action {

	private ICommonActionExtensionSite site;

	private OpenConfigFileAction openConfigFileAction;

	private OpenWebflowGraphAction openWebflowGraphAction;

	private Action action;

	public OpenActionWrapperAction(ICommonActionExtensionSite site,
			OpenConfigFileAction openConfigFileAction,
			OpenWebflowGraphAction openWebflowGraphAction) {
		this.site = site;
		this.openConfigFileAction = openConfigFileAction;
		this.openWebflowGraphAction = openWebflowGraphAction;
		setText("Op&en"); // TODO externalize text
	}

	@Override
	public boolean isEnabled() {
		ISelection selection = site.getViewSite().getSelectionProvider()
				.getSelection();
		if (selection instanceof ITreeSelection) {
			ITreeSelection tSelection = (ITreeSelection) selection;
			if (tSelection.size() == 1) {
				Object tElement = tSelection.getFirstElement();
				if (tElement instanceof IWebflowConfig && openWebflowGraphAction.isEnabled()) {
					this.action = openWebflowGraphAction;
					return true;
				}
				else if (tElement instanceof IFile && openConfigFileAction.isEnabled()) {
					if (WebflowModelUtils.isWebflowConfig((IFile) tElement)) {
						this.action = openConfigFileAction;
						return true;
					}
				}
			}
		}
		this.action = null;
		return false;
	}

	@Override
	public void run() {
		if (this.action != null) {
			this.action.run();
		}
	}

}
