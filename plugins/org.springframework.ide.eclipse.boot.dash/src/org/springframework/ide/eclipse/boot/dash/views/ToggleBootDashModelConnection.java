/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.views;

import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.ConnectOperation;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.TargetApplicationsRefreshOperation;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

/**
 * Action for toggling Boot Dash model connection
 *
 * @author Alex Boyko
 *
 */
public class ToggleBootDashModelConnection extends AbstractBootDashModelAction {

	protected ToggleBootDashModelConnection(LiveExpression<BootDashModel> section, UserInteractions ui) {
		super(section, ui);
	}

	@Override
	public void update() {
		super.update();
		BootDashModel model = sectionSelection.getValue();
		if (model instanceof CloudFoundryBootDashModel) {
			CloudFoundryBootDashModel connectable = (CloudFoundryBootDashModel) model;
			if (connectable.getCloudTarget().isConnected()) {
				setText("Disconnect");
				setDescription("Disconnect Run Target");
				setImageDescriptor(BootDashActivator.getImageDescriptor("icons/cloud-inactive.png"));
			} else {
				setText("Connect");
				setDescription("Connect Run Target");
				setImageDescriptor(BootDashActivator.getImageDescriptor("icons/cloud-ready.png"));
			}
		}
	}

	@Override
	public void updateEnablement() {
		setEnabled(isVisible());
	}

	@Override
	public void updateVisibility() {
		setVisible(sectionSelection.getValue() instanceof CloudFoundryBootDashModel);
	}

	@Override
	public void run() {
		BootDashModel model = sectionSelection.getValue();
		if (model  instanceof CloudFoundryBootDashModel) {
			CloudFoundryBootDashModel connectable = (CloudFoundryBootDashModel) model;
			connectable.getOperationsExecution(ui).runOpAsynch(new ConnectOperation(connectable, !connectable.getCloudTarget().isConnected(), ui));
			connectable.getOperationsExecution(ui).runOpAsynch(new TargetApplicationsRefreshOperation(connectable, ui));
		}
	}

}
