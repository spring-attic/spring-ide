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

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryTargetProperties;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.Connectable;
import org.springframework.ide.eclipse.boot.dash.model.Connectable.ConnectionStateListener;
import org.springframework.ide.eclipse.boot.dash.model.RunTargetWithProperties;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

/**
 * Action for toggling Boot Dash model connection
 *
 * @author Alex Boyko
 *
 */
public class ToggleBootDashModelConnection extends AbstractBootDashModelAction {

	private final ConnectionStateListener CONNECTION_LISTENER = new ConnectionStateListener() {
		@Override
		public void changed() {
			update();
		}
	};

	private Connectable listeningTo = null;

	protected ToggleBootDashModelConnection(LiveExpression<BootDashModel> section, UserInteractions ui) {
		super(section, ui);
	}

	@Override
	public void update() {
		BootDashModel model = sectionSelection.getValue();
		Connectable connectable = null;
		if (model instanceof Connectable) {
			setVisible(true);
			setEnabled(true);
			connectable = (Connectable) model;
			if (connectable.isConnected()) {
				setText("Disconnect");
				setDescription("Disconnect Run Target");
			} else {
				setText("Connect");
				setDescription("Connect Run Target");
			}
		} else {
			setVisible(false);
			setEnabled(false);
		}
		// Update model state and model connection listeners
		if (listeningTo != connectable) {
			if (listeningTo != null) {
				listeningTo.removeConnectionStateListener(CONNECTION_LISTENER);
			}
			listeningTo = connectable;
			if (listeningTo != null) {
				listeningTo.addConnectionStateListener(CONNECTION_LISTENER);
			}
		}
	}

	@Override
	public void run() {
		BootDashModel model = sectionSelection.getValue();
		if (model  instanceof Connectable) {
			Connectable connectable = (Connectable) model;
			if (connectable.isConnected()) {
				connectable.disconnect();
				if (model.getRunTarget() instanceof RunTargetWithProperties) {
					RunTargetWithProperties target = (RunTargetWithProperties) model.getRunTarget();
					target.getTargetProperties().put(CloudFoundryTargetProperties.DISCONNECTED, "true"); //$NON-NLS-1$
					model.getViewModel().updateTargetPropertiesInStore();
				}
			} else {
				connectable.connect();
				if (model.getRunTarget() instanceof RunTargetWithProperties) {
					RunTargetWithProperties target = (RunTargetWithProperties) model.getRunTarget();
					target.getTargetProperties().put(CloudFoundryTargetProperties.DISCONNECTED, null); //$NON-NLS-1$
					model.getViewModel().updateTargetPropertiesInStore();
				}
			}
		}
	}

	@Override
	public void dispose() {
		if (listeningTo != null) {
			listeningTo.removeConnectionStateListener(CONNECTION_LISTENER);
			listeningTo = null;
		}
		super.dispose();
	}

}
