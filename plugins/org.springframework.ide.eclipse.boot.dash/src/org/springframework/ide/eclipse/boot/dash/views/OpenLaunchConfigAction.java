/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import java.util.Collection;

import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;

public class OpenLaunchConfigAction extends AbstractBootDashAction {

	public OpenLaunchConfigAction(BootDashView owner) {
		super(owner);
		this.setText("Open Config");
		this.setToolTipText("Open the launch configuration associated with the selected element, if one exists, or create one if it doesn't.");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/write_obj.gif"));
		this.setDisabledImageDescriptor(BootDashActivator.getImageDescriptor("icons/write_obj_disabled.gif"));
	}

	@Override
	public void run() {
		Collection<BootDashElement> selecteds = owner.getSelectedElements();
		for (BootDashElement bootDashElement : selecteds) {
			bootDashElement.openConfig(owner.getUserInteractions());
		}
	}

}
