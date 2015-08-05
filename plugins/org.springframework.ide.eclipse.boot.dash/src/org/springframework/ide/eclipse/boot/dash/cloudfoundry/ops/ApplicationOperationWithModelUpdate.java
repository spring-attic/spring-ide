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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudAppInstances;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springframework.ide.eclipse.boot.dash.model.RunState;

/**
 * Runs {@link CloudApplicationOperation} in series and updates the model only
 * after all operations are complete.
 *
 * <p/>
 * The ops themselves may individually update the model if they chose too, but
 * at the very least, the model is updated after all of the ops are run or if
 * there is any error while performing the ops in series
 */
public class ApplicationOperationWithModelUpdate extends CloudApplicationOperation {

	private List<CloudApplicationOperation> operations;

	public ApplicationOperationWithModelUpdate(String opName, CloudFoundryBootDashModel model, String appName,
			List<CloudApplicationOperation> operations) {
		super(opName, model, appName);
		this.operations = operations;
	}

	public ApplicationOperationWithModelUpdate(CloudApplicationOperation enclosedOp) {
		super(enclosedOp.getName(), enclosedOp.model, enclosedOp.appName);

		this.operations = new ArrayList<CloudApplicationOperation>();
		this.operations.add(enclosedOp);
	}

	@Override
	public void addApplicationUpdateListener(ApplicationUpdateListener appUpdateNotifier) {
		super.addApplicationUpdateListener(appUpdateNotifier);
		for (CloudApplicationOperation op : operations) {
			op.addApplicationUpdateListener(appUpdateNotifier);
		}
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		try {
			// Run ops in series
			for (CloudApplicationOperation op : operations) {
				op.run(monitor);
			}
		} finally {
			CloudAppInstances appInstances = null;

			// Be sure to catch all errors to properly set the
			// app state to UNKNOWN in case the app or its instances cannot be
			// resolved, as app run state is dependent on instance state
			try {
				appInstances = getCloudApplicationInstances();
			} catch (Throwable e) {
				BootDashActivator.log(e);
			}

			if (appInstances != null) {
				getAppUpdateListener().updateModel(appInstances);
			} else {
				getAppUpdateListener().updateModel(RunState.UNKNOWN);
			}
		}
	}

}
