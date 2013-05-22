/*******************************************************************************
 *  Copyright (c) 2012, 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.wizard.template.infrastructure.ui.WizardUIInfoElement;

/**
 * 
 * Given a Spring project template, this page will generate the UI for the
 * template properties and prompt the user for any template values. This page
 * only gets opened after a user selects a template from the New Spring Project
 * wizard
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class NewTemplateWizardPage extends WizardPage implements ITemplateWizardPage {

	private IWizardPage nextPage;

	private final TemplateInputCollector inputHandler;

	private final String[] errorMessages;

	private final String[] messages;

	private final Set<WizardTextKeyValidator> validators;

	private static final String CONTROL_DATA_KEY = "org.springframework.ide.eclipse.wizard.template.controldatakey";

	private static final String DEFAULT_DESCRIPTION = Messages.getString("TemplateWizardPage.DEFAULT_DESCRIPTION"); //$NON-NLS-1$

	protected NewTemplateWizardPage(String pageTitle, TemplateInputCollector inputHandler, ImageDescriptor icon) {
		super("Template Wizard Page"); //$NON-NLS-1$
		this.inputHandler = inputHandler;

		List<WizardUIInfoElement> elements = inputHandler.getInfoElements();

		this.errorMessages = new String[elements.size()];
		this.messages = new String[elements.size()];
		this.validators = new HashSet<WizardTextKeyValidator>();

		setTitle(pageTitle);
		setDescription(DEFAULT_DESCRIPTION);
		setImageDescriptor(icon);
	}

	@Override
	public boolean canFlipToNextPage() {
		if (hasErrors()) {
			return false;
		}
		return nextPage != null;
	}

	public TemplateInputCollector getInputHandler() {
		return inputHandler;
	}

	public void createControl(Composite parent) {

		Composite control = new Composite(parent, SWT.NONE);
		GridLayout controlLayout = new GridLayout();
		controlLayout.verticalSpacing = 10;
		control.setLayout(controlLayout);

		Composite container = new Composite(control, SWT.NONE);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		List<WizardUIInfoElement> elements = inputHandler.getInfoElements();

		for (int i = 0; i < elements.size(); i++) {
			final WizardUIInfoElement element = elements.get(i);
			String description = null;
			if (element.getRequired()) {
				description = element.getDescription() + "*"; //$NON-NLS-1$
			}
			else {
				description = element.getDescription();
			}

			boolean booleanPrompt = element.getType() == Boolean.class;
			String defaultValue = element.getDefaultValue();

			if (booleanPrompt) {
				Composite buttonContainer = new Composite(container, SWT.NONE);
				buttonContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
				GridLayout layout = new GridLayout(2, false);
				layout.horizontalSpacing = 10;
				buttonContainer.setLayout(layout);

				Label label = new Label(buttonContainer, SWT.NONE);
				label.setText(description);
				label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

				final Button button = new Button(buttonContainer, SWT.CHECK);
				button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
				if (i == 0) {
					button.setFocus();
				}
				button.setData(CONTROL_DATA_KEY, element);

				button.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						WizardUIInfoElement element = (WizardUIInfoElement) button.getData(CONTROL_DATA_KEY);
						if (element != null) {
							inputHandler.updateInput(element.getName(), button.getSelection());
						}
					}
				});

				if (defaultValue != null && defaultValue.equals("true")) {
					button.setSelection(true);
				}
				else if (defaultValue != null && defaultValue.equals("false")) {
					button.setSelection(false);
				}

			}
			else {
				Label descriptionLabel = new Label(container, SWT.NONE);
				descriptionLabel.setText(description);
				descriptionLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

				final Text text = new Text(container, SWT.BORDER);
				text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
				text.setEditable(true);

				text.setData(CONTROL_DATA_KEY, element);

				if (defaultValue != null && defaultValue.length() > 0) {
					text.setText(defaultValue);
					inputHandler.updateInput(element.getName(), defaultValue);
				}

				WizardTextKeyValidator validator = new WizardTextKeyValidator(i, element, text, this) {

					@Override
					public void keyReleased(KeyEvent e) {
						WizardUIInfoElement element = (WizardUIInfoElement) text.getData(CONTROL_DATA_KEY);
						if (element != null) {
							inputHandler.updateInput(element.getName(), text.getText());
						}
						super.keyReleased(e);
					}
				};

				validator.validate();

				validators.add(validator);
				text.addKeyListener(validator);

				if (i == 0) {
					text.setFocus();
				}
			}
		}

		setControl(control);
		updateMessage();

		setPageComplete(validatePage());
	}

	public String[] getErrorMessages() {
		return errorMessages;
	}

	public String[] getMessages() {
		return messages;
	}

	@Override
	public IWizardPage getNextPage() {
		if (nextPage != null) {
			return nextPage;
		}

		return super.getNextPage();
	}

	private boolean hasErrors() {

		for (String errorMessage : errorMessages) {
			if (errorMessage != null) {
				return true;
			}
		}

		for (String message : messages) {
			if (message != null) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isPageComplete() {
		if (hasErrors()) {
			return false;
		}

		if (nextPage == null) {
			return true;
		}
		else {
			return nextPage.getControl() != null && nextPage.isPageComplete();
		}
	}

	private void setMessage(String errorMessage, String message) {
		setErrorMessage(errorMessage);
		setMessage(message);
		getWizard().getContainer().updateButtons();
	}

	public void setNextPage(IWizardPage page) {
		this.nextPage = page;
	}

	public void updateMessage() {

		for (String errorMsg : errorMessages) {
			if (errorMsg != null) {
				setMessage(errorMsg, null);
				return;
			}
		}

		for (String message : messages) {
			if (message != null) {
				setMessage(null, message);
				return;
			}
		}

		setMessage(null, DEFAULT_DESCRIPTION);
	}

	protected boolean validatePage() {

		for (WizardTextKeyValidator validator : validators) {
			validator.validate();
		}

		updateMessage();

		if (hasErrors()) {
			return false;
		}
		return true;
	}

}
