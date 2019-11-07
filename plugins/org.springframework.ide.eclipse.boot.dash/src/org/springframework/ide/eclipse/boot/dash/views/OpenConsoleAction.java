/*******************************************************************************
 * Copyright (c) 2015, 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import java.util.Collection;

import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;

public class OpenConsoleAction extends AbstractBootDashElementsAction {

	public OpenConsoleAction(Params params) {
		super(params);
		this.setText("Open Console");
		this.setToolTipText("Open Console");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/open_console.png"));
		this.setDisabledImageDescriptor(BootDashActivator.getImageDescriptor("icons/open_console_disabled.png"));
	}

	@Override
	public void run() {
		final Collection<BootDashElement> selecteds = getSelectedElements();
		BootDashModelConsoleManager.showSelected(ui, selecteds);
	}
}
