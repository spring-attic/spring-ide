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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.webflow.core.internal.model.DecisionState;
import org.springframework.ide.eclipse.webflow.core.internal.model.EntryActions;
import org.springframework.ide.eclipse.webflow.core.internal.model.ExitActions;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeEnabled;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IDecisionState;
import org.springframework.ide.eclipse.webflow.core.model.IIf;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow.WebflowUIImages;
import org.springframework.ide.eclipse.webflow.ui.graph.model.WebflowModelLabelDecorator;
import org.springframework.ide.eclipse.webflow.ui.graph.model.WebflowModelLabelProvider;

/**
 * 
 */
public class DecisionStatePropertiesDialog extends TitleAreaDialog implements
		IDialogValidator {

	/**
	 * 
	 */
	private class IfContentProvider implements IStructuredContentProvider {

		/**
		 * 
		 */
		private IDecisionState project;

		/**
		 * 
		 * 
		 * @param project 
		 */
		public IfContentProvider(IDecisionState project) {
			this.project = project;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object obj) {
			return project.getIfs().toArray();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
		}
	}

	/**
	 * 
	 */
	private IDecisionState decisionState;

	/**
	 * 
	 */
	private IDecisionState decisionStateClone;

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
	private TableViewer configsViewer;

	/**
	 * 
	 */
	private Button editButton;

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
	public DecisionStatePropertiesDialog(Shell parentShell,
			IWebflowModelElement parent, IDecisionState state) {
		super(parentShell);
		this.decisionState = state;
		this.parent = parent;
		this.decisionStateClone = ((DecisionState) state).cloneModelElement();

		if (this.decisionStateClone.getEntryActions() != null) {
			entryActions = new ArrayList<IActionElement>();
			entryActions.addAll(this.decisionStateClone.getEntryActions()
					.getEntryActions());
		}
		else {
			entryActions = new ArrayList<IActionElement>();
			EntryActions entry = new EntryActions();
			entry.createNew(decisionStateClone);
			decisionStateClone.setEntryActions(entry);
		}
		if (this.decisionStateClone.getExitActions() != null) {
			exitActions = new ArrayList<IActionElement>();
			exitActions.addAll(this.decisionStateClone.getExitActions()
					.getExitActions());
		}
		else {
			exitActions = new ArrayList<IActionElement>();
			ExitActions exit = new ExitActions();
			exit.createNew(decisionStateClone);
			decisionStateClone.setExitActions(exit);
		}

		exceptionHandler = new ArrayList<org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler>();
		if (this.decisionStateClone.getExceptionHandlers() != null) {
			exceptionHandler.addAll(this.decisionStateClone
					.getExceptionHandlers());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			this.decisionStateClone.setId(trimString(getId()));

			if (decisionState.getEntryActions() == null
					&& this.entryActions.size() > 0) {
				EntryActions entry = new EntryActions();
				entry.createNew(decisionStateClone);
				for (IActionElement a : this.entryActions) {
					entry.addEntryAction(a);
				}
				decisionStateClone.setEntryActions(entry);
			}
			else if (this.entryActions.size() == 0) {
				decisionStateClone.setEntryActions(null);
			}
			else {
				decisionStateClone.getEntryActions().removeAll();
				for (IActionElement a : this.entryActions) {
					decisionStateClone.getEntryActions().addEntryAction(a);
				}
			}

			if (decisionState.getExitActions() == null
					&& this.exitActions.size() > 0) {
				ExitActions exit = new ExitActions();
				exit.createNew(decisionStateClone);
				for (IActionElement a : this.exitActions) {
					exit.addExitAction(a);
				}
				decisionStateClone.setExitActions(exit);
			}
			else if (this.exitActions.size() == 0) {
				decisionStateClone.setExitActions(null);
			}
			else {
				decisionStateClone.getExitActions().removeAll();
				for (IActionElement a : this.exitActions) {
					decisionStateClone.getExitActions().addExitAction(a);
				}
			}

			if (this.exceptionHandler != null
					&& this.exceptionHandler.size() > 0) {
				decisionStateClone.removeAllExceptionHandler();
				for (org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler a : this.exceptionHandler) {
					decisionStateClone.addExceptionHandler(a);
				}
			}
			else {
				decisionStateClone.removeAllExceptionHandler();
			}

			((ICloneableModelElement<IDecisionState>) this.decisionState)
					.applyCloneValues(this.decisionStateClone);
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
		if (this.decisionState != null && this.decisionState.getId() != null) {
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
		TabItem item3 = new TabItem(folder, SWT.NULL);
		TabItem item4 = new TabItem(folder, SWT.NULL);
		TabItem item5 = new TabItem(folder, SWT.NULL);
		TabItem item6 = new TabItem(folder, SWT.NULL);

		Group groupActionType = new Group(folder, SWT.NULL);
		GridLayout layoutAttMap = new GridLayout();
		layoutAttMap.marginWidth = 3;
		layoutAttMap.marginHeight = 3;
		groupActionType.setLayout(layoutAttMap);
		groupActionType.setText(" Decision State ");
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
		if (this.decisionState != null && this.decisionState.getId() != null) {
			this.nameText.setText(this.decisionState.getId());
		}
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		Group groupIfsType = new Group(groupActionType, SWT.NULL);
		layoutAttMap = new GridLayout();
		layoutAttMap.marginWidth = 3;
		layoutAttMap.marginHeight = 3;
		groupIfsType.setLayout(layoutAttMap);
		groupIfsType.setText(" Ifs ");
		groupIfsType.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite tableAndButtons = new Composite(groupIfsType, SWT.NONE);
		tableAndButtons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout2 = new GridLayout();
		layout2.marginHeight = 0;
		layout2.marginWidth = 0;
		layout2.numColumns = 2;
		tableAndButtons.setLayout(layout2);

		Table configsTable = new Table(tableAndButtons, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
		// data.widthHint = 250;
		data.heightHint = 145;
		configsTable.setLayoutData(data);
		configsTable.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				handleTableSelectionChanged();
			}
		});

		configsViewer = new TableViewer(configsTable);
		configsViewer.setContentProvider(new IfContentProvider(
				this.decisionStateClone));
		configsViewer.setLabelProvider(new DecoratingLabelProvider(
				new WebflowModelLabelProvider(),
				new WebflowModelLabelDecorator()));
		configsViewer.setInput(this);

		Composite buttonArea = new Composite(tableAndButtons, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonArea.setLayout(layout);
		buttonArea.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		editButton = new Button(buttonArea, SWT.PUSH);
		editButton.setText("Edit");
		GridData data1 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data1.widthHint = 40;
		editButton.setLayoutData(data1);
		editButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) configsViewer
						.getSelection();
				if (selection.getFirstElement() != null) {
					if (selection.getFirstElement() instanceof IIf) {
						IfPropertiesDialog dialog = new IfPropertiesDialog(
								getShell(), decisionState, (IIf) selection
										.getFirstElement(), false);
						if (Dialog.OK == dialog.open()) {
							configsViewer.refresh();
						}
					}
				}
			}
		});

		item1.setControl(groupActionType);

		entryActionsComposite = new ActionComposite(this, item3, getShell(),
				this.entryActions, this.decisionStateClone.getEntryActions(),
				IActionElement.ACTION_TYPE.ENTRY_ACTION);
		item3.setControl(entryActionsComposite.createDialogArea(folder));

		exitActionsComposite = new ActionComposite(this, item4, getShell(),
				this.exitActions, this.decisionStateClone.getExitActions(),
				IActionElement.ACTION_TYPE.EXIT_ACTION);
		item4.setControl(exitActionsComposite.createDialogArea(folder));

		exceptionHandlerComposite = new ExceptionHandlerComposite(this, item5,
				getShell(), this.exceptionHandler, this.decisionStateClone);
		item5.setControl(exceptionHandlerComposite.createDialogArea(folder));

		properties = new PropertiesComposite(this, item6, getShell(),
				(IAttributeEnabled) this.decisionStateClone);
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
		return WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_DECISION_STATE);
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	protected String getMessage() {
		return "Enter the details for the decision state";
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
		return "Decision State";
	}

	/**
	 * 
	 * 
	 * @return 
	 */
	protected String getTitle() {
		return "Decision State properties";
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

	/**
	 * 
	 */
	protected void handleTableSelectionChanged() {
		IStructuredSelection selection = (IStructuredSelection) configsViewer
				.getSelection();
		if (selection.isEmpty()) {
			editButton.setEnabled(false);
		}
		else {
			editButton.setEnabled(true);
		}

	}
}
