/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ide.eclipse.core.ui.fields;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.springframework.ide.eclipse.core.ui.dialogs.IStringButtonAdapter;


/**
 * Dialog field containing a label, text control and a button control.
 * @author Pierre-Antoine Grégoire
 */
public class StringButtonDialogField extends StringDialogField {

    private Button fBrowseButton;

    private String fBrowseButtonLabel;

    private IStringButtonAdapter fStringButtonAdapter;

    private boolean fButtonEnabled;

    public StringButtonDialogField(FormToolkit formToolkit,IStringButtonAdapter adapter) {
        super(formToolkit);
        fStringButtonAdapter = adapter;
        fBrowseButtonLabel = "!Browse...!";
        fButtonEnabled = true;
    }

    /**
     * Sets the label of the button.
     */
    public void setButtonLabel(String label) {
        fBrowseButtonLabel = label;
    }

    // ------ adapter communication

    /**
     * Programmatical pressing of the button
     */
    public void changeControlPressed() {
        fStringButtonAdapter.changeControlPressed(this);
    }

    // ------- layout helpers

    /*
     * @see DialogField#doFillIntoGrid
     */
    public Control[] doFillIntoTable(Composite parent, int nColumns) {
        
        Label label = getLabelControl(parent);
        label.setLayoutData(tableWrapDataForLabel(1));
        Text text = getTextControl(parent);
        text.setLayoutData(tableWrapDataForText(nColumns - 2));
        Button button = getChangeControl(parent);
        button.setLayoutData(tableWrapDataForButton(button, 1));
        formToolkit.paintBordersFor(parent);
        return new Control[] { label, text, button };
    }

    /*
     * @see DialogField#getNumberOfControls
     */
    public int getNumberOfControls() {
        return 3;
    }

    protected static TableWrapData tableWrapDataForButton(Button button, int span) {
        TableWrapData td = new TableWrapData();
        td.colspan=span;
        return td;
    }

    // ------- ui creation

    /**
     * Creates or returns the created buttom widget.
     * 
     * @param parent The parent composite or <code>null</code> if the widget has already been created.
     */
    public Button getChangeControl(Composite parent) {
        if (fBrowseButton == null) {
            assertCompositeNotNull(parent);
            fBrowseButton = formToolkit.createButton(parent,"", SWT.WRAP);
            fBrowseButton.setText(fBrowseButtonLabel);
            fBrowseButton.setEnabled(isEnabled() && fButtonEnabled);
            fBrowseButton.addSelectionListener(new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent e) {
                    changeControlPressed();
                }

                public void widgetSelected(SelectionEvent e) {
                    changeControlPressed();
                }
            });

        }
        return fBrowseButton;
    }

    // ------ enable / disable management

    /**
     * Sets the enable state of the button.
     */
    public void enableButton(boolean enable) {
        if (isOkToUse(fBrowseButton)) {
            fBrowseButton.setEnabled(isEnabled() && enable);
        }
        fButtonEnabled = enable;
    }

    /*
     * @see DialogField#updateEnableState
     */
    protected void updateEnableState() {
        super.updateEnableState();
        if (isOkToUse(fBrowseButton)) {
            fBrowseButton.setEnabled(isEnabled() && fButtonEnabled);
        }
    }
    
    public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}

}