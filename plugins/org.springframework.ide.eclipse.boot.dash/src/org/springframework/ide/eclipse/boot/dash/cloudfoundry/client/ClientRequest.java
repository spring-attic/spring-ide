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
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.client;

import java.util.concurrent.Callable;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.eclipse.core.runtime.Assert;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;

public abstract class ClientRequest<T> implements Callable<T> {

	protected final CloudFoundryOperations client;

	protected final String requestName;

	private final String appName;

	protected final RequestErrorHandler errorHandler;

	/**
	 *
	 * @param model
	 *            cannot be null.
	 * @param requestName
	 *            user-visible request name that may get logged
	 */
	public ClientRequest(CloudFoundryOperations client, String requestName) {
		this(client, null, requestName, null);
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
	public ClientRequest(CloudFoundryOperations client, String appName, String requestName,
			RequestErrorHandler errorHandler) {
		Assert.isLegal(client != null, "ClientRequest needs a non-null client");
		this.client = client;
		this.appName = appName;
		this.requestName = requestName;
		this.errorHandler = errorHandler == null ? new RequestErrorHandler() : errorHandler;
	}

	/**
	 *
	 * @return Application name that pertains to this request. Null if the
	 *         request is general (e.g. fetching list of spaces, etc..)
	 */
	protected String getAppName() {
		return this.appName;
	}

	public T call() throws Exception {

		try {
			return doRun(this.client);
		} catch (Throwable e) {
			// Some CF/REST exceptions are Throwable so catch Throwable rather
			// than Exception
			if (errorHandler.throwError(e)) {
				throw ExceptionUtil.exception(e);
			}
		}
		return null;
	}

	protected abstract T doRun(CloudFoundryOperations client) throws Exception;
}
