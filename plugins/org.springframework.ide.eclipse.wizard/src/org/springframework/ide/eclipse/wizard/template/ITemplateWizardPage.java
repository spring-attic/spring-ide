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
package org.springframework.ide.eclipse.wizard.template;

import org.eclipse.jface.wizard.IWizardPage;

/**
 * @author Terry Denney
 */
public interface ITemplateWizardPage extends IWizardPage {

	public String[] getErrorMessages();

	public String[] getMessages();

	public void setNextPage(IWizardPage page);

	public void updateMessage();

}
