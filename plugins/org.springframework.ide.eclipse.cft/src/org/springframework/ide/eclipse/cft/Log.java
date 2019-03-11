/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.cft;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

public class Log {

	public static void log(Throwable e) {
		if (ExceptionUtil.isCancelation(e)) {
			// Don't log canceled operations, those aren't real errors.
			return;
		}
		try {
			SpringCFTActivator.getDefault().getLog().log(ExceptionUtil.status(e));
		} catch (NullPointerException npe) {
			// Can happen if errors are trying to be logged during Eclipse's
			// shutdown
			e.printStackTrace();
		}
	}

	public static void logError(String message) {

		try {
			SpringCFTActivator.getDefault().getLog().log(createErrorStatus(message));
		} catch (NullPointerException npe) {
			// Can happen if errors are trying to be logged during Eclipse's
			// shutdown
		}
	}

	public static void logWarning(String message) {

		try {
			SpringCFTActivator.getDefault().getLog().log(createWarningStatus(message));
		} catch (NullPointerException npe) {
			// Can happen if errors are trying to be logged during Eclipse's
			// shutdown
		}
	}

	public static IStatus createWarningStatus(String message) {
		if (message == null) {
			message = "";
		}
		return new Status(IStatus.WARNING, SpringCFTActivator.PLUGIN_ID, message);
	}

	/**
	 * Returns a new <code>IStatus</code> for this plug-in
	 */
	public static IStatus createErrorStatus(String message) {
		if (message == null) {
			message = "";
		}
		return new Status(IStatus.ERROR, SpringCFTActivator.PLUGIN_ID, message);
	}

	/**
	 * Returns a new <code>IStatus</code> for this plug-in
	 */
	public static IStatus createErrorStatus(Exception e) {
		return new Status(IStatus.ERROR, SpringCFTActivator.PLUGIN_ID, e.getMessage(), e);
	}

	public static CoreException asCoreException(String message) {
		return new CoreException(createErrorStatus(message));
	}

	public static CoreException asCoreException(Exception e) {
		return new CoreException(createErrorStatus(e));
	}
}
