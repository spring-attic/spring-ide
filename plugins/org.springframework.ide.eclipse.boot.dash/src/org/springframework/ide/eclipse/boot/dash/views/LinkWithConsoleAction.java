/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.livexp.core.ValueListener;

import com.google.common.collect.ImmutableSet;

public class LinkWithConsoleAction extends AbstractBootDashElementsAction {


	private ValueListener<ImmutableSet<BootDashElement>> listener;

	public LinkWithConsoleAction(Params params) {
		super(params);
		this.setText("Link with Console");
		this.setToolTipText("Link with Console");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/link_with_console.gif"));

		selection.getElements().addListener(listener = new ValueListener<ImmutableSet<BootDashElement>>() {
			public void gotValue(LiveExpression<ImmutableSet<BootDashElement>> exp, ImmutableSet<BootDashElement> selecteds) {
				linkToConsole();
			}
		});
	}

	@Override
	public void run() {
		linkToConsole();
	}

	@Override
	public void dispose() {
		if(listener != null && selection !=  null) {
			selection.getElements().removeListener(listener);
		}
		super.dispose();
	}

	protected void linkToConsole() {
		if (LinkWithConsoleAction.this.isChecked()) {
			BootDashModelConsoleManager.showSelected(ui, getSelectedElements());
		}
	}
}
