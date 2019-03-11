/*******************************************************************************
 * Copyright (c) 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.dialogs.CustomizeAppsManagerURLDialogModel;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springframework.ide.eclipse.boot.pstore.PropertyStoreApi;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

public class CustmomizeTargetAppManagerURLAction extends AbstractBootDashModelAction {

	protected CustmomizeTargetAppManagerURLAction(LiveExpression<BootDashModel> section, UserInteractions ui) {
		super(section, ui);
		setText("Customize Cloud Admin Console URL...");
	}

	@Override
	public void updateEnablement() {
		this.setEnabled(isApplicable(sectionSelection.getValue()));
	}

	public void updateVisibility() {
		this.setVisible(isApplicable(sectionSelection.getValue()));
	}

	private boolean isApplicable(BootDashModel section) {
		if (section != null && section instanceof CloudFoundryBootDashModel) {
			PropertyStoreApi props = section.getRunTarget().getType().getPersistentProperties();
			return props != null;
		}
		return false;
	}

	@Override
	public void run() {
		final BootDashModel section = sectionSelection.getValue();
		if (isApplicable(section)) {
			CustomizeAppsManagerURLDialogModel model = new CustomizeAppsManagerURLDialogModel((CloudFoundryBootDashModel)section);
			ui.openEditAppsManagerURLDialog(model);
		}
	}

}
