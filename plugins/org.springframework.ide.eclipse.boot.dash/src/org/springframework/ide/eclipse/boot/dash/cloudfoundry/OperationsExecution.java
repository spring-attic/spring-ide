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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.Operation;

public class OperationsExecution {

	public OperationsExecution() {
	}

	/**
	 * Runs the list of ops in one atomic Job. The scheduling rule of the first
	 * op in the list is used for the Job
	 *
	 * @param ops
	 *            to run in sequence
	 * @param opName
	 *            for all the operations. If null, name of the first op will be
	 *            used
	 */
	public void runAsOneOp(final Operation<?>[] ops, String opName) {
		if (ops == null || ops.length == 0) {
			return;
		}

		String operationName = opName == null ? ops[0].getName() : opName;

		Job job = new Job(operationName) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					for (Operation<?> op : ops) {
						op.run(monitor);
					}
				} catch (Exception e) {
					return BootDashActivator.createErrorStatus(e);
				}
				return Status.OK_STATUS;
			}

		};

		// Use the first scheduling rule for all of the ops
		ISchedulingRule rule = ops[0].getSchedulingRule();
		if (rule != null) {
			job.setRule(rule);
		}

		job.schedule();

	}

	public void runOp(final Operation<?> op) {

		Job job = new Job(op.getName()) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					op.run(monitor);
				} catch (Exception e) {
					return BootDashActivator.createErrorStatus(e);
				}
				return Status.OK_STATUS;
			}

		};

		ISchedulingRule rule = op.getSchedulingRule();
		if (op != null) {
			job.setRule(rule);
		}

		job.schedule();

	}
}
