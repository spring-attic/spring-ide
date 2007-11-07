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
package org.springframework.ide.eclipse.ui.navigator.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * {@link Action} implementation that enables and disables lexical sorting in
 * the Spring Explorer.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class LexicalSortingAction extends Action implements
		IViewActionDelegate, IActionDelegate2 {

	private CommonViewer viewer;

	private IAction action;
	
	public void dispose() {
		viewer = null;
		action = null;
	}

	public void init(IAction action) {
		this.action = action;
		action.setChecked(SpringUIUtils.isSortingEnabled());
	}
	
	public void init(IViewPart view) {
		if (view instanceof CommonNavigator) {
			viewer = ((CommonNavigator) view).getCommonViewer();
		}
	}

	@Override
	public void run() {
		SpringUIUtils.setSortingEnabled(action.isChecked());
		viewer.refresh();
	}

	public void run(IAction action) {
		run();
	}

	public void runWithEvent(IAction action, Event event) {
		run();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// nothing to do
	}
}
