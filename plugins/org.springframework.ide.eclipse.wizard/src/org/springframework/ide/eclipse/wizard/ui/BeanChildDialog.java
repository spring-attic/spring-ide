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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
import org.springframework.ide.eclipse.wizard.core.WizardContentAssistConverter;


/**
 * Abstract parent dialog for displaying and editing bean property or
 * constructor arg
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public abstract class BeanChildDialog extends Dialog {

	protected IDOMElement element;

	protected BeanWizard wizard;

	private CLabel messageLabel;

	private final boolean isNew;

	private final String title;

	protected BeanChildDialog(Shell parentShell, BeanWizard wizard, IDOMElement element, String title, boolean isNew) {
		super(parentShell);

		this.wizard = wizard;
		this.element = element;
		this.title = title;
		this.isNew = isNew;
	}

	protected void addListener(final Text text, final String attributeName) {
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateAttribute(attributeName, text.getText());
			}
		});
	}

	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == Dialog.CANCEL) {
			resetAttributes();
		}
		super.buttonPressed(buttonId);
	}

	protected abstract void createAttributes(Composite container);

	@Override
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);

		if (!isNew) {
			validateAttributes();
		}

		return control;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText(title);

		Composite container = new Composite(parent, SWT.NONE);
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.widthHint = 500;
		container.setLayoutData(layoutData);
		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 10;
		layout.marginWidth = 10;
		container.setLayout(layout);

		messageLabel = new CLabel(container, SWT.NONE);
		messageLabel.setText(getDefaultMessage());
		GridData descriptionData = new GridData(SWT.FILL, SWT.FILL, true, false);
		descriptionData.horizontalSpan = 2;
		descriptionData.verticalIndent = 5;
		messageLabel.setLayoutData(descriptionData);

		createAttributes(container);

		return container;
	}

	protected abstract boolean getCanFinish();

	protected abstract String getDefaultMessage();

	protected abstract String getMessage();

	protected void resetAttribute(String attributeName, String originalValue) {
		if (originalValue != null) {
			element.setAttribute(attributeName, originalValue);
		}
		else {
			element.removeAttribute(attributeName);
		}
	}

	protected abstract void resetAttributes();

	protected void updateAttribute(String attributeName, String value) {
		if (value.length() > 0) {
			element.setAttribute(attributeName, value);
		}
		else {
			element.removeAttribute(attributeName);
		}

		validateAttribute(attributeName, value);
	}

	private void updateButton() {
		getButton(Dialog.OK).setEnabled(getCanFinish());
	}

	protected void updateMessage() {
		String message = getMessage();
		if (message != null) {
			if (BeanWizard.getIgnoreError()) {
				messageLabel.setImage(JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_WARNING));
			}
			else {
				messageLabel.setImage(JFaceResources.getImage(Dialog.DLG_IMG_MESSAGE_ERROR));
			}
			messageLabel.setText(message);
		}
		else {
			messageLabel.setImage(null);
			messageLabel.setText(getDefaultMessage());
		}

		messageLabel.redraw();

	}

	protected void validateAttribute(String attributeName, String value) {
		WizardContentAssistConverter contentAssistConverter = new WizardContentAssistConverter(element, element
				.getAttributeNode(attributeName), wizard.getBeanFile(), wizard.getOriginalDocument());

		validateAttribute(attributeName, value, contentAssistConverter);
		updateButton();
	}

	protected abstract void validateAttribute(String attributeName, String value,
			WizardContentAssistConverter contentAssistConverter);

	protected abstract void validateAttributes();
}
