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
package org.springframework.ide.eclipse.wizard.listeners;

import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.springframework.ide.eclipse.wizard.ui.AbstractBeanWizardPage;
import org.springframework.ide.eclipse.wizard.ui.BeanPropertiesWizardPage;
import org.springframework.ide.eclipse.wizard.ui.BeanWizard;


/**
 * Listener for invoking properties validation on entering or leaving the bean properties page.
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
public class PropertiesPageShownListener implements IPageChangedListener {
	
	private BeanWizard wizard;
	
	public PropertiesPageShownListener(BeanWizard wizard) {
		this.wizard = wizard;
	}

	public void pageChanged(PageChangedEvent event) {
		Object selectedPage = event.getSelectedPage();
		if (selectedPage instanceof AbstractBeanWizardPage) {
			((AbstractBeanWizardPage) selectedPage).updateMessage();
		}
		if (selectedPage instanceof BeanPropertiesWizardPage) {
			((BeanPropertiesWizardPage) selectedPage).validateElements();
		} else {
			wizard.getPropertiesPage().resetProblemCounter();
		}
	}

}
