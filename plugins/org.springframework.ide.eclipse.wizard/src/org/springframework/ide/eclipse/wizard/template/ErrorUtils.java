/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;

public class ErrorUtils {

	private static int WIZARD_ERROR_MAX_CHAR_LENGTH = 50;

	private ErrorUtils() {
		// Utility class
	}

	public static boolean isWithinWizardErrorMaximum(String errorMessage) {
		return errorMessage.length() <= WIZARD_ERROR_MAX_CHAR_LENGTH;
	}

	/**
	 * Generates an error message given an error message prefix and an
	 * exception.
	 * <p/>
	 * If the error message prefix is not null and an error message can be
	 * resolved from the exception, the error message will be composed of both
	 * parts.
	 * <p/>
	 * If the error message prefix is not null, but no error message was
	 * resolved from the exception, then only the prefix is returned.
	 * <p/>
	 * If the error message prefix is null, but an error is resolved from the
	 * exception, the exception error is returned.
	 * <p/>
	 * Otherwise null is returned
	 * @param errorMessagePrefix
	 * @param e exception that may contain an actual error message
	 * @return resolved error message, or null if it wasn't resolved.
	 */
	public static String getErrorMessage(String errorMessagePrefix, Exception e) {

		String fullError = null;
		String exceptionError = null;

		if (e instanceof InvocationTargetException) {
			exceptionError = getErrorMessage((InvocationTargetException) e);
		}
		else if (e instanceof CoreException) {
			exceptionError = getErrorMessage((CoreException) e);
		}
		else if (e != null) {
			exceptionError = e.getMessage();
		}

		if (exceptionError != null) {
			exceptionError = exceptionError.trim();
			if (exceptionError.length() > 0) {

				if (errorMessagePrefix != null) {
					fullError = errorMessagePrefix + ". Cause: " + exceptionError;
				}
				else {
					fullError = exceptionError;
				}
			}
		}

		if (fullError == null) {
			fullError = errorMessagePrefix;
		}

		return fullError;

	}

	public static String getErrorMessage(CoreException e) {
		if (e == null) {
			return null;
		}
		return getErrorMessage(e.getStatus());
	}

	public static String getErrorMessage(InvocationTargetException e) {

		if (e.getTargetException() != null) {
			if (e.getTargetException() instanceof CoreException) {
				return getErrorMessage((CoreException) e.getTargetException());
			}
			else {
				return e.getTargetException().getMessage();
			}
		}
		else {
			return e.getMessage();
		}
	}

	public static String getErrorMessage(IStatus status) {
		String error = null;
		if (status instanceof MultiStatus) {
			// Resolve the first error message;
			IStatus[] children = ((MultiStatus) status).getChildren();
			if (children != null) {
				for (IStatus child : children) {
					error = getErrorMessage(child);
					if (error != null) {
						break;
					}
				}
			}
		}
		else if (status.getSeverity() == IStatus.ERROR) {
			error = status.getMessage();
		}
		return error;
	}

}
