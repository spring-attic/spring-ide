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

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.fieldassist.DecoratedField;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.TextControlCreator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.webflow.core.internal.model.Argument;
import org.springframework.ide.eclipse.webflow.core.internal.model.BeanAction;
import org.springframework.ide.eclipse.webflow.core.internal.model.MethodArguments;
import org.springframework.ide.eclipse.webflow.core.internal.model.MethodResult;
import org.springframework.ide.eclipse.webflow.core.model.IArgument;
import org.springframework.ide.eclipse.webflow.core.model.IAttributeEnabled;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.editor.outline.webflow.WebflowUIImages;
import org.springframework.ide.eclipse.webflow.ui.graph.WebflowUtils;

/**
 * @author Christian Dupuis
 */
public class BeanActionPropertiesDialog extends TitleAreaDialog implements
		IDialogValidator {

	/**
	 * 
	 */
	private BeanAction action;

	/**
	 * 
	 */
	private BeanAction actionClone;

	/**
	 * 
	 */
	private Label nameLabel;

	/**
	 * 
	 */
	// private DecoratedField namefield;
	private Text nameText;

	/**
	 * 
	 */
	private Label beanLabel;

	/**
	 * 
	 */
	private Text beanText;

	/**
	 * 
	 */
	private Label methodLabel;

	/**
	 * 
	 */
	private Text methodText;

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
	private Button browseBeanButton;

	/**
	 * 
	 */
	private Button browseMethodButton;

	/**
	 * 
	 */
	private TableViewer configsViewer;

	/**
	 * 
	 */
	private Button removeButton;

	/**
	 * 
	 */
	private Button addButton;

	/**
	 * 
	 */
	private Button editButton;

	/**
	 * 
	 */
	private List<IArgument> methodArguments;

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
	 * @param parentShell
	 * @param state
	 * @param parent
	 */
	public BeanActionPropertiesDialog(Shell parentShell,
			IWebflowModelElement parent, BeanAction state) {
		super(parentShell);
		this.action = state;
		this.actionClone = this.action.cloneModelElement();
		if (this.actionClone.getMethodArguments() != null) {
			methodArguments = new ArrayList<IArgument>();
			methodArguments.addAll(this.actionClone.getMethodArguments()
					.getArguments());
		}
		else {
			methodArguments = new ArrayList<IArgument>();
			MethodArguments entry = new MethodArguments();
			entry.createNew(actionClone);
			actionClone.setMethodArguments(entry);
		}
	}

	/**
	 * @param string
	 * @return
	 */
	private String trimString(String string) {
		if (string != null && string == "") {
			string = null;
		}
		return string;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
	 */
	protected void buttonPressed(int buttonId) {
		if (buttonId == IDialogConstants.OK_ID) {
			this.actionClone.setName(trimString(this.nameText.getText()));
			this.actionClone.setMethod(trimString(this.methodText.getText()));
			this.actionClone.setBean(trimString(this.beanText.getText()));

			if (action.getMethodArguments() == null
					&& this.methodArguments.size() > 0) {
				MethodArguments entry = new MethodArguments();
				entry.createNew(actionClone);
				for (IArgument a : this.methodArguments) {
					entry.addArgument(a);
				}
				actionClone.setMethodArguments(entry);
			}
			else if (this.methodArguments.size() == 0) {
				actionClone.setMethodArguments(null);
			}
			else {
				actionClone.getMethodArguments().removeAll();
				for (IArgument a : this.methodArguments) {
					actionClone.getMethodArguments().addArgument(a);
				}
			}

			if (trimString(this.scopeText.getText()) == null
					&& trimString(this.resultNameText.getText()) == null) {
				this.actionClone.setMethodResult(null);
			}
			else if (this.action.getMethodResult() != null) {
				this.actionClone.getMethodResult().setName(
						this.resultNameText.getText());
				this.actionClone.getMethodResult().setScope(
						this.scopeText.getText());
			}
			else if (this.action.getMethodResult() == null) {
				MethodResult result = new MethodResult();
				result.createNew(actionClone);
				this.actionClone.setMethodResult(result);
				this.actionClone.getMethodResult().setName(
						this.resultNameText.getText());
				this.actionClone.getMethodResult().setScope(
						this.scopeText.getText());
			}

			this.action.applyCloneValues(this.actionClone);
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
		nameText.setFocus();
		if (this.action != null && this.action.getName() != null) {
			okButton.setEnabled(true);
		}
		else {
			okButton.setEnabled(false);
		}

		this.validateInput();
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
		groupActionType.setText(" Action ");
		GridData grid = new GridData();
		groupActionType.setLayoutData(grid);

		Composite nameGroup = new Composite(groupActionType, SWT.NULL);
		nameGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout1 = new GridLayout();
		layout1.numColumns = 3;
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

		new Label(nameGroup, SWT.NONE);

		// Label field.
		beanLabel = new Label(nameGroup, SWT.NONE);
		beanLabel.setText("Bean");
		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.widthHint = LABEL_WIDTH;
		beanLabel.setLayoutData(gridData);

		// Create a decorated field with a required field decoration.
		DecoratedField beanField = new DecoratedField(nameGroup, SWT.SINGLE
				| SWT.BORDER, new TextControlCreator());
		FieldDecoration requiredFieldIndicator = FieldDecorationRegistry
				.getDefault().getFieldDecoration(
						FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		beanField.addFieldDecoration(requiredFieldIndicator,
				SWT.TOP | SWT.LEFT, true);
		beanText = (Text) beanField.getControl();
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		beanField.getLayoutControl().setLayoutData(data);

		if (this.action != null && this.action.getBean() != null) {
			beanText.setText(this.action.getBean());
		}
		beanText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				if (validator != null) {
					validator.validateInput();
				}
			}
		});

		DialogUtils.attachContentAssist(beanText, WebflowUtils
				.getBeansFromEditorInput().toArray());

		browseBeanButton = new Button(nameGroup, SWT.PUSH);
		browseBeanButton.setText("...");
		browseBeanButton.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_END));
		browseBeanButton.addSelectionListener(buttonListener);

		methodLabel = new Label(nameGroup, SWT.NONE);
		methodLabel.setText("Method");

		// Create a decorated field with a required field decoration.
		DecoratedField methodField = new DecoratedField(nameGroup, SWT.SINGLE
				| SWT.BORDER, new TextControlCreator());
		FieldDecoration requiredFieldIndicator1 = FieldDecorationRegistry
				.getDefault().getFieldDecoration(
						FieldDecorationRegistry.DEC_CONTENT_PROPOSAL);
		methodField.addFieldDecoration(requiredFieldIndicator1, SWT.TOP
				| SWT.LEFT, true);
		methodText = (Text) methodField.getControl();
		data = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		methodField.getLayoutControl().setLayoutData(data);

		if (this.action != null && this.action.getMethod() != null) {
			this.methodText.setText(this.action.getMethod());
		}
		methodText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				validateInput();
			}
		});

		DialogUtils.attachContentAssist(methodText, WebflowUtils
				.getActionMethods(this.actionClone.getNode()).toArray());

		browseMethodButton = new Button(nameGroup, SWT.PUSH);
		browseMethodButton.setText("...");
		browseMethodButton.setLayoutData(new GridData(
				GridData.HORIZONTAL_ALIGN_END));
		browseMethodButton.addSelectionListener(buttonListener);

		// add the indent after getting the decorated field
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalIndent = FieldDecorationRegistry.getDefault()
				.getMaximumDecorationWidth();
		nameText.setLayoutData(data);

		Group groupPropertyType = new Group(groupActionType, SWT.NULL);
		GridLayout layoutPropMap = new GridLayout();
		layoutPropMap.marginWidth = 3;
		layoutPropMap.marginHeight = 3;
		groupPropertyType.setLayout(layoutPropMap);
		groupPropertyType.setText(" Method Arguments ");
		groupPropertyType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Composite tableAndButtons = new Composite(groupPropertyType, SWT.NONE);
		tableAndButtons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		GridLayout layout2 = new GridLayout();
		layout2.marginHeight = 0;
		layout2.marginWidth = 0;
		layout2.numColumns = 2;
		tableAndButtons.setLayout(layout2);

		Table configsTable = new Table(tableAndButtons, SWT.MULTI
				| SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		data = new GridData(GridData.FILL_BOTH);
		// data.widthHint = 250;
		data.heightHint = 70;
		configsTable.setLayoutData(data);
		TableColumn columnName = new TableColumn(configsTable, SWT.NONE);
		columnName.setText("Expression");
		columnName.setWidth(150);
		TableColumn columnType = new TableColumn(configsTable, SWT.NONE);
		columnType.setText("Type");
		columnType.setWidth(200);
		configsTable.setHeaderVisible(true);

		configsViewer = new TableViewer(configsTable);
		String[] columnNames = new String[] { "Expression", "Type" };
		configsViewer.setColumnProperties(columnNames);
		configsViewer.setContentProvider(new MethodArgumentContentProvider(
				this.methodArguments, configsViewer));
		configsViewer.setLabelProvider(new ModelTableLabelProvider());
		configsViewer.setCellModifier(new TableCellModifier());
		configsViewer.setInput(this.action);
		configsTable.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				handleTableSelectionChanged();
			}
		});
		Composite buttonArea = new Composite(tableAndButtons, SWT.NONE);
		GridLayout layout4 = new GridLayout();
		layout4.marginHeight = 0;
		layout4.marginWidth = 0;
		buttonArea.setLayout(layout4);
		buttonArea.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		addButton = new Button(buttonArea, SWT.PUSH);
		addButton.setText("Add");
		GridData data1 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data1.widthHint = 40;
		addButton.setLayoutData(data1);
		addButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				IArgument property = new Argument();
				property.createNew(actionClone);
				MethodArgumentEditorDialog dialog = new MethodArgumentEditorDialog(
						getParentShell(), property);
				if (dialog.open() == Dialog.OK) {
					methodArguments.add(property);
					configsViewer.refresh(true);
				}
			}
		});
		editButton = new Button(buttonArea, SWT.PUSH);
		editButton.setText("Edit");
		data1 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data1.widthHint = 40;
		editButton.setLayoutData(data1);
		editButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) configsViewer
						.getSelection();
				if (selection.getFirstElement() != null) {
					if (selection.getFirstElement() instanceof IArgument) {
						IArgument property = (IArgument) selection
								.getFirstElement();
						MethodArgumentEditorDialog dialog = new MethodArgumentEditorDialog(
								getParentShell(), property);
						if (dialog.open() == Dialog.OK) {
							configsViewer.refresh(true);
						}
					}
				}
			}
		});

		removeButton = new Button(buttonArea, SWT.PUSH);
		removeButton.setText("Delete");
		GridData data2 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		data1.widthHint = 40;
		removeButton.setLayoutData(data2);
		removeButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection) configsViewer
						.getSelection();
				if (selection.getFirstElement() != null) {
					if (selection.getFirstElement() instanceof IArgument) {
						IArgument property = (IArgument) selection
								.getFirstElement();
						methodArguments.remove(property);
						configsViewer.refresh(true);
					}
				}
			}
		});
		removeButton.setEnabled(false);
		editButton.setEnabled(false);

		Group groupMethodResult = new Group(groupActionType, SWT.NULL);
		layoutAttMap = new GridLayout();
		layoutAttMap.marginWidth = 3;
		layoutAttMap.marginHeight = 3;
		layoutAttMap.numColumns = 3;
		layoutAttMap.marginWidth = 5;
		groupMethodResult.setLayout(layoutAttMap);
		groupMethodResult.setText(" Method Result ");
		groupMethodResult.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		resultNameLabel = new Label(groupMethodResult, SWT.NONE);
		resultNameLabel.setText("Name");
		resultNameText = new Text(groupMethodResult, SWT.SINGLE | SWT.BORDER);
		if (this.action != null && this.action.getMethodResult() != null) {
			this.resultNameText
					.setText(this.action.getMethodResult().getName());
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
		gridData = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gridData.widthHint = LABEL_WIDTH;
		scopeLabel.setLayoutData(gridData);

		// Add the text box for action classname.
		scopeText = new Combo(groupMethodResult, SWT.SINGLE | SWT.BORDER
				| SWT.READ_ONLY);
		scopeText.setItems(new String[] { "", "request", "flash", "flow",
				"conversation", "default" });
		if (this.action != null && this.action.getMethodResult() != null
				&& this.action.getMethodResult().getScope() != null) {
			scopeText.setText(this.action.getMethodResult().getScope());
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
	 * @return
	 */
	protected Image getImage() {
		return WebflowUIImages.getImage(WebflowUIImages.IMG_OBJS_BEAN_ACTION);
	}

	/**
	 * @return
	 */
	public String getMessage() {
		return "Enter the details for the Bean action";
	}

	/**
	 * @return
	 */
	public String getName() {
		return this.nameText.getText();
	}

	/**
	 * @return
	 */
	protected String getShellTitle() {
		return "Bean Action";
	}

	/**
	 * @return
	 */
	protected String getTitle() {
		return "Bean Action properties";
	}

	/**
	 * @param error
	 */
	protected void showError(String error) {
		super.setErrorMessage(error);
	}

	/*
	 * (non-Javadoc)
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

	/**
	 * @param button
	 */
	private void handleButtonPressed(Button button) {

		if (button.equals(browseBeanButton)) {
			ElementListSelectionDialog dialog = DialogUtils
					.openBeanReferenceDialog(this.beanText.getText(), false);
			if (Dialog.OK == dialog.open()) {
				this.beanText.setText(((IBean) dialog.getFirstResult())
						.getElementName());
			}
		}
		else if (button.equals(browseMethodButton)) {
			ElementListSelectionDialog dialog = DialogUtils
					.openActionMethodReferenceDialog(this.actionClone.getNode());
			if (Dialog.OK == dialog.open()) {
				this.methodText.setText(((IMethod) dialog.getFirstResult())
						.getElementName());
			}
		}
	}

	/**
	 * The user has selected a different configuration in table. Update button
	 * enablement.
	 */
	private void handleTableSelectionChanged() {
		IStructuredSelection selection = (IStructuredSelection) configsViewer
				.getSelection();
		if (selection.isEmpty()) {
			removeButton.setEnabled(false);
			editButton.setEnabled(false);
		}
		else {
			removeButton.setEnabled(true);
			editButton.setEnabled(true);
		}
	}
}
