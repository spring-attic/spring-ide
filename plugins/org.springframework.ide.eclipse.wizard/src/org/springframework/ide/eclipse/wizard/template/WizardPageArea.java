/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.springframework.ide.eclipse.wizard.WizardPlugin;

/**
 * A wizard area that generates one status result upon validation for a set of
 * UI controls in that area, and notifies a status handler of any changes. In
 * addition, the area can also notify a status handler whether the area is
 * complete or not. This is then used by the wizard to determine if the page
 * containing the area can complete.
 * 
 */
public abstract class WizardPageArea {

	private final IWizardPageStatusHandler statusHandler;

	private IStatus validationStatus;

	private boolean isAreaComplete = true;

	public WizardPageArea(IWizardPageStatusHandler statusHandler) {
		this.statusHandler = statusHandler;
		statusHandler.addPageArea(this);

	}

	public abstract Control createArea(Composite parent);

	protected void notifyStatusChange() {

		if (statusHandler != null) {
			statusHandler.notifyStatusChange(this);
		}
	}

	/**
	 * By default, it is true, unless explicitly set to false through events or
	 * API
	 * @return
	 */
	public boolean isAreaComplete() {
		return isAreaComplete;
	}

	protected void setAreaComplete(boolean isAreaComplete) {
		this.isAreaComplete = isAreaComplete;
	}

	protected IStatus createStatus(String message, int severity) {
		return new Status(severity, WizardPlugin.PLUGIN_ID, message);
	}

	protected void notifyStatusChange(IStatus status, boolean isAreaComplete) {
		this.isAreaComplete = isAreaComplete;
		validationStatus = status;
		notifyStatusChange();
	}

	protected void notifyStatusChange(IStatus status) {
		validationStatus = status;
		notifyStatusChange();
	}

	/**
	 * 
	 * @param refresh if true, recalculates the validation status. Otherwise
	 * uses the cached value.
	 * @return IStatus of the controls in the area.
	 */
	public IStatus getValidationStatus(boolean refresh) {
		if (refresh) {
			validationStatus = validateArea();
		}
		return validationStatus;
	}

	public IStatus getValidationStatus() {
		return getValidationStatus(false);
	}

	/**
	 * 
	 * @return IStatus for the validation of all the controls in the area
	 */
	abstract protected IStatus validateArea();

	public void refreshUI() {
		// Optional call back to refresh the UI based on changes in the wizard
	}

}
