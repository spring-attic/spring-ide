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
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;


/**
 * Dialog Field containing a single button such as a radio or checkbox button.
 * @author Pierre-Antoine Grégoire
 */
public class SelectionButtonDialogField extends DialogField {

    private Button fButton;

    private boolean fIsSelected;

    private boolean eventDispatchingEnabled;

    private IDialogField[] fAttachedDialogFields;

    private int fButtonStyle;

    public boolean isEventDispatchingEnabled() {
        return eventDispatchingEnabled;
    }

    public void setEventDispatchingEnabled(boolean eventDispatchingEnabled) {
        this.eventDispatchingEnabled = eventDispatchingEnabled;
    }

    /**
     * Creates a selection button. Allowed button styles: SWT.RADIO, SWT.CHECK, SWT.TOGGLE, SWT.PUSH
     */
    public SelectionButtonDialogField(FormToolkit formToolkit, int buttonStyle) {
        super(formToolkit);
        fIsSelected = false;
        fAttachedDialogFields = null;
        fButtonStyle = buttonStyle;
    }

    /**
     * Attaches a field to the selection state of the selection button. The attached field will be disabled if the selection button is not selected.
     */
    public void attachDialogField(IDialogField dialogField) {
        attachDialogFields(new IDialogField[] { dialogField });
    }

    /**
     * Attaches fields to the selection state of the selection button. The attached fields will be disabled if the selection button is not selected.
     */
    public void attachDialogFields(IDialogField[] dialogFields) {
        fAttachedDialogFields = dialogFields;
        for (int i = 0; i < dialogFields.length; i++) {
            dialogFields[i].setEnabled(fIsSelected);
        }
    }

    /**
     * Returns <code>true</code> is teh gived field is attached to the selection button.
     */
    public boolean isAttached(IDialogField editor) {
        if (fAttachedDialogFields != null) {
            for (int i = 0; i < fAttachedDialogFields.length; i++) {
                if (fAttachedDialogFields[i] == editor) {
                    return true;
                }
            }
        }
        return false;
    }

    // ------- layout helpers

    /*
     * @see DialogField#doFillIntoGrid
     */
    public Control[] doFillIntoTable(Composite parent, int nColumns) {

        Button button = getSelectionButton(parent);
        TableWrapData td = new TableWrapData();
        td.colspan = nColumns;
        button.setLayoutData(td);

        return new Control[] { button };
    }

    /*
     * @see DialogField#getNumberOfControls
     */
    public int getNumberOfControls() {
        return 1;
    }

    // ------- ui creation

    /**
     * Returns the selection button widget. When called the first time, the widget will be created.
     * 
     * @param group The parent composite when called the first time, or <code>null</code> after.
     */
    public Button getSelectionButton(Composite group) {
        if (fButton == null) {
            assertCompositeNotNull(group);

            fButton =formToolkit.createButton(group,"", fButtonStyle);
            fButton.setFont(group.getFont());
            fButton.setText(fLabelText);
            fButton.setEnabled(isEnabled());
            fButton.setSelection(fIsSelected);
            fButton.addSelectionListener(new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent e) {
                    doWidgetSelected(e);
                }

                public void widgetSelected(SelectionEvent e) {
                    doWidgetSelected(e);
                }
            });
        }
        return fButton;
    }

    private void doWidgetSelected(SelectionEvent e) {
        if (isOkToUse(fButton)) {
            changeValue(fButton.getSelection());
        }
    }

    private void changeValue(boolean newState) {
        if (fIsSelected != newState) {
            fIsSelected = newState;
            if (fAttachedDialogFields != null) {
                boolean focusSet = false;
                for (int i = 0; i < fAttachedDialogFields.length; i++) {
                    fAttachedDialogFields[i].setEnabled(fIsSelected);
                    if (fIsSelected && !focusSet) {
                        focusSet = fAttachedDialogFields[i].setFocus();
                    }
                }
            }
            if (eventDispatchingEnabled) {
                dialogFieldChanged();
            }
        } else if (fButtonStyle == SWT.PUSH) {
            if (eventDispatchingEnabled) {
                dialogFieldChanged();
            }
        }
    }

    // ------ model access

    /**
     * Returns the selection state of the button.
     */
    public boolean isSelected() {
        return fIsSelected;
    }

    /**
     * Sets the selection state of the button.
     */
    public void setSelection(boolean selected) {
        changeValue(selected);
        if (isOkToUse(fButton)) {
            fButton.setSelection(selected);
        }
    }

    // ------ enable / disable management

    /*
     * @see DialogField#updateEnableState
     */
    protected void updateEnableState() {
        super.updateEnableState();
        if (isOkToUse(fButton)) {
            fButton.setEnabled(isEnabled());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField#refresh()
     */
    public void refresh() {
        super.refresh();
        if (isOkToUse(fButton)) {
            fButton.setSelection(fIsSelected);
        }
    }

}
