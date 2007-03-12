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

import java.util.ArrayList;
import java.util.List;

import ognl.Ognl;
import ognl.OgnlException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeEnabled;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.graph.WebflowImages;

/**
 * 
 */
public class StateTransitionPropertiesDialog extends TitleAreaDialog implements
		IDialogValidator {

	/**
	 * 
	 */
	private static final String EXPRESSION_PREFIX = "${";

	/**
	 * 
	 */
	private static final String EXPRESSION_SUFFIX = "}";

	/**
	 * 
	 */
	private List<IActionElement> actions;

	/**
	 * 
	 */
	private Button ognlButton;

	/**
	 * 
	 */
	private Button okButton;

	/**
	 * 
	 */
	private Text onText;

	/**
	 * 
	 */
	private Text onExceptionText;

	/**
	 * 
	 */
	private Button browseExceptionButton;

	/**
	 * 
	 */
	private IWebflowModelElement parent;

	/**
	 * 
	 */
	private IStateTransition transition;

	/**
	 * 
	 */
	private IStateTransition transitionClone;

	/**
	 * 
	 */
	private ActionComposite actionProperties;

	/**
	 * 
	 */
	private PropertiesComposite properties;

	/**
	 * 
	 * 
	 * @param parentShell 
	 * @param state 
	 * @param parent 
	 */
	public StateTransitionPropertiesDialog(Shell parentShell,
			IWebflowModelElement parent, IStateTransition state) {
		super(parentShell);
		this.transition = state;
		this.parent = parent;
		this.transitionClone = ((ICloneableModelElement<IStateTransition>) this.transition)
				.cloneModelElement();
		
		actions = new ArrayList<IActionElement>();
		if (this.transitionClone.getActions() != null) {
			actions.addAll(this.transitionClone.getActions());
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			this.transitionClone.setOn(trimString(getOn()));
			
			if (this.actions != null && this.actions.size() > 0) {
				transitionClone.removeAll();
				for (IActionElement a : this.actions) {
					transitionClone.addAction(a);
				}
			}
			else {
				transitionClone.removeAll();
			}
			
			((ICloneableModelElement<IStateTransition>) this.transition)
					.applyCloneValues(this.transitionClone);
		}
		super.buttonPressed(buttonId);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(getShellTitle());
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
		onText.setFocus();
		if (this.transition != null && this.transition.getOn() != null) {
			okButton.setEnabled(true);
		}
		else {
			okButton.setEnabled(false);
		}
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
		folder.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		TabItem item1 = new TabItem(folder, SWT.NULL);
		item1.setText("General");
		item1.setImage(WebflowImages
				.getImage(WebflowImages.IMG_OBJS_CONNECTION));
		TabItem item2 = new TabItem(folder, SWT.NULL);
		TabItem item4 = new TabItem(folder, SWT.NULL);

		Composite nameGroup = new Composite(folder, SWT.NULL);
		nameGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout1 = new GridLayout();
		layout1.numColumns = 3;
		layout1.marginWidth = 0;
		layout1.marginHeight = 0;
		nameGroup.setLayout(layout1);

		Group groupActionType = new Group(nameGroup, SWT.NULL);
		groupActionType.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layoutAttMap = new GridLayout();
		layoutAttMap.numColumns = 2;
		layoutAttMap.marginWidth = 5;
		groupActionType.setLayout(layoutAttMap);
		groupActionType.setText(" Transition ");
		
		Label onLabel = new Label(groupActionType, SWT.NONE);
		onLabel.setText("On");
		onText = new Text(groupActionType, SWT.SINGLE | SWT.BORDER);
		if (this.transition != null && this.transition.getOn() != null) {
			this.onText.setText(this.transition.getOn());
		}
		onText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		onText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		new Label(groupActionType, SWT.NONE);
		new Label(groupActionType, SWT.NONE);
		ognlButton = new Button(groupActionType, SWT.CHECK);
		ognlButton.setText("Parse OGNL transition criteria");
		ognlButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				validateInput();
			}
		});
		new Label(groupActionType, SWT.NONE);

		Label onExceptionLabel = new Label(groupActionType, SWT.NONE);
		onExceptionLabel.setText("On Exception");
		onExceptionText = new Text(groupActionType, SWT.SINGLE | SWT.BORDER);
		if (this.transition != null && this.transition.getOnException() != null) {
			this.onExceptionText.setText(this.transition.getOnException());
		}
		onExceptionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		onExceptionText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});
		
		item1.setControl(nameGroup);

		actionProperties = new ActionComposite(this, item2, getShell(),
				this.actions, this.transitionClone,
				IActionElement.ACTION_TYPE.ACTION);
		item2.setControl(actionProperties.createDialogArea(folder));

		properties = new PropertiesComposite(this, item4, getShell(),
				(IAttributeEnabled) this.transitionClone);
		item4.setControl(properties.createDialogArea(folder));

		applyDialogFont(parentComposite);

		return parentComposite;
	}

	/**
	 * Cut the expression from given criteria string and return it.
	 * 
	 * @param encodedCriteria 
	 * 
	 * @return 
	 */
	private String cutExpression(String encodedCriteria) {
		return encodedCriteria.substring(EXPRESSION_PREFIX.length(),
				encodedCriteria.length() - EXPRESSION_SUFFIX.length());
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	protected String getMessage() {
		return "Enter the details for the state transition";
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	public IWebflowModelElement getModelElementParent() {
		return this.parent;
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	public String getOn() {
		return this.onText.getText();
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	protected String getShellTitle() {
		return "Transition";
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	protected String getTitle() {
		return "Transition properties";
	}

	/**
	 * 
	 */
	protected void handleTableSelectionChanged() {
		// TODO Auto-generated method stub
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

	/* (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.ui.graph.dialogs.IDialogValidator#validateInput()
	 */
	public void validateInput() {
		String id = this.onText.getText();
		boolean error = false;
		StringBuffer errorMessage = new StringBuffer();
		if (id == null || "".equals(id)) {
			errorMessage.append("A valid id attribute is required. ");
			error = true;
		}
		if (this.ognlButton.getSelection()) {
			if (!id.startsWith(EXPRESSION_PREFIX)
					|| !id.endsWith(EXPRESSION_SUFFIX)) {
				errorMessage
						.append("A valid OGNL expression needs to start with '${' and ends with '}'. ");
				error = true;
			}
			else {
				try {
					Ognl.parseExpression(this.cutExpression(id));
				}
				catch (OgnlException e) {
					errorMessage.append("Malformed OGNL expression. ");
					error = true;
				}
			}
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