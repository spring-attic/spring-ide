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
import org.springframework.ide.eclipse.boot.dash.model.RunState;

/**
 * Runs a list of {@link CloudApplicationOperation} in series
 */
public class CompositeApplicationOperation extends CloudApplicationOperation {

	private List<CloudApplicationOperation> operations;

	private RunState preferredInitialRunState;

	public CompositeApplicationOperation(String opName, CloudFoundryBootDashModel model, String appName,
			List<CloudApplicationOperation> operations, RunState preferredInitialRunState) {
		super(opName, model, appName);
		this.operations = operations;
		this.preferredInitialRunState = preferredInitialRunState;
	}

	public CompositeApplicationOperation(String opName, CloudFoundryBootDashModel model, String appName,
			List<CloudApplicationOperation> operations) {
		this(opName, model, appName, operations, null);
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

			// Can only update the run state if the element exists. It's
			// possible the operation is performing
			// steps where element doesn't yet exist (e.g an operation is
			// creating it)
			if (preferredInitialRunState != null && getDashElement() != null) {
				boolean checkTermination = true;
				this.eventHandler.fireEvent(
						eventFactory.getUpdateRunStateEvent(getDashElement(), preferredInitialRunState),
						checkTermination);
			}

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
