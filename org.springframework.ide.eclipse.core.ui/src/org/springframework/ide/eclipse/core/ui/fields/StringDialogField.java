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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;

/**
 * Dialog field containing a label and a text control.
 * @author Pierre-Antoine Grégoire
 */
public class StringDialogField extends DialogField {

    private String fText;

    private Text fTextControl;

    private ModifyListener fModifyListener;

    public StringDialogField(FormToolkit formToolkit) {
        super(formToolkit);
        fText = ""; //$NON-NLS-1$
    }

    // ------- layout helpers

    /*
     * @see DialogField#doFillIntoGrid
     */
    public Control[] doFillIntoTable(Composite parent, int nColumns) {
        
        Label label = getLabelControl(parent);
        label.setLayoutData(tableWrapDataForLabel(1));
        Text text = getTextControl(parent);
        text.setLayoutData(tableWrapDataForText(nColumns - 1));
        formToolkit.paintBordersFor(parent);
        return new Control[] { label, text };
    }

    /*
     * @see DialogField#getNumberOfControls
     */
    public int getNumberOfControls() {
        return 2;
    }

    protected static TableWrapData tableWrapDataForText(int span) {
        TableWrapData td= new TableWrapData(TableWrapData.FILL_GRAB);
        td.grabHorizontal=true;
        td.indent=5;
        td.colspan = span;
        return td;
    }

    // ------- focus methods

    /*
     * @see DialogField#setFocus
     */
    public boolean setFocus() {
        if (isOkToUse(fTextControl)) {
            fTextControl.setFocus();
            fTextControl.setSelection(0, fTextControl.getText().length());
        }
        return true;
    }

    // ------- ui creation

    /**
     * Creates or returns the created text control.
     * 
     * @param parent The parent composite or <code>null</code> when the widget has already been created.
     */
    public Text getTextControl(Composite parent) {
        
    	if (fTextControl == null) {
            assertCompositeNotNull(parent);
            fModifyListener = new ModifyListener() {
                public void modifyText(ModifyEvent e) {
                    doModifyText(e);
                }
            };
            fTextControl = formToolkit.createText(parent,"", SWT.SINGLE);
            fTextControl.setText(fText);
            fTextControl.setFont(parent.getFont());
            fTextControl.addModifyListener(fModifyListener);
            fTextControl.setData(FormToolkit.KEY_DRAW_BORDER,FormToolkit.TEXT_BORDER);
            fTextControl.setEnabled(isEnabled());
        }
        return fTextControl;
    }

    private void doModifyText(ModifyEvent e) {
        if (isOkToUse(fTextControl)) {
            fText = fTextControl.getText();
        }
        dialogFieldChanged();
    }

    // ------ enable / disable management

    /*
     * @see DialogField#updateEnableState
     */
    protected void updateEnableState() {
        super.updateEnableState();
        if (isOkToUse(fTextControl)) {
        	fTextControl.setEnabled(isEnabled());
        }
    }

    // ------ text access

    /**
     * Gets the text. Can not be <code>null</code>
     */
    public String getText() {
        return fText;
    }

    /**
     * Sets the text. Triggers a dialog-changed event.
     */
    public void setText(String text) {
        fText = text;
        if (isOkToUse(fTextControl)) {
            fTextControl.setText(text);
        } else {
            dialogFieldChanged();
        }
    }

    /**
     * Sets the text without triggering a dialog-changed event.
     */
    public void setTextWithoutUpdate(String text) {
        fText = text;
        if (isOkToUse(fTextControl)) {
            fTextControl.removeModifyListener(fModifyListener);
            fTextControl.setText(text);
            fTextControl.addModifyListener(fModifyListener);
        }
    }

    public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}

	/**
     * @see DialogField#refresh()
     */
    public void refresh() {
        super.refresh();
        if (isOkToUse(fTextControl)) {
            setTextWithoutUpdate(fText);
        }
    }

}