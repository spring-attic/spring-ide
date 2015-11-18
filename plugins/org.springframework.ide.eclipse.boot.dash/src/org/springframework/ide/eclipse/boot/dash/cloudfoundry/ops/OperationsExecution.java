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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

public class OperationsExecution {

	private final UserInteractions ui;

	public OperationsExecution(UserInteractions ui) {
		this.ui = ui;
	}

	public void runOpAsynch(final Operation<?> op) {
		if (op!=null) {
			Job job = new Job(op.getName()) {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						op.run(monitor);
					} catch (Exception e) {
						if (!(e instanceof OperationCanceledException)) {
							if (ui != null) {
								String message = e.getMessage() != null && e.getMessage().trim().length() > 0
										? e.getMessage()
										: "Error type: " + e.getClass().getName()
												+ ". Check Error Log view for further details.";
								ui.errorPopup("Operation Failure", message);

							}
							BootDashActivator.log(e);
						}
					}
					// Only return OK status to avoid a second error dialogue
					// appearing, which is opened by Eclipse when a job returns
					// error status.
					return Status.OK_STATUS;
				}

			};

			ISchedulingRule rule = op.getSchedulingRule();
			if (rule != null) {
				job.setRule(rule);
			}

			job.setPriority(Job.INTERACTIVE);
			job.schedule();
		}
	}
}
