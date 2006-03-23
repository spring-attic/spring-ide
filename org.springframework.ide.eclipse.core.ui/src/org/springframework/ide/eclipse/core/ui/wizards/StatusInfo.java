/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ide.eclipse.core.ui.wizards;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.util.Assert;
import org.springframework.ide.eclipse.core.ui.SpringCoreUIPlugin;

/**
 * A settable IStatus.<br>
 * Can be an error, warning, info or ok.<br>
 * For error, info and warning states, a message describes the problem.
 * 
 * @author Pierre-Antoine Grégoire
 */
public class StatusInfo implements IStatus {

	public static final IStatus OK_STATUS = new StatusInfo();

	private String fStatusMessage;

	private int fSeverity;

	/**
	 * Creates a status set to OK (no message)
	 */
	public StatusInfo() {
		this(OK, null);
	}

	/**
	 * Creates a status .
	 * 
	 * @param severity
	 *            The status severity: ERROR, WARNING, INFO and OK.
	 * @param message
	 *            The message of the status. Applies only for ERROR, WARNING and INFO.
	 */
	public StatusInfo(int severity, String message) {
		fStatusMessage = message;
		fSeverity = severity;
	}

	/**
	 * Returns if the status' severity is OK.
	 */
	public boolean isOK() {
		return fSeverity == IStatus.OK;
	}

	/**
	 * Returns if the status' severity is WARNING.
	 */
	public boolean isWarning() {
		return fSeverity == IStatus.WARNING;
	}

	/**
	 * Returns if the status' severity is INFO.
	 */
	public boolean isInfo() {
		return fSeverity == IStatus.INFO;
	}

	/**
	 * Returns if the status' severity is ERROR.
	 */
	public boolean isError() {
		return fSeverity == IStatus.ERROR;
	}

	/**
	 * @see IStatus#getMessage
	 */
	public String getMessage() {
		return fStatusMessage;
	}

	/**
	 * Sets the status to ERROR.
	 * 
	 * @param errorMessage
	 *            The error message (can be empty, but not null)
	 */
	public void setError(String errorMessage) {
		Assert.isNotNull(errorMessage);
		fStatusMessage = errorMessage;
		fSeverity = IStatus.ERROR;
	}

	/**
	 * Sets the status to WARNING.
	 * 
	 * @param warningMessage
	 *            The warning message (can be empty, but not null)
	 */
	public void setWarning(String warningMessage) {
		Assert.isNotNull(warningMessage);
		fStatusMessage = warningMessage;
		fSeverity = IStatus.WARNING;
	}

	/**
	 * Sets the status to INFO.
	 * 
	 * @param infoMessage
	 *            The info message (can be empty, but not null)
	 */
	public void setInfo(String infoMessage) {
		Assert.isNotNull(infoMessage);
		fStatusMessage = infoMessage;
		fSeverity = IStatus.INFO;
	}

	/**
	 * Sets the status to OK.
	 */
	public void setOK() {
		fStatusMessage = null;
		fSeverity = IStatus.OK;
	}

	/*
	 * @see IStatus#matches(int)
	 */
	public boolean matches(int severityMask) {
		return (fSeverity & severityMask) != 0;
	}

	/**
	 * Returns always <code>false</code>.
	 * 
	 * @see IStatus#isMultiStatus()
	 */
	public boolean isMultiStatus() {
		return false;
	}

	/*
	 * @see IStatus#getSeverity()
	 */
	public int getSeverity() {
		return fSeverity;
	}

	/*
	 * @see IStatus#getPlugin()
	 */
	public String getPlugin() {
		return SpringCoreUIPlugin.PLUGIN_ID;
	}

	/**
	 * Returns always <code>null</code>.
	 * 
	 * @see IStatus#getException()
	 */
	public Throwable getException() {
		return null;
	}

	/**
	 * Returns always the error severity.
	 * 
	 * @see IStatus#getCode()
	 */
	public int getCode() {
		return fSeverity;
	}

	/**
	 * Returns always <code>null</code>.
	 * 
	 * @see IStatus#getChildren()
	 */
	public IStatus[] getChildren() {
		return new IStatus[0];
	}

}
