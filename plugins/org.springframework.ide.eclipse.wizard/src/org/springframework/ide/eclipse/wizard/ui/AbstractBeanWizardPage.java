/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.ui;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.springframework.ide.eclipse.wizard.Messages;


/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public abstract class AbstractBeanWizardPage extends WizardPage {

	protected BeanWizard wizard;
	
	protected AbstractBeanWizardPage(String pageName, BeanWizard wizard) {
		super(pageName);
		this.wizard = wizard;
	}
	
	public abstract void updateMessage();

	protected void setDialogMessage(String message, boolean canIgnore) {
		if (BeanWizard.getIgnoreError()) {
			if (canIgnore) {
				setMessage(message, IMessageProvider.WARNING);
			} else {
				setMessage(message, IMessageProvider.ERROR);
			}
		} else {
			if (canIgnore) {
				message += " " + Messages.getString("NewBeanWizardPage.SELECT_IGNORE_ERROR"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			setMessage(message, IMessageProvider.ERROR); //$NON-NLS-1$
		}
	}
	
	protected void setDialogMessage(String message) {
		setDialogMessage(message, true);
	}
	
}
