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

import java.util.ArrayList;
import java.util.List;

import ognl.Ognl;
import ognl.OgnlException;

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
import org.springframework.ide.eclipse.web.flow.core.model.IAction;
import org.springframework.ide.eclipse.web.flow.core.model.IBeanReference;
import org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IAttributeEnabled;
import org.springframework.ide.eclipse.web.flow.core.model.IStateTransition;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowImages;
import org.springframework.ide.eclipse.web.flow.ui.editor.model.WebFlowModelLabelProvider;

public class StateTransitionPropertiesDialog extends TitleAreaDialog implements
        IDialogValidator {

    private class ActionContentProvider implements IStructuredContentProvider {

        private List markedForDeletion;

        private IStateTransition project;

        public ActionContentProvider(IStateTransition project, List list) {
            this.project = project;
            this.markedForDeletion = list;
        }

        public void dispose() {
        }

        public Object[] getElements(Object obj) {
            List actions = new ArrayList(project.getActions());
            actions.removeAll(markedForDeletion);
            return actions.toArray();
        }

        public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        }
    }

    private static final String EXPRESSION_PREFIX = "${";

    private static final String EXPRESSION_SUFFIX = "}";

    private List markedForDeletion;

    private Button ognlButton;

    private Button okButton;

    private Text onText;

    private IWebFlowModelElement parent;

    private IStateTransition transition;

    private IStateTransition transitionClone;

    private BeanReferencePropertiesComposite beanProperties;

    private PropertiesComposite properties;

    public StateTransitionPropertiesDialog(Shell parentShell,
            IWebFlowModelElement parent, IStateTransition state) {
        super(parentShell);
        this.transition = state;
        this.parent = parent;
        this.markedForDeletion = new ArrayList();
        this.transitionClone = (IStateTransition) ((ICloneableModelElement) this.transition)
                .cloneModelElement();
    }

    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            this.transitionClone.setOn(trimString(getOn()));
            this.transitionClone.getActions().removeAll(markedForDeletion);

            ((ICloneableModelElement) this.transition)
                    .applyCloneValues((ICloneableModelElement) this.transitionClone);
        }
        super.buttonPressed(buttonId);
    }

    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(getShellTitle());
    }

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
        item1.setImage(WebFlowImages
                .getImage(WebFlowImages.IMG_OBJS_CONNECTION));
        TabItem item2 = new TabItem(folder, SWT.NULL);
        item2.setText("Actions");
        item2.setImage(WebFlowImages.getImage(WebFlowImages.IMG_OBJS_ACTION));
        TabItem item3 = new TabItem(folder, SWT.NULL);
        TabItem item4 = new TabItem(folder, SWT.NULL);

        Composite nameGroup = new Composite(folder, SWT.NULL);
        nameGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout1 = new GridLayout();
        layout1.numColumns = 2;
        layout1.marginWidth = 5;
        nameGroup.setLayout(layout1);

        Label onLabel = new Label(nameGroup, SWT.NONE);
        onLabel.setText("On");
        onText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
        if (this.transition != null && this.transition.getOn() != null) {
            this.onText.setText(this.transition.getOn());
        }
        onText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        onText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });

        Label dummy = new Label(nameGroup, SWT.NONE);
        ognlButton = new Button(nameGroup, SWT.CHECK);
        ognlButton.setText("Parse OGNL transition criteria");
        ognlButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
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
        final TableViewer configsViewer = new TableViewer(configsTable);
        configsViewer.setContentProvider(new ActionContentProvider(
                this.transitionClone, this.markedForDeletion));
        configsViewer.setLabelProvider(new WebFlowModelLabelProvider());
        configsViewer.setInput(this.transitionClone);

        Composite buttonArea = new Composite(tableAndButtons, SWT.NONE);
        GridLayout layoutButtonArea = new GridLayout();
        layoutButtonArea.marginHeight = 0;
        layoutButtonArea.marginWidth = 0;
        buttonArea.setLayout(layoutButtonArea);
        buttonArea.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        Button editButton = new Button(buttonArea, SWT.PUSH);
        editButton.setText("Edit");
        GridData data1 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        data.widthHint = 40;
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
        Button deleteButton = new Button(buttonArea, SWT.PUSH);
        deleteButton.setText("Delete");
        data1 = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        data.widthHint = 40;
        deleteButton.setLayoutData(data1);
        deleteButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                IStructuredSelection selection = (IStructuredSelection) configsViewer
                        .getSelection();
                if (selection.getFirstElement() != null) {
                    if (selection.getFirstElement() instanceof IAction) {
                        markedForDeletion.add((IAction) selection
                                .getFirstElement());
                        configsViewer.refresh();
                    }
                }
            }
        });

        item2.setControl(groupActionType);

        beanProperties = new BeanReferencePropertiesComposite(this, item3,
                getShell(), (IBeanReference) this.transitionClone, false);
        item3.setControl(beanProperties.createDialogArea(folder));

        properties = new PropertiesComposite(this, item4, getShell(),
                (IAttributeEnabled) this.transitionClone);
        item4.setControl(properties.createDialogArea(folder));

        applyDialogFont(parentComposite);

        return parentComposite;
    }

    /**
     * Cut the expression from given criteria string and return it.
     */
    private String cutExpression(String encodedCriteria) {
        return encodedCriteria.substring(EXPRESSION_PREFIX.length(),
                encodedCriteria.length() - EXPRESSION_SUFFIX.length());
    }

    protected String getMessage() {
        return "Enter the details for the state transition";
    }

    public IWebFlowModelElement getModelElementParent() {
        return this.parent;
    }

    public String getOn() {
        return this.onText.getText();
    }

    protected String getShellTitle() {
        return "Transition";
    }

    protected String getTitle() {
        return "Transition properties";
    }

    /**
     *  
     */
    protected void handleTableSelectionChanged() {
        // TODO Auto-generated method stub
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
            } else {
                try {

                    Object obj = Ognl.parseExpression(this.cutExpression(id));
                } catch (OgnlException e) {
                    errorMessage.append("Malformed OGNL expression. ");
                    error = true;
                }
            }
        }
        error = this.beanProperties.validateInput(errorMessage);
        if (error) {
            getButton(OK).setEnabled(false);
            setErrorMessage(errorMessage.toString());
        } else {
            getButton(OK).setEnabled(true);
            setErrorMessage(null);
        }
    }
}