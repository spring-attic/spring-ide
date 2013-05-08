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

import org.eclipse.core.runtime.IStatus;

/**
 * Sets the status of a wizard page in the wizard
 * 
 */
public interface IWizardPageStatusHandler {

	/**
	 * Set the status of the wizard, and optionally trigger a full validation of
	 * the wizard pages.
	 * @param status of the page
	 * @param forceValidation whether the wizard should validate all pages
	 * again.
	 */
	public abstract void setStatus(IStatus status, boolean forceValidation);

}