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

/**
 * Sets the status of a wizard page in the wizard based on a notification from
 * wizard area
 * 
 */
public interface IWizardPageStatusHandler {

	/**
	 * Set the status of the wizard, and optionally trigger a full validation of
	 * the wizard pages.
	 * @param status of the page
	 */
	public abstract void notifyStatusChange(WizardPageArea area);

	/**
	 * 
	 * @param area to be added which notify the handler observer of changes
	 */
	public void addPageArea(WizardPageArea area);
}