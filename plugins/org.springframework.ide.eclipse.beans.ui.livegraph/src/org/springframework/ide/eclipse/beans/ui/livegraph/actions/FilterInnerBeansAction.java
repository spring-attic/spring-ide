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
public class FilterInnerBeansAction extends Action {

	private final LiveBeansGraphView view;

	public FilterInnerBeansAction(LiveBeansGraphView view) {
		super("Filter Inner Beans", AS_CHECK_BOX);
		this.view = view;
	}

	@Override
	public void run() {
		view.setFilterInnerBeans(isChecked());
	}

}
