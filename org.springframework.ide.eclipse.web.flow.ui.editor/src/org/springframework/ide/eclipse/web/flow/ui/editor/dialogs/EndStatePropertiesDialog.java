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
import org.springframework.ide.eclipse.web.flow.core.model.IDescriptionEnabled;
import org.springframework.ide.eclipse.web.flow.core.model.IEndState;
import org.springframework.ide.eclipse.web.flow.core.model.IPropertyEnabled;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowImages;

public class EndStatePropertiesDialog extends TitleAreaDialog implements
        IDialogValidator {

    private Button addButton;

    private TableViewer configsViewer2;

    private IEndState endState;

    private IEndState endStateClone;

    private Text nameText;

    private Button okButton;

    private IWebFlowModelElement parent;

    private Button removeButton;

    private Text viewText;

    private BeanReferencePropertiesComposite beanProperties;

    private PropertiesComposite properties;
    
    private DescriptionComposite description;

    public EndStatePropertiesDialog(Shell parentShell,
            IWebFlowModelElement parent, IEndState state) {
        super(parentShell);
        this.endState = state;
        this.parent = parent;
        this.endStateClone = (IEndState) ((ICloneableModelElement) state)
                .cloneModelElement();
    }

    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            this.endStateClone.setId(trimString(getId()));
            this.endStateClone.setView(trimString(getView()));
            if (this.beanProperties.useBeanReference()) {
                if (this.beanProperties.getRadioBeanRef()) {
                    this.endStateClone.setBean(this.beanProperties
                            .getBeanText());
                    this.endStateClone.setBeanClass(null);
                    this.endStateClone.setAutowire(null);
                    this.endStateClone.setClassRef(null);
                } else if (this.beanProperties.getRadioClass()) {
                    this.endStateClone.setBean(null);
                    this.endStateClone
                            .setBeanClass(trimString(this.beanProperties
                                    .getClassText()));
                    this.endStateClone
                            .setAutowire(trimString(this.beanProperties
                                    .getAutowireText()));
                    this.endStateClone.setClassRef(null);
                } else if (this.beanProperties.getRadioClassRef()) {
                    this.endStateClone.setBean(null);
                    this.endStateClone.setBeanClass(null);
                    this.endStateClone.setAutowire(null);
                    this.endStateClone.setClassRef(this.beanProperties
                            .getClassRefText());
                }
            } else {
                this.endStateClone.setBean(null);
                this.endStateClone.setBeanClass(null);
                this.endStateClone.setAutowire(null);
                this.endStateClone.setClassRef(null);
            }
            
            if (this.endStateClone instanceof IDescriptionEnabled) {
                ((IDescriptionEnabled) this.endStateClone)
                        .setDescription(description.getDescription());
            }

            ((ICloneableModelElement) this.endState)
                    .applyCloneValues((ICloneableModelElement) this.endStateClone);
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
        if (this.endState != null && this.endState.getId() != null) {
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
        TabItem item4 = new TabItem(folder, SWT.NULL);

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
        if (this.endState != null && this.endState.getId() != null) {
            this.nameText.setText(this.endState.getId());
        }
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        nameText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });

        Label viewLabel = new Label(nameGroup, SWT.NONE);
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

        item1.setControl(nameGroup);

        beanProperties = new BeanReferencePropertiesComposite(this, item2,
                getShell(), (IBeanReference) this.endStateClone, false);
        item2.setControl(beanProperties.createDialogArea(folder));

        properties = new PropertiesComposite(this, item3, getShell(),
                (IPropertyEnabled) this.endStateClone);
        item3.setControl(properties.createDialogArea(folder));
        
        description = new DescriptionComposite(this, item4, getShell(),
                (IDescriptionEnabled) this.endStateClone);
        item4.setControl(description.createDialogArea(folder));

        applyDialogFont(parentComposite);
        return parentComposite;
    }

    public String getId() {
        return this.nameText.getText();
    }

    protected Image getImage() {
        return WebFlowImages.getImage(WebFlowImages.IMG_OBJS_END_STATE);
    }

    protected String getMessage() {
        return "Enter the details for the end state";
    }

    protected String getShellTitle() {
        return "End State";
    }

    protected String getTitle() {
        return "End State properties";
    }

    public String getView() {
        return this.viewText.getText();
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
                    endState, id)) {
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