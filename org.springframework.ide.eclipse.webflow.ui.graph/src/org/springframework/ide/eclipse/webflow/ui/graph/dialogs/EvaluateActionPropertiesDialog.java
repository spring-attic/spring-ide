/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.webflow.core.internal.model.EvaluateAction;
import org.springframework.ide.eclipse.webflow.core.internal.model.EvaluationResult;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeEnabled;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow.WebflowUIImages;

/**
 * 
 */
public class EvaluateActionPropertiesDialog extends TitleAreaDialog implements
		IDialogValidator {

	/**
	 * 
	 */
	private EvaluateAction action;

	/**
	 * 
	 */
	private EvaluateAction actionClone;

	/**
	 * 
	 */
	private Label nameLabel;

	/**
	 * 
	 */
	private Text nameText;

	/**
	 * 
	 */
	private Label expressionLabel;

	/**
	 * 
	 */
	private Text expressionText;

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
	private PropertiesComposite properties;

	/**
	 * 
	 */
	private Combo scopeText;

	/**
	 * 
	 */
	private Label scopeLabel;

	/**
	 * 
	 */
	private Label resultNameLabel;

	/**
	 * 
	 */
	private Text resultNameText;

	/**
	 * 
	 * 
	 * @param parentShell 
	 * @param state 
	 * @param parent 
	 */
	public EvaluateActionPropertiesDialog(Shell parentShell,
			IWebflowModelElement parent, EvaluateAction state) {
		super(parentShell);
		this.action = state;
		this.actionClone = this.action.cloneModelElement();

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
			this.actionClone.setName(trimString(this.nameText.getText()));
			this.actionClone.setExpression(trimString(this.expressionText.getText()));
			
			if (trimString(this.scopeText.getText()) == null
					&& trimString(this.resultNameText.getText()) == null) {
				this.actionClone.setEvaluationResult(null);
			}
			else if (this.action.getEvaluationResult() != null) {
				this.actionClone.getEvaluationResult().setName(
						this.resultNameText.getText());
				this.actionClone.getEvaluationResult().setScope(
						this.scopeText.getText());
			}
			else if (this.action.getEvaluationResult() == null) {
				EvaluationResult result = new EvaluationResult();
				result.createNew(actionClone);
				this.actionClone.setEvaluationResult(result);
				this.actionClone.getEvaluationResult().setName(
						this.resultNameText.getText());
				this.actionClone.getEvaluationResult().setScope(
						this.scopeText.getText());
			}
			
			this.action.applyCloneValues(this.actionClone);
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
		nameText.setFocus();
		if (this.action != null && this.action.getName() != null) {
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
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		TabFolder folder = new TabFolder(composite, SWT.NULL);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		TabItem item1 = new TabItem(folder, SWT.NULL);
		item1.setText("General");
		item1.setImage(getImage());
		TabItem item2 = new TabItem(folder, SWT.NULL);

		Group groupActionType = new Group(folder, SWT.NULL);
		GridLayout layoutAttMap = new GridLayout();
		layoutAttMap.marginWidth = 3;
		layoutAttMap.marginHeight = 3;
		groupActionType.setLayout(layoutAttMap);
		groupActionType.setText(" Evaluate Action ");
		GridData grid = new GridData();
		groupActionType.setLayoutData(grid);

		Composite nameGroup = new Composite(groupActionType, SWT.NULL);
		nameGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout1 = new GridLayout();
		layout1.numColumns = 2;
		layout1.marginWidth = 5;
		nameGroup.setLayout(layout1);
		nameLabel = new Label(nameGroup, SWT.NONE);
		nameLabel.setText("Name");
		nameText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
		if (this.action != null && this.action.getName() != null) {
			this.nameText.setText(this.action.getName());
		}
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		expressionLabel = new Label(nameGroup, SWT.NONE);
		expressionLabel.setText("Expression");
		expressionText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
		if (this.action != null && this.action.getExpression() != null) {
			this.expressionText.setText(this.action.getExpression());
		}
		expressionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		expressionText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		Group groupMethodResult = new Group(groupActionType, SWT.NULL);
		layoutAttMap = new GridLayout();
		layoutAttMap.marginWidth = 3;
		layoutAttMap.marginHeight = 3;
		layoutAttMap.numColumns = 3;
		layoutAttMap.marginWidth = 5;
		groupMethodResult.setLayout(layoutAttMap);
		groupMethodResult.setText(" Evaluation Result ");
		groupMethodResult.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		resultNameLabel = new Label(groupMethodResult, SWT.NONE);
		resultNameLabel.setText("Name");
		resultNameText = new Text(groupMethodResult, SWT.SINGLE | SWT.BORDER);
		if (this.action != null && this.action.getEvaluationResult() != null) {
			this.resultNameText.setText(this.action.getEvaluationResult()
					.getName());
		}
		resultNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		resultNameText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		new Label(groupMethodResult, SWT.NONE);

		// Label field.
		scopeLabel = new Label(groupMethodResult, SWT.NONE);
		scopeLabel.setText("Scope");
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.widthHint = LABEL_WIDTH;
		scopeLabel.setLayoutData(gridData);

		// Add the text box for action classname.
		scopeText = new Combo(groupMethodResult, SWT.SINGLE | SWT.BORDER
				| SWT.READ_ONLY);
		scopeText.setItems(new String[] { "", "request", "flash", "flow",
				"conversation", "default" });
		if (this.action != null && this.action.getEvaluationResult() != null) {
			scopeText.setText(this.action.getEvaluationResult().getScope());
		}
		scopeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		scopeText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validator.validateInput();
			}
		});

		new Label(groupMethodResult, SWT.NONE);

		item1.setControl(groupActionType);

		properties = new PropertiesComposite(this, item2, getShell(),
				(IAttributeEnabled) this.actionClone);
		item2.setControl(properties.createDialogArea(folder));

		applyDialogFont(parentComposite);

		return parentComposite;
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	protected Image getImage() {
		return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_ACTION);
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	protected String getMessage() {
		return "Enter the details for the Evaluate action";
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	public String getName() {
		return this.nameText.getText();
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	protected String getShellTitle() {
		return "Evaluate Action";
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	protected String getTitle() {
		return "Evaluate Action properties";
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
}