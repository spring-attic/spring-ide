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
package org.springframework.ide.eclipse.boot.dash.views;

import org.springframework.ide.eclipse.boot.dash.cloudfoundry.console.LogType;
import org.springframework.ide.eclipse.boot.dash.model.BootDashElement;
import org.springframework.ide.eclipse.boot.dash.model.BootDashModel;

/**
 * Console manager for elements in a {@link BootDashModel}.
 * <p/>
 * Each {@link BootDashModel} should have its own console manager.
 *
 */
public abstract class BootDashModelConsoleManager {

	/**
	 * Opens the console for the given element.
	 *
	 * @param element
	 * @throws Exception
	 *             if failure occurred while opening console (e.g. failed to
	 *             create console, underlying process is terminated, etc..)
	 */
	public abstract void showConsole(BootDashElement element) throws Exception;

	/**
	 * Write a message to the console for the associated element.
	 *
	 * @param element
	 * @param message
	 */
	public abstract void writeToConsole(BootDashElement element, String message, LogType type) throws Exception;

	/**
	 * API for when element is not yet available.
	 * <p/>
	 * For cases where application is being prepared for deployment, and
	 * messages regarding the pre-deployment should be shown to the user,but the
	 * element is not yet created in the model's associated run target.
	 * <p/>
	 * If the model only supports writing to console on existing elements, this
	 * method can be ignored
	 *
	 * @param appName
	 * @param message
	 */
	public void writeToConsole(String appName, String message, LogType type) throws Exception {

	}

	public abstract void stopConsole(String appName) throws Exception;
}
