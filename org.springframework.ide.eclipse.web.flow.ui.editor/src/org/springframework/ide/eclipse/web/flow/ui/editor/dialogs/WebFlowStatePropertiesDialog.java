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

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.web.flow.core.WebFlowCoreUtils;
import org.springframework.ide.eclipse.web.flow.core.model.IBeanReference;
import org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IAttributeEnabled;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowImages;

public class WebFlowStatePropertiesDialog extends TitleAreaDialog implements
        IDialogValidator {

    private TableViewer configsViewer2;

    private IWebFlowState webflowState;

    private IWebFlowState webflowStateClone;

    private Text nameText;

    private Button okButton;

    private IWebFlowModelElement parent;

    private Button removeButton;

    private BeanReferencePropertiesComposite beanProperties;

    private PropertiesComposite properties;

    public WebFlowStatePropertiesDialog(Shell parentShell,
            IWebFlowModelElement parent, IWebFlowState state) {
        super(parentShell);
        this.webflowState = state;
        this.parent = parent;
        this.webflowStateClone = (IWebFlowState) ((ICloneableModelElement) state)
                .cloneModelElement();
    }

    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            this.webflowStateClone.setId(trimString(getId()));

            ((ICloneableModelElement) this.webflowState)
                    .applyCloneValues((ICloneableModelElement) this.webflowStateClone);
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
        if (this.webflowState != null && this.webflowState.getId() != null) {
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

        GridData gridData = null;
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

        Composite nameGroup = new Composite(folder, SWT.NULL);
        nameGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout1 = new GridLayout();
        layout1.numColumns = 2;
        layout1.marginWidth = 5;
        //layout1.horizontalSpacing = 10;
        //layout1.verticalSpacing = 10;
        nameGroup.setLayout(layout1);
        Label nameLabel = new Label(nameGroup, SWT.NONE);
        nameLabel.setText("Id");
        nameText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
        if (this.webflowState != null && this.webflowState.getId() != null) {
            this.nameText.setText(this.webflowState.getId());
        }
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        nameText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });

        /*Label startState = new Label(nameGroup, SWT.NONE);
        startState.setText("Start State");
        Text startStateText = new Text(nameGroup,  SWT.SINGLE | SWT.BORDER);
        startStateText.setEditable(false);
        startStateText.setText(this.webflowState.getStartState().getId());
        startStateText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));*/
        
        item1.setControl(nameGroup);

        beanProperties = new BeanReferencePropertiesComposite(this, item2,
                getShell(), (IBeanReference) this.webflowStateClone, false);
        item2.setControl(beanProperties.createDialogArea(folder));

        properties = new PropertiesComposite(this, item3, getShell(),
                (IAttributeEnabled) this.webflowStateClone);
        item3.setControl(properties.createDialogArea(folder));

        applyDialogFont(parentComposite);
        return parentComposite;
    }

    public String getId() {
        return this.nameText.getText();
    }

    protected Image getImage() {
        return WebFlowImages.getImage(WebFlowImages.IMG_OBJS_SPRING);
    }

    protected String getMessage() {
        return "Enter the details for the web flow";
    }

    protected String getShellTitle() {
        return "WebFlow";
    }

    protected String getTitle() {
        return "WebFlow properties";
    }

    /**
     *  
     */
    protected void handleTableSelectionChanged() {
        IStructuredSelection selection = (IStructuredSelection) configsViewer2
                .getSelection();
        if (selection.isEmpty()) {
            removeButton.setEnabled(false);
        } else {
            removeButton.setEnabled(true);
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
                    webflowState, id)) {
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