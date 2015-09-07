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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;

/**
 * Runs {@link CloudApplicationOperation} in series
 */
public class CompositeApplicationOperation extends CloudApplicationOperation {

	private List<CloudApplicationOperation> operations;

	private boolean resetConsole;

	public CompositeApplicationOperation(String opName, CloudFoundryBootDashModel model, String appName,
			List<CloudApplicationOperation> operations, boolean resetConsole) {
		super(opName, model, appName);
		this.operations = operations;
		this.resetConsole = resetConsole;
	}

	public CompositeApplicationOperation(CloudApplicationOperation enclosedOp, boolean resetConsole) {
		super(enclosedOp.getName(), enclosedOp.model, enclosedOp.appName);

		this.operations = new ArrayList<CloudApplicationOperation>();
		this.operations.add(enclosedOp);
		this.resetConsole = resetConsole;
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
			if (resetConsole) {
				resetAndShowConsole();
			}
			for (CloudApplicationOperation op : operations) {
				op.run(monitor);
			}
		} catch (Throwable t) {
			if (!(t instanceof OperationCanceledException)) {
				getAppUpdateListener().onError(t);
			}
			throw t instanceof Exception ? (Exception) t : new CoreException(BootDashActivator.createErrorStatus(t));
		}
	}
}
