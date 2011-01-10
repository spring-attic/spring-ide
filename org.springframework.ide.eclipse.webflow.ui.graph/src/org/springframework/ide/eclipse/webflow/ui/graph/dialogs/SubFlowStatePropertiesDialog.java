/*******************************************************************************
 * Copyright (c) 2007, 2010 Spring IDE Developers
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.webflow.core.internal.model.AttributeMapper;
import org.springframework.ide.eclipse.webflow.core.internal.model.EntryActions;
import org.springframework.ide.eclipse.webflow.core.internal.model.ExitActions;
import org.springframework.ide.eclipse.webflow.core.internal.model.InputMapper;
import org.springframework.ide.eclipse.webflow.core.internal.model.OutputMapper;
import org.springframework.ide.eclipse.webflow.core.internal.model.SubflowState;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelXmlUtils;
import org.springframework.ide.eclipse.webflow.core.model.IActionElement;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeEnabled;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IInputAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IMapping;
import org.springframework.ide.eclipse.webflow.core.model.IOutputAttribute;
import org.springframework.ide.eclipse.webflow.core.model.ISubflowState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.editor.outline.webflow.WebflowUIImages;
import org.springframework.ide.eclipse.webflow.ui.graph.WebflowUtils;

/**
 * @author Christian Dupuis
 */
public class SubFlowStatePropertiesDialog extends TitleAreaDialog implements IDialogValidator {

	private ISubflowState state;

	private ISubflowState stateClone;

	private Label nameLabel;

	private Text nameText;

	private Label flowLabel;

	private Text flowText;

	private Label attributeMapperBeanLabel;

	private Text attributeMapperBeanText;

	private Button okButton;

	private Button browseBeanButton;

	private Button browseFlowButton;

	private IWebflowModelElement parentElement;

	private PropertiesComposite properties;

	private ActionComposite entryActionsComposite;

	private ActionComposite exitActionsComposite;

	private ExceptionHandlerComposite exceptionHandlerComposite;

	private OutputMapperComposite outputMapperComposite;

	private InputMapperComposite inputMapperComposite;

	private List<IActionElement> entryActions;

	private List<IActionElement> exitActions;

	private List<IOutputAttribute> outputAttributes;

	private List<IMapping> outputMapping;

	private List<IInputAttribute> inputAttributes;

	private List<IMapping> inputMapping;

	private int index = -1;

	private SelectionListener buttonListener = new SelectionAdapter() {

		public void widgetSelected(SelectionEvent e) {
			handleButtonPressed((Button) e.widget);
		}
	};

	private List<org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler> exceptionHandler;

	private Label parentLabel;

	private Text parentText;

	public SubFlowStatePropertiesDialog(Shell parentShell, IWebflowModelElement parent,
			ISubflowState state, int tabIndex) {
		super(parentShell);
		this.state = state;
		this.parentElement = parent;
		this.stateClone = ((SubflowState) state).cloneModelElement();
		if (this.stateClone.getEntryActions() != null) {
			entryActions = new ArrayList<IActionElement>();
			entryActions.addAll(this.stateClone.getEntryActions().getEntryActions());
		}
		else {
			entryActions = new ArrayList<IActionElement>();
			EntryActions entry = new EntryActions();
			entry.createNew(stateClone);
			stateClone.setEntryActions(entry);
		}
		if (this.stateClone.getExitActions() != null) {
			exitActions = new ArrayList<IActionElement>();
			exitActions.addAll(this.stateClone.getExitActions().getExitActions());
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

		if (this.stateClone.getAttributeMapper() != null) {

			if (this.stateClone.getAttributeMapper().getOutputMapper() != null) {
				outputAttributes.addAll(this.stateClone.getAttributeMapper().getOutputMapper()
						.getOutputAttributes());
				outputMapping.addAll(this.stateClone.getAttributeMapper().getOutputMapper()
						.getMapping());
			}
			else {
				OutputMapper o = new OutputMapper();
				o.createNew(this.stateClone.getAttributeMapper());
				this.stateClone.getAttributeMapper().setOutputMapper(o);
			}
			if (this.stateClone.getAttributeMapper().getInputMapper() != null) {
				inputAttributes.addAll(this.stateClone.getAttributeMapper().getInputMapper()
						.getInputAttributes());
				inputMapping.addAll(this.stateClone.getAttributeMapper().getInputMapper()
						.getMapping());
			}
			else {
				InputMapper i = new InputMapper();
				i.createNew(this.stateClone.getAttributeMapper());
				this.stateClone.getAttributeMapper().setInputMapper(i);
			}
		}
		else {

			AttributeMapper mapper = new AttributeMapper();
			mapper.createNew(stateClone);

			OutputMapper o = new OutputMapper();
			o.createNew(mapper);
			mapper.setOutputMapper(o);

			InputMapper i = new InputMapper();
			i.createNew(mapper);
			mapper.setInputMapper(i);

			stateClone.setAttributeMapper(mapper);
			
			if (!WebflowModelXmlUtils.isVersion1Flow(state)) {
				this.outputAttributes.addAll(stateClone.getOutputAttributes());
				this.inputAttributes.addAll(stateClone.getInputAttributes());
			}
		}

		exceptionHandler = new ArrayList<org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler>();
		if (this.stateClone.getExceptionHandlers() != null) {
			exceptionHandler.addAll(this.stateClone.getExceptionHandlers());
		}

		this.index = tabIndex;
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			this.stateClone.setId(trimString(this.nameText.getText()));
			this.stateClone.setFlow(trimString(this.flowText.getText()));

			if (!WebflowModelXmlUtils.isVersion1Flow(state)) {
				this.stateClone.setParent(trimString(parentText.getText()));
			}

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

			if (this.exceptionHandler != null && this.exceptionHandler.size() > 0) {
				stateClone.removeAllExceptionHandler();
				for (org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler a : this.exceptionHandler) {
					stateClone.addExceptionHandler(a);
				}
			}
			else {
				stateClone.removeAllExceptionHandler();
			}

			if (WebflowModelXmlUtils.isVersion1Flow(state)) {
				if (trimString(this.attributeMapperBeanText.getText()) != null
						|| this.inputAttributes.size() > 0 || this.outputAttributes.size() > 0
						|| this.outputMapping.size() > 0 || this.inputMapping.size() > 0) {

					IAttributeMapper mapper = null;

					if (stateClone.getAttributeMapper() == null) {
						mapper = new AttributeMapper();
						mapper.createNew(stateClone);
						stateClone.setAttributeMapper(mapper);
					}
					else {
						mapper = stateClone.getAttributeMapper();
					}

					mapper.setBean(trimString(this.attributeMapperBeanText.getText()));

					if (mapper.getOutputMapper() == null
							&& (this.outputAttributes.size() > 0 || this.outputMapping.size() > 0)) {
						OutputMapper entry = new OutputMapper();
						entry.createNew(mapper);
						for (IInputAttribute a : this.outputAttributes) {
							entry.addOutputAttribute((IOutputAttribute) a);
						}
						for (IMapping a : this.outputMapping) {
							entry.addMapping(a);
						}
						mapper.setOutputMapper(entry);
					}
					else if (this.outputAttributes.size() == 0 && this.outputMapping.size() == 0) {
						mapper.setOutputMapper(null);
					}
					else {
						mapper.getOutputMapper().removeAllOutputAttribute();
						mapper.getOutputMapper().removeAllMapping();
						for (IInputAttribute a : this.outputAttributes) {
							mapper.getOutputMapper().addOutputAttribute((IOutputAttribute) a);
						}
						for (IMapping a : this.outputMapping) {
							mapper.getOutputMapper().addMapping(a);
						}
					}

					if (mapper.getInputMapper() == null
							&& (this.inputAttributes.size() > 0 || this.inputMapping.size() > 0)) {
						InputMapper entry = new InputMapper();
						entry.createNew(mapper);
						for (IInputAttribute a : this.inputAttributes) {
							entry.addInputAttribute(a);
						}
						for (IMapping a : this.inputMapping) {
							entry.addMapping(a);
						}
						mapper.setInputMapper(entry);
					}
					else if (this.inputAttributes.size() == 0 && this.inputMapping.size() == 0) {
						mapper.setInputMapper(null);
					}
					else {
						mapper.getInputMapper().removeAllInputAttribute();
						mapper.getInputMapper().removeAllMapping();
						for (IInputAttribute a : this.inputAttributes) {
							mapper.getInputMapper().addInputAttribute(a);
						}
						for (IMapping a : this.inputMapping) {
							mapper.getInputMapper().addMapping(a);
						}
					}

				}
				else {
					// remove attribute mapper
					stateClone.removeAttributeMapper();
				}
			}
			else {
				if (trimString(this.attributeMapperBeanText.getText()) != null
						|| this.inputAttributes.size() > 0 || this.outputAttributes.size() > 0
						|| this.outputMapping.size() > 0 || this.inputMapping.size() > 0) {

					stateClone.setSubflowAttributeMapper(this.attributeMapperBeanText.getText());

					stateClone.removeAllInputAttribute();
					for (IInputAttribute a : this.inputAttributes) {
						stateClone.addInputAttribute(a);
					}
					stateClone.removeAllOutputAttribute();
					for (IOutputAttribute a : this.outputAttributes) {
						stateClone.addOutputAttribute(a);
					}
					stateClone.removeAttributeMapper();
				}
			}
			((ICloneableModelElement<ISubflowState>) this.state).applyCloneValues(this.stateClone);
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
		if (this.state != null && this.state.getId() != null) {
			okButton.setEnabled(true);
		}
		else {
			okButton.setEnabled(false);
		}
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
		groupActionType.setText(" Subflow State ");
		GridData grid = new GridData();
		groupActionType.setLayoutData(grid);

		Composite nameGroup = new Composite(groupActionType, SWT.NULL);
		nameGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout1 = new GridLayout();
		layout1.numColumns = 3;
		layout1.marginWidth = 5;
		nameGroup.setLayout(layout1);
		nameLabel = new Label(nameGroup, SWT.NONE);
		nameLabel.setText("State id");
		nameText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
		if (this.state != null && this.state.getId() != null) {
			this.nameText.setText(this.state.getId());
		}
		nameText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		new Label(nameGroup, SWT.NONE);

		flowLabel = new Label(nameGroup, SWT.NONE);
		if (WebflowModelXmlUtils.isVersion1Flow(state)) {
			flowLabel.setText("Flow");
		}
		else {
			flowLabel.setText("Subflow");
		}

		// Create a decorated field with a required field decoration.
		DecoratedField flowField = new DecoratedField(nameGroup, SWT.SINGLE | SWT.BORDER,
				new TextControlCreator());
		FieldDecoration requiredFieldIndicator = FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		flowField.addFieldDecoration(requiredFieldIndicator, SWT.TOP | SWT.LEFT, true);
		flowText = (Text) flowField.getControl();
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		flowField.getLayoutControl().setLayoutData(data);
		if (this.state != null && this.state.getFlow() != null) {
			this.flowText.setText(this.state.getFlow());
		}
		flowText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		// add the indent after getting the decorated field
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalIndent = FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();
		nameText.setLayoutData(data);

		DialogUtils.attachContentAssist(flowText, WebflowUtils.getWebflowConfigNames());

		browseFlowButton = new Button(nameGroup, SWT.PUSH);
		browseFlowButton.setText("...");
		browseFlowButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		browseFlowButton.addSelectionListener(buttonListener);

		if (!WebflowModelXmlUtils.isVersion1Flow(state)) {
			parentLabel = new Label(nameGroup, SWT.NONE);
			parentLabel.setText("Parent state id");
			parentText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
			if (this.state != null && this.state.getParent() != null) {
				this.parentText.setText(this.state.getParent());
			}
			parentText.setLayoutData(data);
			parentText.addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					validateInput();
				}
			});

			new Label(nameGroup, SWT.NONE);
		}

		item1.setControl(groupActionType);

		// add attribute mapper

		item2.setText("Attribute Mapper");
		item2.setImage(WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_ATTRIBUTE_MAPPER));

		Composite attributeMapperGroup = new Composite(folder, SWT.NULL);
		attributeMapperGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		layout1 = new GridLayout();
		layout1.numColumns = 1;
		layout1.marginWidth = 0;
		layout1.marginHeight = 0;
		attributeMapperGroup.setLayout(layout1);

		Group attributeMapperType = new Group(attributeMapperGroup, SWT.NULL);
		layoutAttMap = new GridLayout();
		layoutAttMap.marginWidth = 3;
		layoutAttMap.marginHeight = 3;
		attributeMapperType.setText(" Attribute Mapper ");
		attributeMapperType.setLayoutData(new GridData(GridData.FILL_BOTH));
		attributeMapperType.setLayout(layoutAttMap);

		Composite attributeMapperTypeGroup = new Composite(attributeMapperType, SWT.NULL);
		attributeMapperTypeGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		layout1 = new GridLayout();
		layout1.numColumns = 3;
		layout1.marginWidth = 5;
		attributeMapperTypeGroup.setLayout(layout1);

		attributeMapperBeanLabel = new Label(attributeMapperTypeGroup, SWT.NONE);
		attributeMapperBeanLabel.setText("Bean");

		// Create a decorated field with a required field decoration.
		DecoratedField beanField = new DecoratedField(attributeMapperTypeGroup, SWT.SINGLE
				| SWT.BORDER, new TextControlCreator());
		FieldDecoration requiredFieldIndicator3 = FieldDecorationRegistry.getDefault()
				.getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		beanField.addFieldDecoration(requiredFieldIndicator3, SWT.TOP | SWT.LEFT, true);
		attributeMapperBeanText = (Text) beanField.getControl();
		data = new GridData(GridData.FILL_HORIZONTAL);
		beanField.getLayoutControl().setLayoutData(data);
		if (this.state != null && this.state.getAttributeMapper() != null && this.state.getAttributeMapper()
						.getBean() != null) {
			this.attributeMapperBeanText.setText(this.state.getAttributeMapper().getBean());
		}
		if (this.state != null && this.state.getSubflowAttributeMapper() != null) {
			this.attributeMapperBeanText.setText(this.state.getSubflowAttributeMapper());
		}
		attributeMapperBeanText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		DialogUtils.attachContentAssist(attributeMapperBeanText, WebflowUtils
				.getBeansFromEditorInput().toArray());

		browseBeanButton = new Button(attributeMapperTypeGroup, SWT.PUSH);
		browseBeanButton.setText("...");
		browseBeanButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		browseBeanButton.addSelectionListener(buttonListener);

		TabFolder folder2 = new TabFolder(attributeMapperGroup, SWT.NULL);
		folder2.setLayoutData(new GridData(GridData.FILL_BOTH));
		TabItem item21 = new TabItem(folder2, SWT.NULL);
		TabItem item22 = new TabItem(folder2, SWT.NULL);

		inputMapperComposite = new InputMapperComposite(this, item21, getShell(),
				this.inputAttributes, this.inputMapping, this.stateClone.getAttributeMapper()
						.getInputMapper());
		item21.setControl(inputMapperComposite.createDialogArea(folder2));

		outputMapperComposite = new OutputMapperComposite(this, item22, getShell(),
				this.outputAttributes, this.outputMapping, this.stateClone.getAttributeMapper()
						.getOutputMapper());
		item22.setControl(outputMapperComposite.createDialogArea(folder2));

		item2.setControl(attributeMapperGroup);

		entryActionsComposite = new ActionComposite(this, item3, getShell(), this.entryActions,
				this.stateClone.getEntryActions(), IActionElement.ACTION_TYPE.ENTRY_ACTION);
		item3.setControl(entryActionsComposite.createDialogArea(folder));

		exitActionsComposite = new ActionComposite(this, item4, getShell(), this.exitActions,
				this.stateClone.getExitActions(), IActionElement.ACTION_TYPE.EXIT_ACTION);
		item4.setControl(exitActionsComposite.createDialogArea(folder));

		exceptionHandlerComposite = new ExceptionHandlerComposite(this, item5, getShell(),
				this.exceptionHandler, this.stateClone);
		item5.setControl(exceptionHandlerComposite.createDialogArea(folder));

		properties = new PropertiesComposite(this, item6, getShell(),
				(IAttributeEnabled) this.stateClone);
		item6.setControl(properties.createDialogArea(folder));

		applyDialogFont(parentComposite);

		if (this.index >= 0) {
			folder.setSelection(this.index);
		}

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
		return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_SUBFLOW_STATE);
	}

	/**
	 * @return
	 */
	public String getMessage() {
		return "Enter the details for the subflow state";
	}

	/**
	 * @return
	 */
	public IWebflowModelElement getModelElementParent() {
		return this.parentElement;
	}

	/**
	 * @return
	 */
	protected String getShellTitle() {
		return "Subflow State";
	}

	/**
	 * @return
	 */
	protected String getTitle() {
		return "Subflow State properties";
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
	 * 
	 * @see
	 * org.springframework.ide.eclipse.webflow.ui.graph.dialogs.IDialogValidator#validateInput()
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

	/**
	 * @param button
	 */
	private void handleButtonPressed(Button button) {

		if (button.equals(browseBeanButton)) {
			ElementListSelectionDialog dialog = DialogUtils.openBeanReferenceDialog(
					this.attributeMapperBeanText.getText(), false);
			if (Dialog.OK == dialog.open()) {
				this.attributeMapperBeanText.setText(((IBean) dialog.getFirstResult())
						.getElementName());
			}
		}
		else if (button.equals(browseFlowButton)) {
			ElementListSelectionDialog dialog = DialogUtils.openFlowReferenceDialog();
			if (Dialog.OK == dialog.open()) {
				this.flowText.setText((String) dialog.getFirstResult());
			}
		}
	}
}
