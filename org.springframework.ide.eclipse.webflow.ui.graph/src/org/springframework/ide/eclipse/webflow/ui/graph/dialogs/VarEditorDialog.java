/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph.dialogs;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.ui.dialogs.FilteredTypesSelectionDialog;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelXmlUtils;
import org.springframework.ide.eclipse.webflow.core.model.IVar;
import org.springframework.ide.eclipse.webflow.ui.editor.outline.webflow.WebflowUIImages;

@SuppressWarnings("restriction")
public class VarEditorDialog extends TitleAreaDialog implements IDialogValidator {

	private IVar property;

	private SelectionListener buttonListener = new SelectionAdapter() {

		public void widgetSelected(SelectionEvent e) {
			handleButtonPressed((Button) e.widget);
		}
	};

	private Label nameLabel;

	private Text nameText;

	private Label scopeLabel;

	private Combo scopeText;

	private Label beanLabel;

	private Text beanText;

	private Label classLabel;

	private Text classText;

	private Button okButton;

	private Button browseTypeButton;

	private Button browseBeanButton;

	public VarEditorDialog(Shell parentShell, IVar state) {
		super(parentShell);
		this.property = state;
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			if (WebflowModelXmlUtils.isVersion1Flow(property)) {
				this.property.setScope(trimString(this.scopeText.getText()));
				this.property.setBean(trimString(this.beanText.getText()));
			}
			this.property.setName(trimString(this.nameText.getText()));
			this.property.setClazz(trimString(this.classText.getText()));
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
		layout1.numColumns = 3;
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

		new Label(nameGroup, SWT.NONE);

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

			new Label(nameGroup, SWT.NONE);
		}

		classLabel = new Label(nameGroup, SWT.NONE);
		classLabel.setText("Class");
		classText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
		if (this.property != null && this.property.getClazz() != null) {
			this.classText.setText(this.property.getClazz());
		}
		classText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		classText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		browseTypeButton = new Button(nameGroup, SWT.PUSH);
		browseTypeButton.setText("...");
		browseTypeButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		browseTypeButton.addSelectionListener(buttonListener);

		if (WebflowModelXmlUtils.isVersion1Flow(property)) {
			beanLabel = new Label(nameGroup, SWT.NONE);
			beanLabel.setText("Bean");
			beanText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
			if (this.property != null && this.property.getBean() != null) {
				this.beanText.setText(this.property.getBean());
			}
			beanText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			beanText.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					validateInput();
				}
			});

			browseBeanButton = new Button(nameGroup, SWT.PUSH);
			browseBeanButton.setText("...");
			browseBeanButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
			browseBeanButton.addSelectionListener(buttonListener);
		}
		applyDialogFont(parentComposite);

		return parentComposite;
	}

	/**
	 * @return
	 */
	public String getId() {
		return this.nameText.getText();
	}

	/**
	 * @return
	 */
	protected Image getImage() {
		return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_VAR);
	}

	/**
	 * @return
	 */
	protected String getMessage() {
		return "Enter the details for the variable";
	}

	/**
	 * @return
	 */
	protected String getShellTitle() {
		return "Variable";
	}

	/**
	 * @return
	 */
	protected String getTitle() {
		return "Variable properties";
	}

	/**
	 * One of the buttons has been pressed, act accordingly.
	 * @param button
	 */
	private void handleButtonPressed(Button button) {

		if (button.equals(this.browseTypeButton)) {

			IJavaSearchScope searchScope = SearchEngine.createWorkspaceScope();
			FilteredTypesSelectionDialog dialog = new FilteredTypesSelectionDialog(getShell(), false,
					new ProgressMonitorDialog(getShell()), searchScope, IJavaSearchConstants.CLASS);

			dialog.setMessage("Select an type"); //$NON-NLS-1$
			dialog.setBlockOnOpen(true);
			dialog.setTitle("Type Selection");
			if (Dialog.OK == dialog.open()) {
				IType obj = (IType) dialog.getFirstResult();
				this.classText.setText(obj.getFullyQualifiedName());
			}
		}
		else if (button.equals(this.browseBeanButton)) {
			ElementListSelectionDialog dialog = DialogUtils.openBeanReferenceDialog(this.beanText
					.getText(), false);
			if (Dialog.OK == dialog.open()) {
				this.beanText.setText(((IBean) dialog.getFirstResult()).getElementName());
			}
		}
		this.validateInput();

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
