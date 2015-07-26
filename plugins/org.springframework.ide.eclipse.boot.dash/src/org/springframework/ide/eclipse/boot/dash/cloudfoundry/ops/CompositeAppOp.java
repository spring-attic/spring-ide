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

import java.util.List;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class CompositeAppOp extends CloudApplicationOperation {

	private List<CloudApplicationOperation> operations;

	public CompositeAppOp(String opName, CloudFoundryOperations client, String appName, CloudFoundryBootDashModel model,
			UserInteractions ui, List<CloudApplicationOperation> operations) {
		super(opName, client, appName, model, ui);
		this.operations = operations;
	}

	@Override
	public void addApplicationUpdateListener(ApplicationUpdateListener appUpdateNotifier) {
		super.addApplicationUpdateListener(appUpdateNotifier);
		for (CloudApplicationOperation op : operations) {
			op.addApplicationUpdateListener(appUpdateNotifier);
		}
	}

	@Override
	protected CloudApplication doCloudOp(CloudFoundryOperations client, IProgressMonitor monitor)
			throws Exception, OperationCanceledException {
		for (CloudApplicationOperation op : operations) {
			op.run(monitor);
		}
		return null;
	}

}
