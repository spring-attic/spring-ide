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
package org.springframework.ide.eclipse.aop.ui.matcher.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.springframework.ide.eclipse.aop.ui.matcher.PointcutMatcherPlugin;

/**
 * Action Delegate that is used to open the pointcut matcher page from Eclipse'
 * Search menu.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class OpenPointcutMatcherPageAction implements
		IWorkbenchWindowActionDelegate {

	private static final String POINTCUT_MATCHER_PAGE_ID = 
		PointcutMatcherPlugin.PLUGIN_ID	+ ".matcherPage";

	private IWorkbenchWindow window;

	public void dispose() {
		window = null;
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run(IAction action) {
		if (window == null || window.getActivePage() == null) {
			PointcutMatcherPlugin.log("Could not open the search dialog - for "
					+ "some reason the window handle was null", null);
			return;
		}
		NewSearchUI.openSearchDialog(window, POINTCUT_MATCHER_PAGE_ID);
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// do nothing since the action isn't selection dependent.
	}
}
