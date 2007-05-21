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

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.ui.dialogs.TypeSelectionDialog2;
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
import org.springframework.ide.eclipse.webflow.core.model.IMapping;
import org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow.WebflowUIImages;

/**
 * 
 */
@SuppressWarnings("restriction")
public class MappingEditorDialog extends TitleAreaDialog implements
		IDialogValidator {

	/**
	 * 
	 */
	private IMapping property;

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
	 */
	private Label sourceLabel;

	/**
	 * 
	 */
	private Text sourceText;

	/**
	 * 
	 */
	private Label targetLabel;

	/**
	 * 
	 */
	private Text targetText;

	/**
	 * 
	 */
	private Label targetCollectionLabel;

	/**
	 * 
	 */
	private Text targetCollectionText;

	/**
	 * 
	 */
	private Label fromLabel;

	/**
	 * 
	 */
	private Text fromText;

	/**
	 * 
	 */
	private Label toLabel;

	/**
	 * 
	 */
	private Text toText;

	/**
	 * 
	 */
	private Label requiredLabel;

	/**
	 * 
	 */
	private Combo requiredText;

	/**
	 * 
	 */
	private Button okButton;
	
	/**
	 * 
	 */
	private Button fromBrowseType;

	/**
	 * 
	 */
	private Button toBrowseType;

	/**
	 * 
	 * 
	 * @param parentShell 
	 * @param state 
	 */
	public MappingEditorDialog(Shell parentShell, IMapping state) {
		super(parentShell);
		this.property = state;
	}

	/**
	 * 
	 * 
	 * @param buttonId 
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			this.property.setSource(trimString(this.sourceText.getText()));
			this.property.setTarget(trimString(this.targetText.getText()));
			this.property
					.setTargetCollection(trimString(this.targetCollectionText
							.getText()));
			this.property.setFrom(trimString(this.fromText.getText()));
			this.property.setTo(trimString(this.toText.getText()));
			if (this.requiredText.getText() != null) {
				this.property.setRequired(Boolean
						.valueOf(trimString(this.requiredText.getText())));
			}
		}
		super.buttonPressed(buttonId);
	}

	/**
	 * 
	 * 
	 * @param shell 
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(getShellTitle());
		shell.setImage(getImage());
	}

	/**
	 * 
	 * 
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
		sourceText.setFocus();
		if (this.property != null && this.property.getSource() != null) {
			okButton.setEnabled(true);
		}
		else {
			okButton.setEnabled(false);
		}

		this.validateInput();
	}

	/**
	 * 
	 * 
	 * @param parent 
	 * 
	 * @return 
	 */
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		setTitle(getTitle());
		setMessage(getMessage());
		return contents;
	}

	/**
	 * 
	 * 
	 * @param parent 
	 * 
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
		sourceLabel = new Label(nameGroup, SWT.NONE);
		sourceLabel.setText("Source");
		sourceText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
		if (this.property != null && this.property.getSource() != null) {
			this.sourceText.setText(this.property.getSource());
		}
		sourceText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		sourceText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		new Label(nameGroup, SWT.NONE);

		targetLabel = new Label(nameGroup, SWT.NONE);
		targetLabel.setText("Target");
		targetText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
		if (this.property != null && this.property.getTarget() != null) {
			this.targetText.setText(this.property.getTarget());
		}
		targetText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		targetText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		new Label(nameGroup, SWT.NONE);

		targetCollectionLabel = new Label(nameGroup, SWT.NONE);
		targetCollectionLabel.setText("Target Collection");
		targetCollectionText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
		if (this.property != null && this.property.getTargetCollection() != null) {
			this.targetCollectionText.setText(this.property.getTargetCollection());
		}
		targetCollectionText.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		targetCollectionText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		new Label(nameGroup, SWT.NONE);
		
		fromLabel = new Label(nameGroup, SWT.NONE);
		fromLabel.setText("From");
		fromText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
		if (this.property != null && this.property.getFrom() != null) {
			this.fromText.setText(this.property.getFrom());
		}
		fromText.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		fromText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		fromBrowseType = new Button(nameGroup, SWT.PUSH);
		fromBrowseType.setText("...");
		fromBrowseType.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		fromBrowseType.addSelectionListener(buttonListener);

		toLabel = new Label(nameGroup, SWT.NONE);
		toLabel.setText("To");
		toText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
		if (this.property != null && this.property.getTo() != null) {
			this.toText.setText(this.property.getTo());
		}
		toText.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		toText.addModifyListener(new ModifyListener() {
			
			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});
		
		toBrowseType = new Button(nameGroup, SWT.PUSH);
		toBrowseType.setText("...");
		toBrowseType.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		toBrowseType.addSelectionListener(buttonListener);

		requiredLabel = new Label(nameGroup, SWT.NONE);
		requiredLabel.setText("Required");
		requiredText = new Combo(nameGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
		requiredText.setItems(new String[] { "", "true", "false" });
		if (this.property != null) {
			this.requiredText.setText(Boolean.toString(this.property
					.getRequired()));
		}
		requiredText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		requiredText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});
		
		new Label(nameGroup, SWT.NONE);
		
		applyDialogFont(parentComposite);

		return parentComposite;
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	public String getId() {
		return this.sourceText.getText();
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	protected Image getImage() {
		return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_PROPERTIES);
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	protected String getMessage() {
		return "Enter the details for the property";
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	protected String getShellTitle() {
		return "Property";
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	protected String getTitle() {
		return "Property properties";
	}

	/**
	 * One of the buttons has been pressed, act accordingly.
	 * 
	 * @param button 
	 */
	private void handleButtonPressed(Button button) {

		IJavaSearchScope searchScope = SearchEngine.createWorkspaceScope();
		TypeSelectionDialog2 dialog = new TypeSelectionDialog2(getShell(),
				false, new ProgressMonitorDialog(getShell()), searchScope,
				IJavaSearchConstants.CLASS);

		dialog.setMessage("Select an type"); //$NON-NLS-1$
		dialog.setBlockOnOpen(true);
		dialog.setTitle("Type Selection");
		// dialog.setFilter("*");
		if (Dialog.OK == dialog.open()) {
			IType obj = (IType) dialog.getFirstResult();
			this.requiredText.setText(obj.getFullyQualifiedName());
		}

		this.validateInput();

	}

	/**
	 * 
	 * 
	 * @param error 
	 */
	protected void showError(String error) {
		super.setErrorMessage(error);
	}

	/**
	 * 
	 * 
	 * @param string 
	 * 
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
		String id = this.sourceText.getText();
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
