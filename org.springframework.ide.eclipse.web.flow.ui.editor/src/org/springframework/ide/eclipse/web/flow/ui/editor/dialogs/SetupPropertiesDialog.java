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
import org.springframework.ide.eclipse.web.flow.core.model.IBeanReference;
import org.springframework.ide.eclipse.web.flow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.web.flow.core.model.IPropertyEnabled;
import org.springframework.ide.eclipse.web.flow.core.model.ISetup;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowImages;

public class SetupPropertiesDialog extends TitleAreaDialog implements
        IDialogValidator {

    private ISetup setup;

    private ISetup setupClone;

    private BeanReferencePropertiesComposite beanProperties;

    private int LABEL_WIDTH = 70;

    private Label nameLabel;

    private Text nameText;

    private Button okButton;

    private IWebFlowModelElement parent;

    private PropertiesComposite properties;

    public SetupPropertiesDialog(Shell parentShell,
            IWebFlowModelElement parent, ISetup state) {
        super(parentShell);
        this.setup = state;
        this.parent = parent;
        this.setupClone = (ISetup) ((ICloneableModelElement) this.setup)
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
            if (this.beanProperties.useBeanReference()) {
                if (this.beanProperties.getRadioBeanRef()) {
                    this.setupClone.setBean(this.beanProperties.getBeanText());
                    this.setupClone.setBeanClass(null);
                    this.setupClone.setAutowire(null);
                    this.setupClone.setClassRef(null);
                } else if (this.beanProperties.getRadioClass()) {
                    this.setupClone.setBean(null);
                    this.setupClone.setBeanClass(trimString(this.beanProperties
                            .getClassText()));
                    this.setupClone.setAutowire(trimString(this.beanProperties
                            .getAutowireText()));
                    this.setupClone.setClassRef(null);
                } else if (this.beanProperties.getRadioClassRef()) {
                    this.setupClone.setBean(null);
                    this.setupClone.setBeanClass(null);
                    this.setupClone.setAutowire(null);
                    this.setupClone.setClassRef(this.beanProperties
                            .getClassRefText());
                }
            } else {
                this.setupClone.setBean(null);
                this.setupClone.setBeanClass(null);
                this.setupClone.setAutowire(null);
                this.setupClone.setClassRef(null);
            }
            this.setupClone.setMethod(this.beanProperties.getMethodText());
            ((ICloneableModelElement) this.setup)
                    .applyCloneValues((ICloneableModelElement) this.setupClone);
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
        okButton.setEnabled(true);

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
        GridData gridData = null;
        Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        //layout.verticalSpacing = 10;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        TabFolder folder = new TabFolder(composite, SWT.NULL);
        TabItem item2 = new TabItem(folder, SWT.NULL);
        TabItem item3 = new TabItem(folder, SWT.NULL);
        
        beanProperties = new BeanReferencePropertiesComposite(this, item2,
                getShell(), (IBeanReference) this.setupClone, true);
        item2.setControl(beanProperties.createDialogArea(folder));

        properties = new PropertiesComposite(this, item3, getShell(),
                (IPropertyEnabled) this.setupClone);
        item3.setControl(properties.createDialogArea(folder));

        applyDialogFont(parentComposite);

        return parentComposite;
    }

    protected Image getImage() {
        return WebFlowImages.getImage(WebFlowImages.IMG_OBJS_SETUP);
    }

    protected String getMessage() {
        return "Enter the details for the setup";
    }

    public String getName() {
        return this.nameText.getText();
    }

    protected String getShellTitle() {
        return "Setup";
    }

    protected String getTitle() {
        return "Setup properties";
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