/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.web.flow.ui.editor.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.web.flow.core.WebFlowCoreUtils;
import org.springframework.ide.eclipse.web.flow.core.model.IAction;
import org.springframework.ide.eclipse.web.flow.core.model.IActionState;
import org.springframework.ide.eclipse.web.flow.core.model.IBeanReference;
import org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IDescriptionEnabled;
import org.springframework.ide.eclipse.web.flow.core.model.IPropertyEnabled;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowImages;
import org.springframework.ide.eclipse.web.flow.ui.editor.model.WebFlowModelLabelProvider;

public class ActionStatePropertiesDialog extends TitleAreaDialog implements
        IDialogValidator {

    private class ActionContentProvider implements IStructuredContentProvider {

        private IActionState project;

        public ActionContentProvider(IActionState project) {
            this.project = project;
        }

        public void dispose() {
        }

        public Object[] getElements(Object obj) {
            return project.getActions().toArray();
        }

        public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
        }
    }

    private IActionState actionState;

    private IActionState actionStateClone;

    private BeanReferencePropertiesComposite beanProperties;

    private TableViewer configsViewer;

    private Button editButton;

    private Label nameLabel;

    private Text nameText;

    private Button okButton;

    private IWebFlowModelElement parent;

    private PropertiesComposite properties;

    private DescriptionComposite description;

    public ActionStatePropertiesDialog(Shell parentShell,
            IWebFlowModelElement parent, IActionState state) {
        super(parentShell);
        this.actionState = state;
        this.parent = parent;
        this.actionStateClone = (IActionState) ((ICloneableModelElement) state)
                .cloneModelElement();
    }

    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            this.actionStateClone.setId(trimString(getId()));
            if (this.beanProperties.useBeanReference()) {
                if (this.beanProperties.getRadioBeanRef()) {
                    this.actionStateClone.setBean(this.beanProperties
                            .getBeanText());
                    this.actionStateClone.setBeanClass(null);
                    this.actionStateClone.setAutowire(null);
                    this.actionStateClone.setClassRef(null);
                } else if (this.beanProperties.getRadioClass()) {
                    this.actionStateClone.setBean(null);
                    this.actionStateClone
                            .setBeanClass(trimString(this.beanProperties
                                    .getClassText()));
                    this.actionStateClone
                            .setAutowire(trimString(this.beanProperties
                                    .getAutowireText()));
                    this.actionStateClone.setClassRef(null);
                } else if (this.beanProperties.getRadioClassRef()) {
                    this.actionStateClone.setBean(null);
                    this.actionStateClone.setBeanClass(null);
                    this.actionStateClone.setAutowire(null);
                    this.actionStateClone.setClassRef(this.beanProperties
                            .getClassRefText());
                }
            } else {
                this.actionStateClone.setBean(null);
                this.actionStateClone.setBeanClass(null);
                this.actionStateClone.setAutowire(null);
                this.actionStateClone.setClassRef(null);
            }

            if (this.actionStateClone instanceof IDescriptionEnabled) {
                ((IDescriptionEnabled) this.actionStateClone)
                        .setDescription(description.getDescription());
            }

            ((ICloneableModelElement) this.actionState)
                    .applyCloneValues((ICloneableModelElement) this.actionStateClone);
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
        okButton = createButton(parent, IDialogConstants.OK_ID,
                IDialogConstants.OK_LABEL, true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
        // do this here because setting the text will set enablement on the
        // ok button
        nameText.setFocus();
        if (this.actionState != null && this.actionState.getId() != null) {
            okButton.setEnabled(true);
        } else {
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
        item2.setText("Actions");
        item2.setImage(WebFlowImages.getImage(WebFlowImages.IMG_OBJS_ACTION));
        TabItem item3 = new TabItem(folder, SWT.NULL);
        TabItem item4 = new TabItem(folder, SWT.NULL);
        TabItem item5 = new TabItem(folder, SWT.NULL);

        Composite nameGroup = new Composite(folder, SWT.NULL);
        nameGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout1 = new GridLayout();
        layout1.numColumns = 2;
        layout1.marginWidth = 5;
        nameGroup.setLayout(layout1);
        nameLabel = new Label(nameGroup, SWT.NONE);
        nameLabel.setText("Id");
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

        item1.setControl(nameGroup);

        Group groupActionType = new Group(folder, SWT.NULL);
        GridLayout layoutAttMap = new GridLayout();
        layoutAttMap.marginWidth = 3;
        layoutAttMap.marginHeight = 3;
        groupActionType.setLayout(layoutAttMap);
        groupActionType.setText(" Actions ");
        groupActionType.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite tableAndButtons = new Composite(groupActionType, SWT.NONE);
        tableAndButtons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout2 = new GridLayout();
        layout2.marginHeight = 0;
        layout2.marginWidth = 0;
        layout2.numColumns = 2;
        tableAndButtons.setLayout(layout2);

        Table configsTable = new Table(tableAndButtons, SWT.MULTI
                | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
        GridData data = new GridData(GridData.FILL_BOTH);
        //data.widthHint = 250;
        data.heightHint = 50;
        configsTable.setLayoutData(data);
        configsTable.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                handleTableSelectionChanged();
            }
        });
        configsViewer = new TableViewer(configsTable);
        configsViewer.setContentProvider(new ActionContentProvider(
                this.actionStateClone));
        configsViewer.setLabelProvider(new WebFlowModelLabelProvider());
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
                    if (selection.getFirstElement() instanceof IAction) {
                        ActionPropertiesDialog dialog = new ActionPropertiesDialog(
                                getShell(), getModelElementParent(),
                                (IAction) selection.getFirstElement());
                        if (Dialog.OK == dialog.open()) {
                            configsViewer.refresh();
                        }
                    }
                }
            }
        });

        item2.setControl(groupActionType);

        beanProperties = new BeanReferencePropertiesComposite(this, item3,
                getShell(), (IBeanReference) this.actionStateClone, false);
        item3.setControl(beanProperties.createDialogArea(folder));

        properties = new PropertiesComposite(this, item4, getShell(),
                (IPropertyEnabled) this.actionStateClone);
        item4.setControl(properties.createDialogArea(folder));

        description = new DescriptionComposite(this, item5, getShell(),
                (IDescriptionEnabled) this.actionStateClone);
        item5.setControl(description.createDialogArea(folder));

        applyDialogFont(parentComposite);

        return parentComposite;
    }

    public String getId() {
        return this.nameText.getText();
    }

    protected Image getImage() {
        return WebFlowImages.getImage(WebFlowImages.IMG_OBJS_ACTION_STATE);
    }

    protected String getMessage() {
        return "Enter the details for the action state";
    }

    public IWebFlowModelElement getModelElementParent() {
        return this.parent;
    }

    protected String getShellTitle() {
        return "Action State";
    }

    protected String getTitle() {
        return "Action State properties";
    }

    /**
     *  
     */
    protected void handleTableSelectionChanged() {
        IStructuredSelection selection = (IStructuredSelection) configsViewer
                .getSelection();
        if (selection.isEmpty()) {
            this.editButton.setEnabled(false);
        } else {
            this.editButton.setEnabled(true);
        }
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
        error = this.beanProperties.validateInput(errorMessage);

        if (id == null || "".equals(id)) {
            errorMessage.append("A valid id attribute is required. ");
            error = true;
        } else {
            if (WebFlowCoreUtils.isIdAlreadyChoosenByAnotherState(parent,
                    actionState, id)) {
                errorMessage
                        .append("The entered id attribute must be unique within a single web flow. ");
                error = true;
            }
        }

        if (error) {
            getButton(OK).setEnabled(false);
            setErrorMessage(errorMessage.toString());
        } else {
            getButton(OK).setEnabled(true);
            setErrorMessage(null);
        }
    }
}