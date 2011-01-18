/*******************************************************************************
 * Copyright (c) 2007, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelXmlUtils;
import org.springframework.ide.eclipse.webflow.core.model.IInputAttribute;
import org.springframework.ide.eclipse.webflow.ui.editor.outline.webflow.WebflowUIImages;

/**
 * @author Christian Dupuis
 */
public class InputAttributeEditorDialog extends TitleAreaDialog implements IDialogValidator {

	private IInputAttribute property;

	private Label nameLabel;

	private Text nameText;

	private Label requiredLabel;

	private Combo requiredText;

	private Label scopeLabel;

	private Combo scopeText;

	private Button okButton;

	private Label valueLabel;

	private Text valueText;

	private Label typeLabel;

	private Text typeText;

	public InputAttributeEditorDialog(Shell parentShell, IInputAttribute state) {
		super(parentShell);
		this.property = state;
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			this.property.setName(trimString(this.nameText.getText()));

			if (WebflowModelXmlUtils.isVersion1Flow(property)) {
				if (trimString(this.requiredText.getText()) != null) {
					this.property.setRequired(Boolean.valueOf(trimString(this.requiredText
							.getText())));
				}
				this.property.setScope(trimString(this.scopeText.getText()));
			}
			else {
				this.property.setType(trimString(typeText.getText()));
				this.property.setValue(trimString(valueText.getText()));
			}
		}
		super.buttonPressed(buttonId);
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(getShellTitle());
		shell.setImage(getImage());
	}

	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		// do this here because setting the text will set enablement on the
		// ok button
		nameText.setFocus();
		if (this.property != null && this.property.getName() != null) {
			okButton.setEnabled(true);
		}
		else {
			okButton.setEnabled(false);
		}

		this.validateInput();
	}

	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		setTitle(getTitle());
		setMessage(getMessage());
		return contents;
	}

	protected Control createDialogArea(Composite parent) {
		Composite parentComposite = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(parentComposite, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite nameGroup = new Composite(composite, SWT.NULL);
		nameGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout1 = new GridLayout();
		layout1.numColumns = 2;
		layout1.marginWidth = 5;
		nameGroup.setLayout(layout1);
		nameLabel = new Label(nameGroup, SWT.NONE);
		nameLabel.setText("Name");
		nameText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
		if (this.property != null && this.property.getName() != null) {
			this.nameText.setText(this.property.getName());
		}
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		requiredLabel = new Label(nameGroup, SWT.NONE);
		requiredLabel.setText("Required");
		requiredText = new Combo(nameGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		requiredText.setItems(new String[] { "", "true", "false" });
		if (this.property != null) {
			this.requiredText.setText(Boolean.toString(this.property.getRequired()));
		}
		requiredText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		requiredText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		if (WebflowModelXmlUtils.isVersion1Flow(property)) {
			scopeLabel = new Label(nameGroup, SWT.NONE);
			scopeLabel.setText("Scope");
			scopeText = new Combo(nameGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
			scopeText.setItems(new String[] { "", "request", "flash", "flow", "conversation",
					"default" });
			if (this.property != null && this.property.getScope() != null) {
				this.scopeText.setText(this.property.getScope());
			}
			scopeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			scopeText.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					validateInput();
				}
			});
		}
		else {
			valueLabel = new Label(nameGroup, SWT.NONE);
			valueLabel.setText("Value");
			valueText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
			if (this.property != null && this.property.getValue() != null) {
				this.valueText.setText(this.property.getValue());
			}
			valueText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			valueText.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					validateInput();
				}
			});

			typeLabel = new Label(nameGroup, SWT.NONE);
			typeLabel.setText("Type");
			typeText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
			if (this.property != null && this.property.getType() != null) {
				this.typeText.setText(this.property.getType());
			}
			typeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			typeText.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					validateInput();
				}
			});

		}

		applyDialogFont(parentComposite);

		return parentComposite;
	}

	public String getId() {
		return this.nameText.getText();
	}

	protected Image getImage() {
		return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_PROPERTIES);
	}

	public String getMessage() {
		return "Enter the details for the property";
	}

	protected String getShellTitle() {
		return "Property";
	}

	protected String getTitle() {
		return "Property properties";
	}

	protected void showError(String error) {
		super.setErrorMessage(error);
	}

	public String trimString(String string) {
		if (string != null && string == "") {
			string = null;
		}
		return string;
	}

	public void validateInput() {
		String id = this.nameText.getText();
		boolean error = false;
		StringBuffer errorMessage = new StringBuffer();

		if (id == null || "".equals(id)) {
			errorMessage.append("A valid name is required. ");
			error = true;
		}

		if (error) {
			getButton(OK).setEnabled(false);
			setErrorMessage(errorMessage.toString());
		}
		else {
			getButton(OK).setEnabled(true);
			setErrorMessage(null);
		}
	}
}
