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

import org.cloudfoundry.client.lib.CloudFoundryException;

public class CloudErrors {

	/**
	 * check 403 error due to invalid credentials or access token errors.
	 *
	 * @param t
	 * @return true if 403. False otherwise
	 */
	public static boolean isAccessException(Throwable t) {
		return isCloudError(t, "403");
	}

	/**
	 * check 404 error. For example, application does not exist
	 *
	 * @param t
	 * @return true if 404 error. False otherwise
	 */
	public static boolean isNotFoundException(Throwable t) {
		return isCloudError(t, "404");
	}

	public static boolean isCloudError(Throwable t, String error) {

		CloudFoundryException cloudError = getCloudFoundryError(t);

		if (cloudError != null) {
			String message = cloudError.getMessage();
			if (message == null || message.trim().length() == 0) {
				message = cloudError.getDescription();
			}
			if (message != null) {
				return message.contains(error);
			}
		}

		return false;
	}

	protected static CloudFoundryException getCloudFoundryError(Throwable t) {
		if (t == null) {
			return null;
		}
		CloudFoundryException cloudException = null;
		if (t instanceof CloudFoundryException) {
			cloudException = (CloudFoundryException) t;
		} else {
			Throwable cause = t.getCause();
			if (cause instanceof CloudFoundryException) {
				cloudException = (CloudFoundryException) cause;
			}
		}
		return cloudException;
	}
}
