/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.debug;

import java.util.Map;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudDashElement;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ops.Operation;
import org.springframework.ide.eclipse.boot.dash.model.UserInteractions;

/**
 * Abstract class that must be implemented to add debug support to a CF application.
 *
 * @author Kris De Volder
 */
public abstract class DebugSupport {

	protected final CloudDashElement app;

	public DebugSupport(CloudDashElement app) {
		this.app = app;
	}

	/**
	 * Determine whether debugging can be supported (using the strategy impemented by this DebugSupport instance)
	 */
	public abstract boolean isSupported();

	/**
	 * If isSupported returns false than the support strategy may also return an explanation why the strategy is not
	 * supported (e.g. PCF version too old, SSH support disabled etc.)
	 */
	public abstract String getNotSupportedMessage();

	/**
	 * Creates operation that does whatever is needed to get debugger connected to the targetted app.
	 */
	public abstract Operation<?> createOperation(String opName, UserInteractions ui);

	/**
	 * Called to allow debug support to muck around with environment variables so that it can
	 * do things like add debugging options to 'JAVA_OPTS'.
	 */
	public abstract void setupEnvVars(Map<String, String> environmentVariables);

	/**
	 * Like setupEnvVars, but called when debugging is disabled. The debug strategy should try to
	 * undo any changes it made to the env vars to enable debugging.
	 */
	public abstract void clearEnvVars(Map<String, String> environmentVariables);

}
