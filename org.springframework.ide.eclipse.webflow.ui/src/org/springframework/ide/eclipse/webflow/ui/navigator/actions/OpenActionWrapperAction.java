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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.springframework.ide.eclipse.ui.navigator.actions.AbstractNavigatorAction;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;

/**
 * {@link Action} implementation that wraps {@link OpenConfigFileAction} and
 * {@link OpenWebflowGraphAction} and gets installed as global double click
 * action.
 * 
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class OpenActionWrapperAction extends AbstractNavigatorAction {

	private OpenConfigFileAction openConfigFileAction;
	private OpenWebflowGraphAction openWebflowGraphAction;
	private Action action;

	public OpenActionWrapperAction(ICommonActionExtensionSite site,
			OpenConfigFileAction openConfigFileAction,
			OpenWebflowGraphAction openWebflowGraphAction) {
		super(site);
		this.openConfigFileAction = openConfigFileAction;
		this.openWebflowGraphAction = openWebflowGraphAction;
		setText("Op&en"); // TODO externalize text
	}

	public boolean isEnabled(IStructuredSelection selection) {
		if (selection.size() == 1) {
			Object element = selection.getFirstElement();
			if (element instanceof IWebflowConfig
					&& openWebflowGraphAction.isEnabled()) {
				this.action = openWebflowGraphAction;
				return true;
			}
			else if (element instanceof IFile
					&& openConfigFileAction.isEnabled()) {
				if (WebflowModelUtils.isWebflowConfig((IFile) element)) {
					this.action = openConfigFileAction;
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void run() {
		this.action.run();
	}
}
