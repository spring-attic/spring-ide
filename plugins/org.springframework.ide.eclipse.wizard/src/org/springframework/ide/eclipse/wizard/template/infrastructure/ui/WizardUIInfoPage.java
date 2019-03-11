/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template.infrastructure.ui;

/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class WizardUIInfoPage {
	
	private int order;
	
	private String description;
	
	public int getOrder() {
		return order;
	}
	
	public String getDescription() {
		return description;
	}
	
	public static WizardUIInfoPage getDefaultPage(int order) {
		WizardUIInfoPage page = new WizardUIInfoPage();
		page.order = order;
		page.description = "";
		return page;
	}
	
}
