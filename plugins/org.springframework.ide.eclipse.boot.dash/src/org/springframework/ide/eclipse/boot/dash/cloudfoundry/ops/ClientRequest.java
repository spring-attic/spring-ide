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

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.eclipse.core.runtime.CoreException;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudFoundryBootDashModel;

public abstract class ClientRequest<T> {

	protected final CloudFoundryBootDashModel model;

	protected final String requestName;

	private final String appName;

	private RequestErrorListener listener;

	/**
	 *
	 * @param model
	 *            cannot be null.
	 * @param requestName
	 *            user-visible request name that may get logged
	 */
	public ClientRequest(CloudFoundryBootDashModel model, String requestName) {
		this(model, null, requestName);
	}

	/**
	 *
	 * @return Application name that pertains to this request. Null if the
	 *         request is general (e.g. fetching list of spaces, etc..)
	 */
	protected String getAppName() {
		return this.appName;
	}

	/**
	 *
	 * @param model
	 *            cannot be null.
	 * @param appName
	 *            Optional application name if a request is performed on an
	 *            application. Null if it is a general request (e.g. fetching
	 *            list of all apps)
	 * @param requestName
	 *            user-visible request name that may get logged
	 */
	public ClientRequest(CloudFoundryBootDashModel model, String appName, String requestName) {
		this.model = model;
		this.appName = appName;
		this.requestName = requestName;
	}

	public void addRequestListener(RequestErrorListener listener) {
		this.listener = listener;
	}

	/**
	 *
	 * @return should never be null.
	 */
	protected RequestErrorListener getRequestListener() {
		if (this.listener == null) {
			this.listener = new RequestErrorListener(this);
		}
		return this.listener;
	}

	public T run() throws Exception {

		try {
			return runWithReattempt();
		} catch (Throwable e) {
			if (e instanceof Exception) {
				if (!listener.shouldIgnoreError((Exception) e)) {
					throw (Exception) e;
				}
			} else {
				throw new CoreException(BootDashActivator.createErrorStatus(e));
			}
		}
		return null;
	}

	/**
	 * Runs the request. Will attempt the request again on error if the listener
	 * determines a reattempt is needed
	 *
	 * @return value of request. Null if request does not generate a result
	 * @throws Throwable
	 *             if error occurred during request
	 */
	protected T runWithReattempt() throws Throwable {
		try {
			return getClientAndRunRequest();
		} catch (Exception e) {

			RequestErrorListener listener = getRequestListener();
			if (listener.retryOnError(e)) {
				return getClientAndRunRequest();
			} else {
				throw e;
			}
		}
	}

	private T getClientAndRunRequest() throws Exception {
		CloudFoundryOperations client = model.getCloudTarget().getClient();
		return doRun(client);
	}

	protected abstract T doRun(CloudFoundryOperations client) throws Exception;
}
