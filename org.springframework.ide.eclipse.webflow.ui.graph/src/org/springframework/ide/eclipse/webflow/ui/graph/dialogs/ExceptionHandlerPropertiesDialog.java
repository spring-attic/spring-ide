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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.TextControlCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.webflow.core.internal.model.ExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.editor.outline.webflow.WebflowUIImages;
import org.springframework.ide.eclipse.webflow.ui.graph.WebflowUtils;

/**
 * 
 */
public class ExceptionHandlerPropertiesDialog extends TitleAreaDialog implements
		IDialogValidator {

	/**
	 * 
	 */
	private org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler action;

	/**
	 * 
	 */
	private org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler actionClone;

	/**
	 * 
	 */
	private Label beanLabel;

	/**
	 * 
	 */
	private Text beanText;

	/**
	 * 
	 */
	private Button okButton;

	/**
	 * 
	 */
	private int LABEL_WIDTH = 70;

	/**
	 * 
	 */
	private IDialogValidator validator;

	/**
	 * 
	 */
	private Button browseBeanButton;

	/**
	 * 
	 */
	private SelectionListener buttonListener = new SelectionAdapter() {

		public void widgetSelected(SelectionEvent e) {
			handleButtonPressed((Button) e.widget);
		}
	};

	/**
	 * 
	 * 
	 * @param parentShell 
	 * @param state 
	 * @param parent 
	 */
	public ExceptionHandlerPropertiesDialog(Shell parentShell,
			IWebflowModelElement parent, ExceptionHandler state) {
		super(parentShell);
		this.action = state;
		this.actionClone = state.cloneModelElement();

	}

	/**
	 * 
	 * 
	 * @param string 
	 * 
	 * @return 
	 */
	private String trimString(String string) {
		if (string != null && string == "") {
			string = null;
		}
		return string;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			this.actionClone.setBean(trimString(this.beanText.getText()));
			((ICloneableModelElement<org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler>) this.action)
					.applyCloneValues(this.actionClone);
		}
		super.buttonPressed(buttonId);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(getShellTitle());
		shell.setImage(getImage());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		okButton = createButton(parent, IDialogConstants.OK_ID,
				IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
		// do this here because setting the text will set enablement on the
		// ok button
		beanText.setFocus();
		if (this.action != null && this.action.getBean() != null) {
			okButton.setEnabled(true);
		}
		else {
			okButton.setEnabled(false);
		}

		this.validateInput();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		setTitle(getTitle());
		setMessage(getMessage());
		return contents;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite parentComposite = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(parentComposite, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite nameGroup = new Composite(composite, SWT.NULL);
		nameGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout1 = new GridLayout();
		layout1.numColumns = 3;
		layout1.marginWidth = 5;
		nameGroup.setLayout(layout1);

		// Label field.
		beanLabel = new Label(nameGroup, SWT.NONE);
		beanLabel.setText("Bean");
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.widthHint = LABEL_WIDTH;
		beanLabel.setLayoutData(gridData);
		
		// Create a decorated field with a required field decoration.
		DecoratedField beanField = new DecoratedField(nameGroup, SWT.SINGLE
				| SWT.BORDER, new TextControlCreator());
		FieldDecoration requiredFieldIndicator = FieldDecorationRegistry
				.getDefault().getFieldDecoration(
						FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		beanField.addFieldDecoration(requiredFieldIndicator,
				SWT.TOP | SWT.LEFT, true);
		beanText = (Text) beanField.getControl();
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		beanField.getLayoutControl().setLayoutData(data);
		if (this.action != null && this.action.getBean() != null) {
			beanText.setText(this.action.getBean());
		}
		beanText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				if (validator != null) {
					validator.validateInput();
				}
			}
		});
		
		DialogUtils.attachContentAssist(beanText, WebflowUtils
				.getBeansFromEditorInput().toArray());
		
		browseBeanButton = new Button(nameGroup, SWT.PUSH);
		browseBeanButton.setText("...");
		browseBeanButton.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_END));
		browseBeanButton.addSelectionListener(buttonListener);

		applyDialogFont(parentComposite);
		return parentComposite;
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	protected Image getImage() {
		return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_EXCEPTION_HANDLER);
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	protected String getMessage() {
		return "Enter the details for the exception handler";
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	protected String getShellTitle() {
		return "Exception Handler";
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	protected String getTitle() {
		return "Exception Handler properties";
	}

	/**
	 * 
	 * 
	 * @param error 
	 */
	protected void showError(String error) {
		super.setErrorMessage(error);
	}

	/* (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.ui.graph.dialogs.IDialogValidator#validateInput()
	 */
	public void validateInput() {
		boolean error = false;
		StringBuffer errorMessage = new StringBuffer();
		if (error) {
			getButton(OK).setEnabled(false);
			setErrorMessage(errorMessage.toString());
		}
		else {
			getButton(OK).setEnabled(true);
			setErrorMessage(null);
		}
	}

	/**
	 * 
	 * 
	 * @param button 
	 */
	private void handleButtonPressed(Button button) {

		if (button.equals(browseBeanButton)) {
			ElementListSelectionDialog dialog = DialogUtils.openBeanReferenceDialog(this.beanText
					.getText(), false);
			if (Dialog.OK == dialog.open()) {
				this.beanText.setText(((IBean) dialog.getFirstResult())
						.getElementName());
			}
		}
	}
}
