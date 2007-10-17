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
import org.springframework.ide.eclipse.webflow.core.internal.model.GlobalTransitions;
import org.springframework.ide.eclipse.webflow.core.internal.model.InputMapper;
import org.springframework.ide.eclipse.webflow.core.internal.model.OutputMapper;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowState;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeEnabled;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IInputAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IMapping;
import org.springframework.ide.eclipse.webflow.core.model.IOutputAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.springframework.ide.eclipse.webflow.ui.editor.outline.webflow.WebflowUIImages;

/**
 * 
 */
public class WebflowStatePropertiesDialog extends TitleAreaDialog implements
		IDialogValidator {

	/**
	 * 
	 */
	private IWebflowState state;

	/**
	 * 
	 */
	private IWebflowState stateClone;

	/**
	 * 
	 */
	private Label startStateLabel;

	/**
	 * 
	 */
	private Text startStateText;

	/**
	 * 
	 */
	private Button okButton;

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
	private OutputMapperComposite outputMapperComposite;

	/**
	 * 
	 */
	private InputMapperComposite inputMapperComposite;

	private VarComposite varComposite;

	private ImportComposite importComposite;

	private GlobalTransitionsComposite globalTransitionComposite;

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
	private List<IStateTransition> globalTransitions;

	/**
	 * 
	 */
	private List<IOutputAttribute> outputAttributes;

	/**
	 * 
	 */
	private List<IMapping> outputMapping;

	/**
	 * 
	 */
	private List<IInputAttribute> inputAttributes;

	/**
	 * 
	 */
	private List<IMapping> inputMapping;

	/**
	 * 
	 */
	private List<org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler> exceptionHandler;

	/**
	 * @param parentShell
	 * @param state
	 */
	public WebflowStatePropertiesDialog(Shell parentShell, IWebflowState state) {
		super(parentShell);
		this.state = state;
		this.stateClone = ((WebflowState) state).cloneModelElement();
		// TODO add handling for var, import, global-transistion

		if (this.stateClone.getEntryActions() != null) {
			entryActions = new ArrayList<IActionElement>();
			entryActions.addAll(this.stateClone.getEntryActions()
					.getEntryActions());
		}
		else {
			entryActions = new ArrayList<IActionElement>();
			EntryActions entry = new EntryActions();
			entry.createNew(stateClone);
			stateClone.setEntryActions(entry);
		}
		if (this.stateClone.getExitActions() != null) {
			exitActions = new ArrayList<IActionElement>();
			exitActions.addAll(this.stateClone.getExitActions()
					.getExitActions());
		}
		else {
			exitActions = new ArrayList<IActionElement>();
			ExitActions exit = new ExitActions();
			exit.createNew(stateClone);
			stateClone.setExitActions(exit);
		}

		outputAttributes = new ArrayList<IOutputAttribute>();
		outputMapping = new ArrayList<IMapping>();
		inputAttributes = new ArrayList<IInputAttribute>();
		inputMapping = new ArrayList<IMapping>();

		if (this.stateClone.getOutputMapper() != null) {
			outputAttributes = new ArrayList<IOutputAttribute>();
			outputMapping = new ArrayList<IMapping>();
			outputAttributes.addAll(this.stateClone.getOutputMapper()
					.getOutputAttributes());
			outputMapping
					.addAll(this.stateClone.getOutputMapper().getMapping());
		}
		else {
			outputAttributes = new ArrayList<IOutputAttribute>();
			outputMapping = new ArrayList<IMapping>();
			OutputMapper entry = new OutputMapper();
			entry.createNew(stateClone);
			stateClone.setOutputMapper(entry);
		}

		if (this.stateClone.getInputMapper() != null) {
			inputAttributes = new ArrayList<IInputAttribute>();
			inputMapping = new ArrayList<IMapping>();
			inputAttributes.addAll(this.stateClone.getInputMapper()
					.getInputAttributes());
			inputMapping.addAll(this.stateClone.getInputMapper().getMapping());
		}
		else {
			inputAttributes = new ArrayList<IInputAttribute>();
			inputMapping = new ArrayList<IMapping>();
			InputMapper entry = new InputMapper();
			entry.createNew(stateClone);
			stateClone.setInputMapper(entry);
		}

		if (this.stateClone.getGlobalTransitions() != null) {
			globalTransitions = new ArrayList<IStateTransition>();
			globalTransitions.addAll(this.stateClone.getGlobalTransitions()
					.getGlobalTransitions());
		}
		else {
			globalTransitions = new ArrayList<IStateTransition>();
			GlobalTransitions entry = new GlobalTransitions();
			entry.createNew(stateClone);
			stateClone.setGlobalTransitions(entry);
		}

		exceptionHandler = new ArrayList<org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler>();
		if (this.stateClone.getExceptionHandlers() != null) {
			exceptionHandler.addAll(this.stateClone.getExceptionHandlers());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {

			if (state.getEntryActions() == null && this.entryActions.size() > 0) {
				EntryActions entry = new EntryActions();
				entry.createNew(stateClone);
				for (IActionElement a : this.entryActions) {
					entry.addEntryAction(a);
				}
				stateClone.setEntryActions(entry);
			}
			else if (this.entryActions.size() == 0) {
				stateClone.setEntryActions(null);
			}
			else {
				stateClone.getEntryActions().removeAll();
				for (IActionElement a : this.entryActions) {
					stateClone.getEntryActions().addEntryAction(a);
				}
			}

			if (state.getExitActions() == null && this.exitActions.size() > 0) {
				ExitActions exit = new ExitActions();
				exit.createNew(stateClone);
				for (IActionElement a : this.exitActions) {
					exit.addExitAction(a);
				}
				stateClone.setExitActions(exit);
			}
			else if (this.exitActions.size() == 0) {
				stateClone.setExitActions(null);
			}
			else {
				stateClone.getExitActions().removeAll();
				for (IActionElement a : this.exitActions) {
					stateClone.getExitActions().addExitAction(a);
				}
			}

			if (this.exceptionHandler != null
					&& this.exceptionHandler.size() > 0) {
				stateClone.removeAllExceptionHandler();
				for (org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler a : this.exceptionHandler) {
					stateClone.addExceptionHandler(a);
				}
			}
			else {
				stateClone.removeAllExceptionHandler();
			}

			if (state.getOutputMapper() == null
					&& (this.outputAttributes.size() > 0 || this.outputMapping
							.size() > 0)) {
				OutputMapper entry = new OutputMapper();
				entry.createNew(stateClone);
				for (IInputAttribute a : this.outputAttributes) {
					entry.addOutputAttribute((IOutputAttribute) a);
				}
				for (IMapping a : this.outputMapping) {
					entry.addMapping(a);
				}
				stateClone.setOutputMapper(entry);
			}
			else if (this.outputAttributes.size() == 0
					&& this.outputMapping.size() == 0) {
				stateClone.setOutputMapper(null);
			}
			else {
				stateClone.getOutputMapper().removeAllOutputAttribute();
				stateClone.getOutputMapper().removeAllMapping();
				for (IInputAttribute a : this.outputAttributes) {
					stateClone.getOutputMapper().addOutputAttribute(
							(IOutputAttribute) a);
				}
				for (IMapping a : this.outputMapping) {
					stateClone.getOutputMapper().addMapping(a);
				}
			}

			if (state.getInputMapper() == null
					&& (this.inputAttributes.size() > 0 || this.inputMapping
							.size() > 0)) {
				InputMapper entry = new InputMapper();
				entry.createNew(stateClone);
				for (IInputAttribute a : this.inputAttributes) {
					entry.addInputAttribute(a);
				}
				for (IMapping a : this.inputMapping) {
					entry.addMapping(a);
				}
				stateClone.setInputMapper(entry);
			}
			else if (this.inputAttributes.size() == 0
					&& this.inputMapping.size() == 0) {
				stateClone.setInputMapper(null);
			}
			else {
				stateClone.getInputMapper().removeAllInputAttribute();
				stateClone.getInputMapper().removeAllMapping();
				for (IInputAttribute a : this.inputAttributes) {
					stateClone.getInputMapper().addInputAttribute(a);
				}
				for (IMapping a : this.inputMapping) {
					stateClone.getInputMapper().addMapping(a);
				}
			}

			if (state.getGlobalTransitions() == null
					&& this.globalTransitions.size() > 0) {
				GlobalTransitions entry = new GlobalTransitions();
				entry.createNew(stateClone);
				for (IStateTransition a : this.globalTransitions) {
					entry.addGlobalTransition(a);
				}
				stateClone.setGlobalTransitions(entry);
			}
			else if (this.globalTransitions.size() == 0) {
				stateClone.setGlobalTransitions(null);
			}
			else {
				stateClone.getGlobalTransitions().removeAll();
				for (IStateTransition a : this.globalTransitions) {
					stateClone.getGlobalTransitions().addGlobalTransition(a);
				}
			}

			((ICloneableModelElement<IWebflowState>) this.state)
					.applyCloneValues(this.stateClone);
		}
		super.buttonPressed(buttonId);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(getShellTitle());
		shell.setImage(getImage());
	}

	/*
	 * (non-Javadoc)
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
		if (this.state != null) {
			okButton.setEnabled(true);
		}
		else {
			okButton.setEnabled(false);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		setTitle(getTitle());
		setMessage(getMessage());
		return contents;
	}

	/*
	 * (non-Javadoc)
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
		TabItem item7 = new TabItem(folder, SWT.NULL);
		TabItem item8 = new TabItem(folder, SWT.NULL);
		TabItem item9 = new TabItem(folder, SWT.NULL);

		Group groupActionType = new Group(folder, SWT.NULL);
		GridLayout layoutAttMap = new GridLayout();
		layoutAttMap.marginWidth = 3;
		layoutAttMap.marginHeight = 3;
		groupActionType.setLayout(layoutAttMap);
		groupActionType.setText(" Web Flow ");
		GridData grid = new GridData();
		groupActionType.setLayoutData(grid);

		Composite nameGroup = new Composite(groupActionType, SWT.NULL);
		nameGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout1 = new GridLayout();
		layout1.numColumns = 2;
		layout1.marginWidth = 5;
		nameGroup.setLayout(layout1);
		startStateLabel = new Label(nameGroup, SWT.NONE);
		startStateLabel.setText("Start State id");
		startStateText = new Text(nameGroup, SWT.READ_ONLY | SWT.SINGLE
				| SWT.BORDER);
		if (this.state != null && this.state.getStartState() != null
				&& this.state.getStartState().getId() != null) {
			this.startStateText.setText(this.state.getStartState().getId());
		}
		startStateText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		/*
		 * startStateText.addModifyListener(new ModifyListener() { public void
		 * modifyText(ModifyEvent e) { validateInput(); } });
		 */

		item1.setControl(groupActionType);

		// add attribute mapper
		item4.setText("Attribute Mapping");
		item4.setImage(WebflowUIImages
				.getImage(WebflowUIImages.IMG_OBJS_ATTRIBUTE_MAPPER));

		Composite attributeMapperGroup = new Composite(folder, SWT.NULL);
		attributeMapperGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		layout1 = new GridLayout();
		layout1.numColumns = 1;
		layout1.marginWidth = 0;
		layout1.marginHeight = 0;
		attributeMapperGroup.setLayout(layout1);

		TabFolder folder2 = new TabFolder(attributeMapperGroup, SWT.NULL);
		folder2.setLayoutData(new GridData(GridData.FILL_BOTH));
		TabItem item21 = new TabItem(folder2, SWT.NULL);
		TabItem item22 = new TabItem(folder2, SWT.NULL);

		inputMapperComposite = new InputMapperComposite(this, item21,
				getShell(), this.inputAttributes, this.inputMapping,
				this.stateClone.getInputMapper());
		item21.setControl(inputMapperComposite.createDialogArea(folder2));

		outputMapperComposite = new OutputMapperComposite(this, item22,
				getShell(), this.outputAttributes, this.outputMapping,
				this.stateClone.getOutputMapper());
		item22.setControl(outputMapperComposite.createDialogArea(folder2));

		item4.setControl(attributeMapperGroup);

		properties = new PropertiesComposite(this, item2, getShell(),
				(IAttributeEnabled) this.stateClone);
		item2.setControl(properties.createDialogArea(folder));

		varComposite = new VarComposite(this, item3, getShell(),
				this.stateClone);
		item3.setControl(varComposite.createDialogArea(folder));

		entryActionsComposite = new ActionComposite(this, item5, getShell(),
				this.entryActions, this.stateClone.getEntryActions(),
				IActionElement.ACTION_TYPE.ENTRY_ACTION);
		item5.setControl(entryActionsComposite.createDialogArea(folder));

		globalTransitionComposite = new GlobalTransitionsComposite(this, item6,
				getShell(), this.globalTransitions, this.stateClone
						.getGlobalTransitions(), this.stateClone);
		item6.setControl(globalTransitionComposite.createDialogArea(folder));

		exitActionsComposite = new ActionComposite(this, item7, getShell(),
				this.exitActions, this.stateClone.getExitActions(),
				IActionElement.ACTION_TYPE.EXIT_ACTION);
		item7.setControl(exitActionsComposite.createDialogArea(folder));

		exceptionHandlerComposite = new ExceptionHandlerComposite(this, item8,
				getShell(), this.exceptionHandler, this.stateClone);
		item8.setControl(exceptionHandlerComposite.createDialogArea(folder));

		importComposite = new ImportComposite(this, item9, getShell(),
				this.stateClone);
		item9.setControl(importComposite.createDialogArea(folder));

		applyDialogFont(parentComposite);

		return parentComposite;
	}

	/**
	 * @return
	 */
	protected Image getImage() {
		return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_WEBFLOW);
	}

	/**
	 * @return
	 */
	protected String getMessage() {
		return "Enter the details for the web flow";
	}

	/**
	 * @return
	 */
	protected String getShellTitle() {
		return "Flow";
	}

	/**
	 * @return
	 */
	protected String getTitle() {
		return "Flow properties";
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

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.webflow.ui.graph.dialogs.IDialogValidator#validateInput()
	 */
	public void validateInput() {
		String id = "";
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
