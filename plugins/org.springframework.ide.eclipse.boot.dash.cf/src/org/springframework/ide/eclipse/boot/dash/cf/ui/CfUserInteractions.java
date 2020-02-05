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
package org.springframework.ide.eclipse.boot.dash.cf.ui;

import org.springframework.ide.eclipse.boot.dash.cf.dialogs.CustomizeAppsManagerURLDialogModel;
import org.springframework.ide.eclipse.boot.dash.cf.dialogs.PasswordDialogModel;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.dialogs.DeploymentPropertiesDialogModel;

public interface CfUserInteractions {
	void openPasswordDialog(PasswordDialogModel model);
	void openEditAppsManagerURLDialog(CustomizeAppsManagerURLDialogModel model);

	/**
	 * Brings up the UI to enter application deployment manifest
	 */
	CloudApplicationDeploymentProperties promptApplicationDeploymentProperties(DeploymentPropertiesDialogModel model) throws Exception;
}
