/*******************************************************************************
 * Copyright (c) 2015, 2016 Pivotal, Inc.
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
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

/**
 * Runs a list of {@link CloudApplicationOperation} in series
 */
public class CompositeApplicationOperation extends CloudApplicationOperation {

	private List<Operation<?>> operations;

	private boolean resetConsole;

	public CompositeApplicationOperation(String opName, CloudFoundryBootDashModel model, String appName,
			List<Operation<?>> operations, boolean resetConsole) {
		super(opName, model, appName);
		this.operations = operations;
		this.resetConsole = resetConsole;
	}

	public CompositeApplicationOperation(String opName, CloudFoundryBootDashModel model, String appName,
			List<Operation<?>> operations) {
		this(opName, model, appName, operations, true);
	}

	public CompositeApplicationOperation(CloudApplicationOperation enclosedOp) {
		super(enclosedOp.getName(), enclosedOp.model, enclosedOp.appName);

		this.operations = new ArrayList<Operation<?>>();
		this.operations.add(enclosedOp);
		setSchedulingRule(enclosedOp.getSchedulingRule());
	}

	@Override
	protected void doCloudOp(IProgressMonitor monitor) throws Exception, OperationCanceledException {
		try {

			// Can only update the run state if the element exists. It's
			// possible the operation is performing
			// steps where element doesn't yet exist (e.g an operation is
			// creating it)
			if (getDashElement() != null) {
				checkTerminationRequested(monitor);
			}

			// Run ops in series
			if (resetConsole) {
				resetAndShowConsole();
			}

			for (Operation<?> op : operations) {
				op.run(monitor);
			}

		} catch (Throwable t) {
			throw ExceptionUtil.exception(t);
		}
	}
}
