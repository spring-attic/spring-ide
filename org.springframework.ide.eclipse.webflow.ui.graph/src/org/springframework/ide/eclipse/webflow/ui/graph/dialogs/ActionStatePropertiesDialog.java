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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.webflow.core.internal.model.EntryActions;
import org.springframework.ide.eclipse.webflow.core.internal.model.ExitActions;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IActionState;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeEnabled;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow.WebflowUIImages;

/**
 * 
 */
public class ActionStatePropertiesDialog extends TitleAreaDialog implements
		IDialogValidator {

	/**
	 * 
	 */
	private IActionState actionState;

	/**
	 * 
	 */
	private IActionState actionStateClone;

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
	private Button okButton;

	/**
	 * 
	 */
	private IWebflowModelElement parent;

	/**
	 * 
	 */
	private PropertiesComposite properties;

	/**
	 * 
	 */
	private ActionComposite actionsComposite;

	/**
	 * 
	 */
	private ActionComposite entryActionsComposite;

	/**
	 * 
	 */
	private ActionComposite exitActionsComposite;

	/**
	 * 
	 */
	private ExceptionHandlerComposite exceptionHandlerComposite;

	/**
	 * 
	 */
	private List<IActionElement> entryActions;

	/**
	 * 
	 */
	private List<IActionElement> exitActions;

	/**
	 * 
	 */
	private List<IActionElement> actions;

	/**
	 * 
	 */
	private List<org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler> exceptionHandler;

	/**
	 * 
	 * 
	 * @param parentShell 
	 * @param state 
	 * @param parent 
	 */
	public ActionStatePropertiesDialog(Shell parentShell,
			IWebflowModelElement parent, IActionState state) {
		super(parentShell);
		this.actionState = state;
		this.parent = parent;
		this.actionStateClone = ((ICloneableModelElement<IActionState>) state)
				.cloneModelElement();
		if (this.actionStateClone.getEntryActions() != null) {
			entryActions = new ArrayList<IActionElement>();
			entryActions.addAll(this.actionStateClone.getEntryActions()
					.getEntryActions());
		}
		else {
			entryActions = new ArrayList<IActionElement>();
			EntryActions entry = new EntryActions();
			entry.createNew(actionStateClone);
			actionStateClone.setEntryActions(entry);
		}
		if (this.actionStateClone.getExitActions() != null) {
			exitActions = new ArrayList<IActionElement>();
			exitActions.addAll(this.actionStateClone.getExitActions()
					.getExitActions());
		}
		else {
			exitActions = new ArrayList<IActionElement>();
			ExitActions exit = new ExitActions();
			exit.createNew(actionStateClone);
			actionStateClone.setExitActions(exit);
		}
		actions = new ArrayList<IActionElement>();
		if (this.actionStateClone.getActions() != null) {
			actions.addAll(this.actionStateClone.getActions());
		}

		exceptionHandler = new ArrayList<org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler>();
		if (this.actionStateClone.getExceptionHandlers() != null) {
			exceptionHandler.addAll(this.actionStateClone
					.getExceptionHandlers());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			this.actionStateClone.setId(trimString(getId()));

			if (actionState.getEntryActions() == null
					&& this.entryActions.size() > 0) {
				EntryActions entry = new EntryActions();
				entry.createNew(actionStateClone);
				for (IActionElement a : this.entryActions) {
					entry.addEntryAction(a);
				}
				actionStateClone.setEntryActions(entry);
			}
			else if (this.entryActions.size() == 0) {
				actionStateClone.setEntryActions(null);
			}
			else {
				actionStateClone.getEntryActions().removeAll();
				for (IActionElement a : this.entryActions) {
					actionStateClone.getEntryActions().addEntryAction(a);
				}
			}

			if (actionState.getExitActions() == null
					&& this.exitActions.size() > 0) {
				ExitActions exit = new ExitActions();
				exit.createNew(actionStateClone);
				for (IActionElement a : this.exitActions) {
					exit.addExitAction(a);
				}
				actionStateClone.setExitActions(exit);
			}
			else if (this.exitActions.size() == 0) {
				actionStateClone.setExitActions(null);
			}
			else {
				actionStateClone.getExitActions().removeAll();
				for (IActionElement a : this.exitActions) {
					actionStateClone.getExitActions().addExitAction(a);
				}
			}

			if (this.actions != null && this.actions.size() > 0) {
				actionStateClone.removeAll();
				for (IActionElement a : this.actions) {
					actionStateClone.addAction(a);
				}
			}
			else {
				actionStateClone.removeAll();
			}

			if (this.exceptionHandler != null
					&& this.exceptionHandler.size() > 0) {
				actionStateClone.removeAllExceptionHandler();
				for (org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler a : this.exceptionHandler) {
					actionStateClone.addExceptionHandler(a);
				}
			}
			else {
				actionStateClone.removeAllExceptionHandler();
			}

			((ICloneableModelElement<IActionState>) this.actionState)
					.applyCloneValues(this.actionStateClone);
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
		if (this.actionState != null && this.actionState.getId() != null) {
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
		TabItem item1 = new TabItem(folder, SWT.NULL);
		item1.setText("General");
		item1.setImage(getImage());
		TabItem item2 = new TabItem(folder, SWT.NULL);
		TabItem item3 = new TabItem(folder, SWT.NULL);
		TabItem item4 = new TabItem(folder, SWT.NULL);
		TabItem item5 = new TabItem(folder, SWT.NULL);
		TabItem item6 = new TabItem(folder, SWT.NULL);

		Group groupActionType = new Group(folder, SWT.NULL);
		GridLayout layoutAttMap = new GridLayout();
		layoutAttMap.marginWidth = 3;
		layoutAttMap.marginHeight = 3;
		groupActionType.setLayout(layoutAttMap);
		groupActionType.setText(" Action State ");
		GridData grid = new GridData();
		groupActionType.setLayoutData(grid);

		Composite nameGroup = new Composite(groupActionType, SWT.NULL);
		nameGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout1 = new GridLayout();
		layout1.numColumns = 2;
		layout1.marginWidth = 5;
		nameGroup.setLayout(layout1);
		nameLabel = new Label(nameGroup, SWT.NONE);
		nameLabel.setText("State id");
		nameText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
		if (this.actionState != null && this.actionState.getId() != null) {
			this.nameText.setText(this.actionState.getId());
		}
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		item1.setControl(groupActionType);

		actionsComposite = new ActionComposite(this, item2, getShell(),
				actions, this.actionStateClone,
				IActionElement.ACTION_TYPE.ACTION);
		item2.setControl(actionsComposite.createDialogArea(folder));

		entryActionsComposite = new ActionComposite(this, item3, getShell(),
				this.entryActions, this.actionStateClone.getEntryActions(),
				IActionElement.ACTION_TYPE.ENTRY_ACTION);
		item3.setControl(entryActionsComposite.createDialogArea(folder));

		exitActionsComposite = new ActionComposite(this, item4, getShell(),
				this.exitActions, this.actionStateClone.getExitActions(),
				IActionElement.ACTION_TYPE.EXIT_ACTION);
		item4.setControl(exitActionsComposite.createDialogArea(folder));

		exceptionHandlerComposite = new ExceptionHandlerComposite(this, item5,
				getShell(), this.exceptionHandler, this.actionStateClone);
		item5.setControl(exceptionHandlerComposite.createDialogArea(folder));

		properties = new PropertiesComposite(this, item6, getShell(),
				(IAttributeEnabled) this.actionStateClone);
		item6.setControl(properties.createDialogArea(folder));

		applyDialogFont(parentComposite);

		return parentComposite;
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	public String getId() {
		return this.nameText.getText();
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	protected Image getImage() {
		return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_ACTION_STATE);
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	protected String getMessage() {
		return "Enter the details for the action state";
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
	protected String getShellTitle() {
		return "Action State";
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	protected String getTitle() {
		return "Action State properties";
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
		String id = this.nameText.getText();
		boolean error = false;
		StringBuffer errorMessage = new StringBuffer();

		if (id == null || "".equals(id)) {
			errorMessage.append("A valid id attribute is required. ");
			error = true;
		}
		else {
			/*
			 * if (WebFlowCoreUtils.isIdAlreadyChoosenByAnotherState(parent,
			 * actionState, id)) { errorMessage .append("The entered id
			 * attribute must be unique within a single web flow. "); error =
			 * true; }
			 */
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