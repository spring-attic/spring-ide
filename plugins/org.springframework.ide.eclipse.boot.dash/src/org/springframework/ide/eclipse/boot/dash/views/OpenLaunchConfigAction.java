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
import org.springframework.ide.eclipse.boot.dash.livexp.MultiSelection;
import org.springframework.ide.eclipse.boot.dash.livexp.ObservableSet;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashViewModel;
import org.springframework.ide.eclipse.boot.dash.model.BootProjectDashElement;
import org.springframework.ide.eclipse.boot.dash.model.LaunchConfDashElement;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.dash.model.runtargettypes.LocalRunTargetType;

/**
 * @author Kris De Volder
 */
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
		if (element instanceof BootProjectDashElement) {
			BootProjectDashElement projectEl = (BootProjectDashElement) element;
			ObservableSet<BootDashElement> confs = projectEl.getAllChildren();
			return confs.getValues().size()<=1;
		} else if (element instanceof LaunchConfDashElement) {
			return true;
		}
		return false;
	}

	@Override
	public void updateVisibility() {
		setVisible(shouldShow());
	}

	private boolean shouldShow() {
		//Only show if all selected elements are local elements
		for (BootDashElement e : getSelectedElements()) {
			if (!isCorrectTargetType(e)) {
				return false;
			}
		}
		return true;
	}

	private boolean isCorrectTargetType(BootDashElement e) {
		return e.getTarget().getType() instanceof LocalRunTargetType;
	}

}
