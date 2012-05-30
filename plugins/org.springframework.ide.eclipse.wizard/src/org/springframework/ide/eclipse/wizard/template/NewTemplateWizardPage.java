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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.springframework.ide.eclipse.wizard.template.infrastructure.ui.WizardUIInfoElement;


/**
 * @author Terry Denney
 * @author Leo Dos Santos
 * @author Christian Dupuis
 */
public class NewTemplateWizardPage extends WizardNewProjectCreationPage implements ITemplateWizardPage {

	private IWizardPage nextPage;

	private final List<WizardUIInfoElement> elements;

	private final Map<String, Control> controls;

	private final String[] errorMessages;

	private final String[] messages;

	private final Set<WizardTextKeyValidator> validators;

	private final String wizardTitle;

	private final TemplateWizard wizard;

	private static final String DEFAULT_DESCRIPTION = Messages.getString("TemplateWizardPage.DEFAULT_DESCRIPTION"); //$NON-NLS-1$

	protected NewTemplateWizardPage(String pageTitle, List<WizardUIInfoElement> elements, String wizardTitle,
			TemplateWizard wizard, ImageDescriptor icon) {
		super("Template Wizard Page"); //$NON-NLS-1$
		this.elements = elements;
		this.wizardTitle = wizardTitle;
		this.wizard = wizard;

		this.controls = new HashMap<String, Control>();
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

	public void collectInput(Map<String, Object> collectedInput, Map<String, String> inputKinds) {
		for (WizardUIInfoElement element : elements) {
			String elementName = element.getName();
			Control control = controls.get(elementName);
			if (control instanceof Button) {
				collectedInput.put(elementName, ((Button) control).getSelection());
			}
			else if (control instanceof Text) {
				collectedInput.put(elementName, ((Text) control).getText());
			}

			String replaceKind = element.getReplaceKind();
			if (replaceKind != null) {
				inputKinds.put(elementName, replaceKind);
			}
			else {
				inputKinds.put(elementName, WizardUIInfoElement.DEFAULT_KIND);
			}
		}
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		Composite control = (Composite) getControl();
		GridLayout controlLayout = new GridLayout();
		controlLayout.verticalSpacing = 10;
		control.setLayout(controlLayout);

		wizard.setWindowTitle("New " + wizardTitle); //$NON-NLS-1$

		Composite container = new Composite(control, SWT.NONE);
		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

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

				Button button = new Button(buttonContainer, SWT.CHECK);
				button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
				if (i == 0) {
					button.setFocus();
				}

				if (defaultValue != null && defaultValue.equals("true")) {
					button.setSelection(true);
				}
				else if (defaultValue != null && defaultValue.equals("false")) {
					button.setSelection(false);
				}

				controls.put(element.getName(), button);
			}
			else {
				Label descriptionLabel = new Label(container, SWT.NONE);
				descriptionLabel.setText(description);
				descriptionLabel.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, true, false));

				final Text text = new Text(container, SWT.BORDER);
				text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
				text.setEditable(true);
				controls.put(element.getName(), text);

				if (defaultValue != null && defaultValue.length() > 0) {
					text.setText(defaultValue);
				}

				WizardTextKeyValidator validator = new WizardTextKeyValidator(i, element, text, this);
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
		if (getProjectName() == null || getProjectName().length() == 0 || getProjectName().contains(" ")) {
			return true;
		}
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
		if (getProjectName() == null || getProjectName().length() == 0) {
			setMessage(null, "Enter project name.");
			return;
		}

		if (getProjectName().contains(" ")) {
			setErrorMessage("Project name can not contain spaces.");
			return;
		}

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

	@Override
	protected boolean validatePage() {
		if (!super.validatePage()) {
			return false;
		}

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
