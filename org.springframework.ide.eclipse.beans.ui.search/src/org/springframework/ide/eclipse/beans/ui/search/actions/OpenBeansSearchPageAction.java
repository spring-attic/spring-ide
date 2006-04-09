/*
 * Copyright 2002-2006 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

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
