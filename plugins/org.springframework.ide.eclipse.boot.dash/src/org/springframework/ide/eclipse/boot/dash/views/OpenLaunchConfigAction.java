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

import org.eclipse.debug.core.ILaunchConfiguration;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

import com.google.common.collect.ImmutableSet;

public class OpenLaunchConfigAction extends AbstractBootDashElementsAction {

	public OpenLaunchConfigAction(BootDashViewModel model, MultiSelection<BootDashElement> selection, UserInteractions ui) {
		super(model, selection, ui);
		this.setText("Open Config");
		this.setToolTipText("Open the launch configuration associated with the selected element, if one exists, or create one if it doesn't.");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/write_obj.gif"));
		this.setDisabledImageDescriptor(BootDashActivator.getImageDescriptor("icons/write_obj_disabled.gif"));
	}

	@Override
	public void run() {
		Collection<BootDashElement> selecteds = getSelectedElements();
		for (BootDashElement bootDashElement : selecteds) {
			bootDashElement.openConfig(ui);
		}
	}

	@Override
	public void updateEnablement() {
		setEnabled(shouldEnable());
	}

	private boolean shouldEnable() {
		BootDashElement element = getSingleSelectedElement();
		if (element!=null) {
			ImmutableSet<ILaunchConfiguration> confs = element.getLaunchConfigs();
			return confs.size()<=1;
		}
		return false;
	}

}
