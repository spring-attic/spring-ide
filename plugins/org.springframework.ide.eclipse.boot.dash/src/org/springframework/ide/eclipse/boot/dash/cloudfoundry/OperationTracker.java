/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
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
import org.eclipse.core.runtime.OperationCanceledException;
import org.springframework.ide.eclipse.boot.dash.livexp.LiveCounter;
import org.springframework.ide.eclipse.boot.dash.util.CancelationTokens.CancelationToken;
import org.springsource.ide.eclipse.commons.livexp.core.LiveVariable;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

/**
 * Keeps track of whether a certain 'operation' is currently in progress.
 *
 * @author Kris De Volder
 */
public class OperationTracker {

	private LiveVariable<Throwable> error;

	/**
	 * Counter that keeps a count of the number of 'nested' operations are
	 * currently in progress (i.e. operation was started but not yet ended)
	 */
	public final LiveCounter inProgress = new LiveCounter();

	public OperationTracker(LiveVariable<Throwable> error) {
		this.error = error;
	}

	public void start() {
		setError(null);
		inProgress.increment();
	}

	private void setError(Throwable e) {
		error.setValue(e);
	}

	public void end(Throwable error, CancelationToken cancelationToken, IProgressMonitor monitor) throws Exception {
		int level = inProgress.decrement();
		if (cancelationToken.isCanceled() || monitor.isCanceled()) {
			//Avoid setting error results for canceled operation. If an op is canceled
			// its errors should simply be ignored.
			throw new OperationCanceledException();
		}
		if (level==0 && !(error instanceof OperationCanceledException)) {
			setError(error);
		}
		if (error != null) {
			throw ExceptionUtil.exception(error);
		}
	}



}
