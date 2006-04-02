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
import org.springframework.ide.eclipse.web.flow.core.model.IAction;
import org.springframework.ide.eclipse.web.flow.core.model.IBeanReference;
import org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IAttributeEnabled;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowImages;

public class ActionPropertiesDialog extends TitleAreaDialog implements
        IDialogValidator {

    private IAction action;

    private IAction actionClone;

    private BeanReferencePropertiesComposite beanProperties;

    private Label nameLabel;

    private Text nameText;

    private Button okButton;

    private PropertiesComposite properties;

    public ActionPropertiesDialog(Shell parentShell,
            IWebFlowModelElement parent, IAction state) {
        super(parentShell);
        this.action = state;
        this.actionClone = (IAction) ((ICloneableModelElement) this.action)
                .cloneModelElement();

    }

    private String trimString(String string) {
        if (string != null && string == "") {
            string = null;
        }
        return string;
    }

    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            this.actionClone.setName(trimString(getName()));
            if (this.beanProperties.useBeanReference()) {
                if (this.beanProperties.getRadioBeanRef()) {
                    this.actionClone.setBean(this.beanProperties.getBeanText());
                } else if (this.beanProperties.getRadioClass()) {
                    this.actionClone.setBean(null);
                } else if (this.beanProperties.getRadioClassRef()) {
                    this.actionClone.setBean(null);
                }
            } else {
                this.actionClone.setBean(null);
            }
            this.actionClone.setMethod(this.beanProperties.getMethodText());
            ((ICloneableModelElement) this.action)
                    .applyCloneValues((ICloneableModelElement) this.actionClone);
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
        if (this.action != null && this.action.getName() != null) {
            okButton.setEnabled(true);
        } else {
            okButton.setEnabled(false);
        }

        this.validateInput();
    }

    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        setTitle(getTitle());
        setMessage(getMessage());
        return contents;
    }

    protected Control createDialogArea(Composite parent) {
        Composite parentComposite = (Composite) super.createDialogArea(parent);
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        //layout.verticalSpacing = 10;
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

        item1.setControl(nameGroup);
        beanProperties = new BeanReferencePropertiesComposite(this, item2,
                getShell(), (IBeanReference) this.actionClone, true);
        item2.setControl(beanProperties.createDialogArea(folder));

        properties = new PropertiesComposite(this, item3, getShell(),
                (IAttributeEnabled) this.actionClone);
        item3.setControl(properties.createDialogArea(folder));

        applyDialogFont(parentComposite);

        return parentComposite;
    }

    protected Image getImage() {
        return WebFlowImages.getImage(WebFlowImages.IMG_OBJS_ACTION);
    }

    protected String getMessage() {
        return "Enter the details for the action";
    }

    public String getName() {
        return this.nameText.getText();
    }

    protected String getShellTitle() {
        return "Action";
    }

    protected String getTitle() {
        return "Action properties";
    }

    protected void showError(String error) {
        super.setErrorMessage(error);
    }

    public void validateInput() {
        boolean error = false;
        StringBuffer errorMessage = new StringBuffer();
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