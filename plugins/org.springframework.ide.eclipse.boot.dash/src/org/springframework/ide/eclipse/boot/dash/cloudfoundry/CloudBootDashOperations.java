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
import org.springframework.ide.eclipse.boot.dash.model.BootDashModelContext;
import org.springframework.ide.eclipse.boot.dash.model.Operation;

public class CloudBootDashOperations {

	private final BootDashModelContext context;

	public CloudBootDashOperations(BootDashModelContext context) {
		this.context = context;
	}

	public static final ISchedulingRule REFRESH_RULE = new ISchedulingRule() {

		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == REFRESH_RULE;
		}

		@Override
		public boolean contains(ISchedulingRule rule) {
			return false;
		}
	};

	public static final ISchedulingRule DEPLOYMENT_RULE = new ISchedulingRule() {

		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			// Allow parallel deployments but not when refresh is running
			return rule == REFRESH_RULE;
		}

		@Override
		public boolean contains(ISchedulingRule rule) {
			return false;
		}
	};

	public BootDashModelContext getModelContext() {
		return context;
	}


	public void runDeploymentOperation(final Operation<?> op) {
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

//		job.setRule(DEPLOYMENT_RULE);
		job.schedule();
	}

	public void runRefreshOperation(final Operation<?> op) {
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

//		job.setRule(REFRESH_RULE);
		job.schedule();
	}

	public void runOp(final CloudApplicationDashOperation op)
			throws Exception {

		Job job = new Job(op.getName()) {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				return Status.OK_STATUS;
			}

		};

		job.schedule();

	}
}
