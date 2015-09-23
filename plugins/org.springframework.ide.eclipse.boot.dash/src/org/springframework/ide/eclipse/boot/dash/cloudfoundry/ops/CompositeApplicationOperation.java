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

	public CompositeApplicationOperation(String opName, CloudFoundryBootDashModel model, String appName,
			List<CloudApplicationOperation> operations) {
		super(opName, model, appName);
		this.operations = operations;
	}

	public CompositeApplicationOperation(CloudApplicationOperation enclosedOp) {
		super(enclosedOp.getName(), enclosedOp.model, enclosedOp.appName);

		this.operations = new ArrayList<CloudApplicationOperation>();
		this.operations.add(enclosedOp);
		setSchedulingRule(enclosedOp.getSchedulingRule());
	}

	@Override
	public void addOperationEventHandler(ApplicationOperationEventHandler eventHandler) {
		super.addOperationEventHandler(eventHandler);
		for (CloudApplicationOperation op : operations) {
			op.addOperationEventHandler(eventHandler);
		}
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		try {
			// Run ops in series
			resetAndShowConsole();

			for (CloudApplicationOperation op : operations) {
				op.run(monitor);
			}

		} catch (Throwable t) {
			if (!(t instanceof OperationCanceledException)) {
				eventHandler.onError(appName, t);
			}
			throw t instanceof Exception ? (Exception) t : new CoreException(BootDashActivator.createErrorStatus(t));
		}
	}
}
