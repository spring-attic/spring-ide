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
import org.springframework.ide.eclipse.webflow.core.internal.model.EndState;
import org.springframework.ide.eclipse.webflow.core.internal.model.EntryActions;
import org.springframework.ide.eclipse.webflow.core.internal.model.OutputMapper;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelXmlUtils;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeEnabled;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IEndState;
import org.springframework.ide.eclipse.webflow.core.model.IInputAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IMapping;
import org.springframework.ide.eclipse.webflow.core.model.IOutputAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.editor.outline.webflow.WebflowUIImages;

public class EndStatePropertiesDialog extends TitleAreaDialog implements IDialogValidator {

	private IEndState endState;

	private IEndState endStateClone;

	private Label nameLabel;

	private Text nameText;

	private Label viewLabel;

	private Text viewText;

	private Button okButton;

	private IWebflowModelElement parent;

	private PropertiesComposite properties;

	private ActionComposite entryActionsComposite;

	private OutputMapperComposite outputMapperComposite;

	private ExceptionHandlerComposite exceptionHandlerComposite;

	private List<IActionElement> entryActions;

	private List<IOutputAttribute> outputAttributes;

	private List<IMapping> outputMapping;

	private List<org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler> exceptionHandler;

	private Label parentLabel;

	private Text parentText;

	private Label commitLabel;

	private Button commitText;

	public EndStatePropertiesDialog(Shell parentShell, IWebflowModelElement parent, IEndState state) {
		super(parentShell);
		this.endState = state;
		this.parent = parent;
		this.endStateClone = ((EndState) state).cloneModelElement();
		if (this.endStateClone.getEntryActions() != null) {
			entryActions = new ArrayList<IActionElement>();
			entryActions.addAll(this.endStateClone.getEntryActions().getEntryActions());
		}
		else {
			entryActions = new ArrayList<IActionElement>();
			EntryActions entry = new EntryActions();
			entry.createNew(endStateClone);
			endStateClone.setEntryActions(entry);
		}

		if (this.endStateClone.getOutputMapper() != null) {
			outputAttributes = new ArrayList<IOutputAttribute>();
			outputMapping = new ArrayList<IMapping>();
			outputAttributes.addAll(this.endStateClone.getOutputMapper().getOutputAttributes());
			outputMapping.addAll(this.endStateClone.getOutputMapper().getMapping());
		}
		else {
			outputAttributes = new ArrayList<IOutputAttribute>();
			outputMapping = new ArrayList<IMapping>();
			OutputMapper entry = new OutputMapper();
			entry.createNew(endStateClone);
			endStateClone.setOutputMapper(entry);
		}

		exceptionHandler = new ArrayList<org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler>();
		if (this.endStateClone.getExceptionHandlers() != null) {
			exceptionHandler.addAll(this.endStateClone.getExceptionHandlers());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			this.endStateClone.setId(trimString(getId()));
			this.endStateClone.setView(trimString(viewText.getText()));
			if (!WebflowModelXmlUtils.isVersion1Flow(endState)) {
				this.endStateClone.setParent(trimString(parentText.getText()));
				this.endStateClone.setCommit(Boolean.toString(commitText.getSelection()));
			}

			if (endState.getEntryActions() == null && this.entryActions.size() > 0) {
				EntryActions entry = new EntryActions();
				entry.createNew(endStateClone);
				for (IActionElement a : this.entryActions) {
					entry.addEntryAction(a);
				}
				endStateClone.setEntryActions(entry);
			}
			else if (this.entryActions.size() == 0) {
				endStateClone.setEntryActions(null);
			}
			else {
				endStateClone.getEntryActions().removeAll();
				for (IActionElement a : this.entryActions) {
					endStateClone.getEntryActions().addEntryAction(a);
				}
			}

			if (this.exceptionHandler != null && this.exceptionHandler.size() > 0) {
				endStateClone.removeAllExceptionHandler();
				for (org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler a : this.exceptionHandler) {
					endStateClone.addExceptionHandler(a);
				}
			}
			else {
				endStateClone.removeAllExceptionHandler();
			}

			if (endState.getOutputMapper() == null
					&& (this.outputAttributes.size() > 0 || this.outputMapping.size() > 0)) {
				OutputMapper entry = new OutputMapper();
				entry.createNew(endStateClone);
				for (IInputAttribute a : this.outputAttributes) {
					entry.addOutputAttribute((IOutputAttribute) a);
				}
				for (IMapping a : this.outputMapping) {
					entry.addMapping(a);
				}
				endStateClone.setOutputMapper(entry);
			}
			else if (this.outputAttributes.size() == 0 && this.outputMapping.size() == 0) {
				endStateClone.setOutputMapper(null);
			}
			else {
				endStateClone.getOutputMapper().removeAllOutputAttribute();
				endStateClone.getOutputMapper().removeAllMapping();
				for (IInputAttribute a : this.outputAttributes) {
					endStateClone.getOutputMapper().addOutputAttribute((IOutputAttribute) a);
				}
				for (IMapping a : this.outputMapping) {
					endStateClone.getOutputMapper().addMapping(a);
				}
			}

			((ICloneableModelElement<IEndState>) this.endState)
					.applyCloneValues(this.endStateClone);
		}
		super.buttonPressed(buttonId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(getShellTitle());
		shell.setImage(getImage());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK and Cancel buttons by default
		okButton = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		// do this here because setting the text will set enablement on the
		// ok button
		nameText.setFocus();
		if (this.endState != null && this.endState.getId() != null) {
			okButton.setEnabled(true);
		}
		else {
			okButton.setEnabled(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		setTitle(getTitle());
		setMessage(getMessage());
		return contents;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
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
		groupActionType.setText(" View State ");
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
		if (this.endState != null && this.endState.getId() != null) {
			this.nameText.setText(this.endState.getId());
		}
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		viewLabel = new Label(nameGroup, SWT.NONE);
		viewLabel.setText("View");
		viewText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
		if (this.endState != null && this.endState.getView() != null) {
			this.viewText.setText(this.endState.getView());
		}
		viewText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		viewText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		if (!WebflowModelXmlUtils.isVersion1Flow(endState)) {
			parentLabel = new Label(nameGroup, SWT.NONE);
			parentLabel.setText("Parent state id");
			parentText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
			if (this.endState != null && this.endState.getParent() != null) {
				this.parentText.setText(this.endState.getParent());
			}
			parentText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			parentText.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					validateInput();
				}
			});
			commitLabel = new Label(nameGroup, SWT.NONE);
			commitLabel.setText("Commit");
			commitText = new Button(nameGroup, SWT.CHECK | SWT.BORDER);
			if (this.endState != null && this.endState.getCommit() != null
					&& this.endState.getCommit().equalsIgnoreCase("true")) {
				this.commitText.setSelection(true);
			}
			commitText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		}

		item1.setControl(groupActionType);

		entryActionsComposite = new ActionComposite(this, item3, getShell(), this.entryActions,
				this.endStateClone.getEntryActions(), IActionElement.ACTION_TYPE.ENTRY_ACTION);
		item3.setControl(entryActionsComposite.createDialogArea(folder));

		outputMapperComposite = new OutputMapperComposite(this, item4, getShell(),
				this.outputAttributes, this.outputMapping, this.endStateClone.getOutputMapper());
		item4.setControl(outputMapperComposite.createDialogArea(folder));

		exceptionHandlerComposite = new ExceptionHandlerComposite(this, item5, getShell(),
				this.exceptionHandler, this.endStateClone);
		item5.setControl(exceptionHandlerComposite.createDialogArea(folder));

		properties = new PropertiesComposite(this, item6, getShell(),
				(IAttributeEnabled) this.endStateClone);
		item6.setControl(properties.createDialogArea(folder));

		applyDialogFont(parentComposite);

		return parentComposite;
	}

	public String getId() {
		return this.nameText.getText();
	}

	protected Image getImage() {
		return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_END_STATE);
	}

	protected String getMessage() {
		return "Enter the details for the end state";
	}

	public IWebflowModelElement getModelElementParent() {
		return this.parent;
	}

	protected String getShellTitle() {
		return "End State";
	}

	protected String getTitle() {
		return "End State properties";
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
			errorMessage.append("A valid id attribute is required. ");
			error = true;
		}
		else {
			/*
			 * if (WebFlowCoreUtils.isIdAlreadyChoosenByAnotherState(parent, actionState, id)) {
			 * errorMessage .append("The entered id attribute must be unique within a single web
			 * flow. "); error = true; }
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
