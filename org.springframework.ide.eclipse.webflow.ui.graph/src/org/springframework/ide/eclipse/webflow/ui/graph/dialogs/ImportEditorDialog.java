/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.webflow.core.model.IImport;
import org.springframework.ide.eclipse.webflow.ui.editor.outline.webflow.WebflowUIImages;

/**
 * 
 */
@SuppressWarnings("restriction")
public class ImportEditorDialog extends TitleAreaDialog implements
		IDialogValidator {

	/**
	 * 
	 */
	private IImport property;

	/**
	 * 
	 */
	private Label resourceLabel;

	/**
	 * 
	 */
	private Text resouceText;

	/**
	 * 
	 */
	private Button okButton;

	/**
	 * @param parentShell
	 * @param state
	 */
	public ImportEditorDialog(Shell parentShell, IImport state) {
		super(parentShell);
		this.property = state;
	}

	/**
	 * @param buttonId
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			this.property.setResource(trimString(this.resouceText.getText()));
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * @param shell
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(getShellTitle());
		shell.setImage(getImage());
	}

	/**
	 * @param parent
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		okButton = createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		// do this here because setting the text will set enablement on the
		// ok button
		resouceText.setFocus();
		if (this.property != null && this.property.getResource() != null) {
			okButton.setEnabled(true);
		}
		else {
			okButton.setEnabled(false);
		}

		this.validateInput();
	}

	/**
	 * @param parent
	 * @return
	 */
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		setTitle(getTitle());
		setMessage(getMessage());
		return contents;
	}

	/**
	 * @param parent
	 * @return
	 */
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
		layout1.numColumns = 3;
		layout1.marginWidth = 5;
		nameGroup.setLayout(layout1);
		resourceLabel = new Label(nameGroup, SWT.NONE);
		resourceLabel.setText("Name");
		resouceText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
		if (this.property != null && this.property.getResource() != null) {
			this.resouceText.setText(this.property.getResource());
		}
		resouceText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		resouceText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		new Label(nameGroup, SWT.NONE);

		applyDialogFont(parentComposite);

		return parentComposite;
	}

	/**
	 * @return
	 */
	public String getId() {
		return this.resouceText.getText();
	}

	/**
	 * @return
	 */
	protected Image getImage() {
		return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_IMPORT);
	}

	/**
	 * @return
	 */
	protected String getMessage() {
		return "Enter the details for the import";
	}

	/**
	 * @return
	 */
	protected String getShellTitle() {
		return "Import";
	}

	/**
	 * @return
	 */
	protected String getTitle() {
		return "Import properties";
	}

	/**
	 * @param error
	 */
	protected void showError(String error) {
		super.setErrorMessage(error);
	}

	/**
	 * @param string
	 * @return
	 */
	public String trimString(String string) {
		if (string != null && string == "") {
			string = null;
		}
		return string;
	}

	/**
	 * 
	 */
	public void validateInput() {
		String id = this.resouceText.getText();
		boolean error = false;
		StringBuffer errorMessage = new StringBuffer();

		if (id == null || "".equals(id)) {
			errorMessage.append("A valid resource name is required. ");
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
