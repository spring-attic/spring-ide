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

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.ui.dialogs.TypeSelectionDialog;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.springframework.ide.eclipse.web.flow.core.model.IProperty;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowImages;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowUtils;

public class PropertyEditorDialog extends TitleAreaDialog implements
        IDialogValidator {

    private IProperty property;

    private SelectionListener buttonListener = new SelectionAdapter() {

        public void widgetSelected(SelectionEvent e) {
            handleButtonPressed((Button) e.widget);
        }
    };

    private int LABEL_WIDTH = 70;

    private Label nameLabel;

    private Text nameText;

    private Label valueLabel;

    private Text valueText;

    private Label typeLabel;

    private Combo typeText;

    private Button okButton;

    public PropertyEditorDialog(Shell parentShell, IProperty state) {
        super(parentShell);
        this.property = state;
    }

    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            this.property.setName(trimString(this.nameText.getText()));
            this.property.setValue(trimString(this.valueText.getText()));
            this.property.setType(trimString(this.typeText.getText()));
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
        if (this.property != null && this.property.getName() != null
                && this.property.getValue() != null) {
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
        GridData gridData = null;
        Composite composite = new Composite(parentComposite, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite nameGroup = new Composite(composite, SWT.NULL);
        nameGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout1 = new GridLayout();
        layout1.numColumns = 3;
        layout1.marginWidth = 5;
        nameGroup.setLayout(layout1);
        nameLabel = new Label(nameGroup, SWT.NONE);
        nameLabel.setText("Name");
        nameText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
        if (this.property != null && this.property.getName() != null) {
            this.nameText.setText(this.property.getName());
        }
        nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        nameText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });

        Label dummy1 = new Label(nameGroup, SWT.NONE);

        valueLabel = new Label(nameGroup, SWT.NONE);
        valueLabel.setText("Value");
        valueText = new Text(nameGroup, SWT.SINGLE | SWT.BORDER);
        if (this.property != null && this.property.getValue() != null) {
            this.valueText.setText(this.property.getValue());
        }
        valueText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        valueText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });

        Label dummy2 = new Label(nameGroup, SWT.NONE);

        typeLabel = new Label(nameGroup, SWT.NONE);
        typeLabel.setText("Type");
        typeText = new Combo(nameGroup, SWT.DROP_DOWN);
        typeText.setItems(new String[] { "char", "byte", "short", "int",
                "long", "float", "double", "boolean" });
        if (this.property != null && this.property.getType() != null) {
            this.typeText.setText(this.property.getType());
        }
        typeText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        typeText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validateInput();
            }
        });

        Button browseType = new Button(nameGroup, SWT.PUSH);
        browseType.setText("...");
        browseType.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        browseType.addSelectionListener(buttonListener);

        applyDialogFont(parentComposite);

        return parentComposite;
    }

    public String getId() {
        return this.nameText.getText();
    }

    protected Image getImage() {
        return WebFlowImages.getImage(WebFlowImages.IMG_OBJS_PROPERTIES);
    }

    protected String getMessage() {
        return "Enter the details for the property";
    }

    protected String getShellTitle() {
        return "Property";
    }

    protected String getTitle() {
        return "Property properties";
    }

    /**
     * One of the buttons has been pressed, act accordingly.
     */
    private void handleButtonPressed(Button button) {

        IProject project = WebFlowUtils.getActiveFlowEditorInput().getFile()
                .getProject();
        IJavaSearchScope searchScope = SearchEngine.createWorkspaceScope();

        /*
         * TypeSelectionDialog2 dialog= new TypeSelectionDialog2(getShell(),
         * false, new ProgressMonitorDialog(getShell()), searchScope,
         * IJavaSearchConstants.TYPE);
         * dialog.setMessage(JavaUIMessages.JavaUI_defaultDialogMessage);
         */

        TypeSelectionDialog dialog = new TypeSelectionDialog(getShell(),
                new ProgressMonitorDialog(getShell()),
                IJavaSearchConstants.CLASS, searchScope);
        dialog.setMessage("Select an type"); //$NON-NLS-1$
        dialog.setBlockOnOpen(true);
        dialog.setTitle("Type Selection");
        //dialog.setFilter("*");
        if (Dialog.OK == dialog.open()) {
            IType obj = (IType) dialog.getFirstResult();
            this.typeText.setText(obj.getFullyQualifiedName());
        }

        this.validateInput();

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
            errorMessage.append("A valid name is required. ");
            error = true;
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