/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.livegraph.actions;

import org.eclipse.jface.action.Action;
import org.springframework.ide.eclipse.beans.ui.livegraph.views.LiveBeansGraphView;

/**
 * @author Leo Dos Santos
 */
public class ToggleGroupByAction extends Action {

	private final LiveBeansGraphView view;

	private final int mode;

	public ToggleGroupByAction(LiveBeansGraphView view, int mode) {
		super("", AS_RADIO_BUTTON);
		if (mode == LiveBeansGraphView.GROUP_BY_RESOURCE) {
			setText("Group by resource");
		}
		else if (mode == LiveBeansGraphView.GROUP_BY_CONTEXT) {
			setText("Group by context");
		}
		this.view = view;
		this.mode = mode;
	}

	public int getGroupByMode() {
		return mode;
	}

	@Override
	public void run() {
		if (isChecked()) {
			view.setGroupByMode(mode);
		}
	}

}
