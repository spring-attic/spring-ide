/*******************************************************************************
 * Copyright (c) 2005, 2010 Spring IDE Developers
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.webflow.core.internal.model.Set;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelXmlUtils;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeEnabled;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.editor.outline.webflow.WebflowUIImages;

public class SetActionPropertiesDialog extends TitleAreaDialog implements IDialogValidator {

	private Set action;

	private Set actionClone;

	private Label nameLabel;

	private Text nameText;

	private Label valueLabel;

	private Text valueText;

	private Button okButton;

	private PropertiesComposite properties;

	private Combo scopeText;

	private Label scopeLabel;

	private Label attributeLabel;

	private Text attributeText;

	private Label typeLabel;

	private Text typeText;

	public SetActionPropertiesDialog(Shell parentShell, IWebflowModelElement parent, Set state) {
		super(parentShell);
		this.action = state;
		this.actionClone = this.action.cloneModelElement();
	}

	private String trimString(String string) {
		if (string != null && string == "") {
			string = null;
		}
		return string;
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			if (WebflowModelXmlUtils.isVersion1Flow(action)) {
				this.actionClone.setName(trimString(this.nameText.getText()));
				this.actionClone.setScope(trimString(this.scopeText.getText()));
			}
			else {
				this.actionClone.setSetType(trimString(this.typeText.getText()));
			}
			this.actionClone.setAttribute(trimString(this.attributeText.getText()));
			this.actionClone.setValue(trimString(this.valueText.getText()));
			
			
			this.action.applyCloneValues(this.actionClone);
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
		if (this.action != null && this.action.getName() != null) {
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
		groupActionType.setText(" Set ");
		GridData grid = new GridData();
		groupActionType.setLayoutData(grid);

		Composite nameGroup = new Composite(groupActionType, SWT.NULL);
		nameGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout1 = new GridLayout();
		layout1.numColumns = 2;
		layout1.marginWidth = 5;
		nameGroup.setLayout(layout1);
		if (WebflowModelXmlUtils.isVersion1Flow(action)) {
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
		}

		attributeLabel = new Label(nameGroup, SWT.NONE);
		if (WebflowModelXmlUtils.isVersion1Flow(action)) {
			attributeLabel.setText("Attribute");
		}
		else {
			attributeLabel.setText("Name");
		}
		attributeText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
		if (this.action != null && this.action.getAttribute() != null) {
			this.attributeText.setText(this.action.getAttribute());
		}
		attributeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		attributeText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		valueLabel = new Label(nameGroup, SWT.NONE);
		valueLabel.setText("Value");
		valueText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
		if (this.action != null && this.action.getValue() != null) {
			this.valueText.setText(this.action.getValue());
		}
		valueText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		valueText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		if (WebflowModelXmlUtils.isVersion1Flow(action)) {
			scopeLabel = new Label(nameGroup, SWT.NONE);
			scopeLabel.setText("Scope");
			scopeText = new Combo(nameGroup, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
			scopeText.setItems(new String[] { "", "request", "flash", "flow", "conversation",
					"default" });
			if (this.action != null && this.action.getScope() != null) {
				this.scopeText.setText(this.action.getScope());
			}
			scopeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			scopeText.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					validateInput();
				}
			});
		}
		else {
			typeLabel = new Label(nameGroup, SWT.NONE);
			typeLabel.setText("Type");
			typeText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
			if (this.action != null && this.action.getType() != null) {
				this.typeText.setText(this.action.getSetType());
			}
			typeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			typeText.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					validateInput();
				}
			});
		}

		item1.setControl(groupActionType);

		properties = new PropertiesComposite(this, item2, getShell(),
				(IAttributeEnabled) this.actionClone);
		item2.setControl(properties.createDialogArea(folder));

		applyDialogFont(parentComposite);

		return parentComposite;
	}

	protected Image getImage() {
		return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_ACTION);
	}

	public String getMessage() {
		return "Enter the details for the Set element";
	}

	public String getName() {
		return this.nameText.getText();
	}

	protected String getShellTitle() {
		return "Set";
	}

	protected String getTitle() {
		return "Set properties";
	}

	protected void showError(String error) {
		super.setErrorMessage(error);
	}

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
