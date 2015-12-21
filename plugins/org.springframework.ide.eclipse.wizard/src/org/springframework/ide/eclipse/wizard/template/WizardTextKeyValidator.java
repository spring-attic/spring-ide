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

import java.util.regex.Pattern;

import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.wizard.template.infrastructure.ui.WizardUIInfoElement;


/**
 * @author Terry Denney
 */
public class WizardTextKeyValidator extends KeyAdapter {

	private final int index;

	private final WizardUIInfoElement element;

	private final Text text;

	private final ITemplateWizardPage page;

	public WizardTextKeyValidator(int index, WizardUIInfoElement element, Text text, ITemplateWizardPage page) {
		this.index = index;
		this.element = element;
		this.text = text;
		this.page = page;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		validate();
		page.updateMessage();
	}

	public void validate() {
		String[] errorMessages = page.getErrorMessages();
		String[] messages = page.getMessages();

		errorMessages[index] = null;
		messages[index] = null;
		String textEntered = text.getText();
		String errorMsg = element.getErrorMessage();
		if (textEntered == null || textEntered.length() == 0) {
			if (element.getRequired()) {
				if (errorMsg != null && errorMsg.length() > 0) {
					messages[index] = errorMsg;
				}
				else {
					messages[index] = "Enter a  " + element.getName() + "."; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		else {
			String regex = element.getPattern();
			if (regex != null) {
				if (!Pattern.matches(regex, textEntered)) {
					if (errorMsg != null && errorMsg.length() > 0) {
						errorMessages[index] = errorMsg;
					}
					else {
						errorMessages[index] = "Input for field " + element.getName() + " does not match the regex " + regex + "."; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					}
				}
			}
		}
	}
}
