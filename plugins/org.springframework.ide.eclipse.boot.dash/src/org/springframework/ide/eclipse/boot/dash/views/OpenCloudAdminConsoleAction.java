/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryRunTarget;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RunTarget;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;
import org.springsource.ide.eclipse.commons.ui.UiUtil;

/**
 * @author Martin Lippert
 */
public class OpenCloudAdminConsoleAction extends AbstractCloudDashModelAction {

	public OpenCloudAdminConsoleAction(LiveExpression<BootDashModel> sectionSelection, UserInteractions ui) {
		super(sectionSelection, ui);
		this.setText("Open Cloud Admin Console");
		this.setToolTipText("Opens the Cloud Administration Console for this Cloud Foundry Space and Organisation");
		this.setImageDescriptor(BootDashActivator.getImageDescriptor("icons/cloud_obj.png"));
	}

	@Override
	public void run() {
		final BootDashModel targetModel = sectionSelection.getValue();
		if (targetModel != null) {
			RunTarget runTarget = targetModel.getRunTarget();
			if (runTarget instanceof CloudFoundryRunTarget) {
				String appsManagerURL = ((CloudFoundryRunTarget) runTarget).getAppsManagerURL();
				if (appsManagerURL != null && appsManagerURL.length() > 0) {
					UiUtil.openUrl(appsManagerURL);
				}
				else {
					ui.errorPopup("can't find unique identificators",
							"The Cloud Target that you selected doesn't contain required information about the organization and the space yet (recently added unique identifiers). Please remove the target and add it again to fix this.");
				}
			}
		}
	}

	@Override
	public void updateEnablement() {
		final BootDashModel targetModel = sectionSelection.getValue();
		if (targetModel != null) {
			RunTarget runTarget = targetModel.getRunTarget();
			this.setEnabled(runTarget instanceof CloudFoundryRunTarget);
		} else {
			this.setEnabled(false);
		}
	}

}
