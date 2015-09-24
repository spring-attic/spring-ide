/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.livexp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springsource.ide.eclipse.commons.livexp.core.LiveExpression;

/**
 * Like a LiveExpression but ensures that its refresh method is always called
 * in a background job.
 * <p>
 * With a regular LiveExp the refresh will be called on the same thread
 * as the change event it reacts to. So in general you have little control
 * over what thread that might be.
 * <p>
 * {@link AsyncLiveExpression} is when the 'compute' method called during refresh
 * does something that you might not want to just execute, for example, on the UI thread.
 * <p>
 * It is also useful in that when refreshes might be 'lengthy' operations, bursty events
 * triggering refreshes will only causes limited refreshes as only a single Job is
 * being scheduled and rescheduled.
 *
 * @author Kris De Volder
 */
public abstract class AsyncLiveExpression<T> extends LiveExpression<T> {

	private Job refreshJob;
	private long refreshDelay = 0;

	public AsyncLiveExpression(T initialValue) {
		super(initialValue);
		refreshJob = new Job("AsyncLiveExpression refresh") {
			protected IStatus run(IProgressMonitor monitor) {
				syncRefresh();
				return Status.OK_STATUS;
			};
		};
	}

	@Override
	public void refresh() {
		refreshJob.schedule(refreshDelay);
	}

	private void syncRefresh() {
		super.refresh();
	}

	public long getRefreshDelay() {
		return refreshDelay;
	}

	public void setRefreshDelay(long refreshDelay) {
		this.refreshDelay = refreshDelay;
	}

}
