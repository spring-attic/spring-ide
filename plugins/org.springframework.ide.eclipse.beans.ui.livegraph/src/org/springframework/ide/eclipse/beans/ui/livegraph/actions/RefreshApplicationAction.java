/*******************************************************************************
 * Copyright (c) 2012, 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.livegraph.actions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.springframework.ide.eclipse.beans.ui.live.model.LiveBeansModel;
import org.springframework.ide.eclipse.beans.ui.livegraph.LiveGraphUIImages;
import org.springframework.ide.eclipse.beans.ui.livegraph.LiveGraphUiPlugin;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBeansModelGenerator;
import org.springframework.ide.eclipse.beans.ui.livegraph.views.LiveBeansGraphView;
import org.springsource.ide.eclipse.commons.core.StatusHandler;

/**
 * @author Leo Dos Santos
 */
public class RefreshApplicationAction extends Action {

	private final LiveBeansGraphView view;

	public RefreshApplicationAction(LiveBeansGraphView view) {
		super("Refresh Graph", LiveGraphUIImages.REFRESH);
		this.view = view;
	}

	@Override
	public void run() {
		try {
			LiveBeansModel model = view.getInput();
			if (model != null) {
				view.setInput(LiveBeansModelGenerator.refreshModel(model));
			}
		}
		catch (CoreException e) {
			StatusHandler.log(new Status(IStatus.ERROR, LiveGraphUiPlugin.PLUGIN_ID, e.getMessage(), e));
		}
	}

}
