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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class CloudApplicationDeleteOperation extends CloudApplicationOperation {

	public CloudApplicationDeleteOperation(CloudFoundryOperations client, String appName,
			CloudFoundryBootDashModel model, UserInteractions ui) {
		super("Deleting application", client, appName, model, ui);
	}

	@Override
	protected CloudApplication doCloudOp(CloudFoundryOperations client, IProgressMonitor monitor) throws Exception {
		try {
			client.deleteApplication(appName);
		} catch (Exception e) {
			if (!is404Error(e)) {
				throw e;
			}
		}
		return null;
	}

}
