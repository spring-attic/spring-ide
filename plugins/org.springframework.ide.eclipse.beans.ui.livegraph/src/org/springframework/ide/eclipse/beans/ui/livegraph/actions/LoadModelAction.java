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
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBeansModel;
import org.springframework.ide.eclipse.beans.ui.livegraph.views.LiveBeansGraphView;

/**
 * @author Leo Dos Santos
 */
public class LoadModelAction extends Action {

	private final LiveBeansGraphView view;

	private final LiveBeansModel model;

	public LoadModelAction(LiveBeansGraphView view, LiveBeansModel model) {
		super(model.getApplicationName(), Action.AS_RADIO_BUTTON);
		this.view = view;
		this.model = model;
	}

	@Override
	public boolean isChecked() {
		return model.equals(view.getInput());
	}

	@Override
	public void run() {
		view.setInput(model);
	}

}
