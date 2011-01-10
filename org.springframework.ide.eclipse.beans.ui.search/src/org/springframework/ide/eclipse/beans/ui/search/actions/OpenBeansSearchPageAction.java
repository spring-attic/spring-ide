/*******************************************************************************
 * Copyright (c) 2006, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.search.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.springframework.ide.eclipse.beans.ui.search.BeansSearchPlugin;

/**
 * Opens the search dialog and brings the beans search page to front.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class OpenBeansSearchPageAction implements IWorkbenchWindowActionDelegate {

	private static final String BEANS_SEARCH_PAGE_ID =
								   BeansSearchPlugin.PLUGIN_ID + ".searchPage";
	private IWorkbenchWindow window;

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run(IAction action) {
		if (window == null || window.getActivePage() == null) {
			beep();
			BeansSearchPlugin.log("Could not open the search dialog - for " +
							   "some reason the window handle was null", null);
			return;
		}
		NewSearchUI.openSearchDialog(window, BEANS_SEARCH_PAGE_ID);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing since the action isn't selection dependent.
	}

	public void dispose() {
		window = null;
	}

	protected void beep() {
		Shell shell = BeansSearchPlugin.getActiveWorkbenchShell();
		if (shell != null && shell.getDisplay() != null) {
			shell.getDisplay().beep();
		}
	}	
}
