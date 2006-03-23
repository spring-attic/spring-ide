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

import org.eclipse.jface.util.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * Dialog field describing a group with buttons (Checkboxes, radio buttons..)
 * @author Pierre-Antoine Grégoire
 */
public class SelectionButtonDialogFieldGroup extends DialogField {

    private Composite fButtonComposite;

    private Button[] fButtons;

    private String[] fButtonNames;

    private boolean[] fButtonsSelected;

    private boolean[] fButtonsEnabled;

    private int fGroupBorderStyle;

    private int fGroupNumberOfColumns;

    private int fButtonsStyle;

    /**
     * Creates a group without border.
     */
    public SelectionButtonDialogFieldGroup(FormToolkit formToolkit,int buttonsStyle, String[] buttonNames, int nColumns) {
        this(formToolkit,buttonsStyle, buttonNames, nColumns, SWT.NONE);
    }

    /**
     * Creates a group with border (label in border). Accepted button styles are: SWT.RADIO, SWT.CHECK, SWT.TOGGLE For border styles see <code>Group</code>
     */
    public SelectionButtonDialogFieldGroup(FormToolkit formToolkit,int buttonsStyle, String[] buttonNames, int nColumns, int borderStyle) {
        super(formToolkit);

        Assert.isTrue(buttonsStyle == SWT.RADIO || buttonsStyle == SWT.CHECK || buttonsStyle == SWT.TOGGLE);
        fButtonNames = buttonNames;

        int nButtons = buttonNames.length;
        fButtonsSelected = new boolean[nButtons];
        fButtonsEnabled = new boolean[nButtons];
        for (int i = 0; i < nButtons; i++) {
            fButtonsSelected[i] = false;
            fButtonsEnabled[i] = true;
        }
        if (fButtonsStyle == SWT.RADIO) {
            fButtonsSelected[0] = true;
        }

        fGroupBorderStyle = borderStyle;
        fGroupNumberOfColumns = (nColumns <= 0) ? nButtons : nColumns;

        fButtonsStyle = buttonsStyle;

    }

    // ------- layout helpers

    /*
     * @see DialogField#doFillIntoGrid
     */
    public Control[] doFillIntoTable(Composite parent, int nColumns) {

        if (fGroupBorderStyle == SWT.NONE) {
            Label label = getLabelControl(parent);
            label.setLayoutData(tableWrapDataForLabel(1));

            Composite buttonsgroup = getSelectionButtonsGroup(parent);
            TableWrapData td = new TableWrapData();
            td.colspan= nColumns - 1;
            buttonsgroup.setLayoutData(td);

            return new Control[] { label, buttonsgroup };
        } else {
            Composite buttonsgroup = getSelectionButtonsGroup(parent);
            TableWrapData td = new TableWrapData();
            td.colspan= nColumns ;
            buttonsgroup.setLayoutData(td);

            return new Control[] { buttonsgroup };
        }
    }

    /*
     * @see DialogField#doFillIntoGrid
     */
    public int getNumberOfControls() {
        return (fGroupBorderStyle == SWT.NONE) ? 2 : 1;
    }

    // ------- ui creation

    private Button createSelectionButton(int index, Composite group, SelectionListener listener) {
        Button button = formToolkit.createButton(group,"", fButtonsStyle | SWT.LEFT);
        button.setFont(group.getFont());
        button.setText(fButtonNames[index]);
        button.setEnabled(isEnabled() && fButtonsEnabled[index]);
        button.setSelection(fButtonsSelected[index]);
        button.addSelectionListener(listener);
        button.setLayoutData(new GridData());
        return button;
    }

    /**
     * Returns the group widget. When called the first time, the widget will be created.
     * 
     * @param parent The parent composite when called the first time, or <code>null</code> after.
     */
    public Composite getSelectionButtonsGroup(Composite parent) {
        if (fButtonComposite == null) {
            assertCompositeNotNull(parent);

            TableWrapLayout layout = new TableWrapLayout();
            layout.makeColumnsEqualWidth = true;
            layout.numColumns = fGroupNumberOfColumns;

            if (fGroupBorderStyle != SWT.NONE) {
                Group group =new Group(parent, fGroupBorderStyle);
                if (fLabelText != null && fLabelText.length() > 0) {
                    group.setText(fLabelText);
                }
                fButtonComposite = group;
            } else {
                fButtonComposite = new Composite(parent, SWT.NULL);
                
            }
            fButtonComposite.setLayout(layout);

            SelectionListener listener = new SelectionListener() {
                public void widgetDefaultSelected(SelectionEvent e) {
                    doWidgetSelected(e);
                }

                public void widgetSelected(SelectionEvent e) {
                    doWidgetSelected(e);
                }
            };
            int nButtons = fButtonNames.length;
            fButtons = new Button[nButtons];
            for (int i = 0; i < nButtons; i++) {
                fButtons[i] = createSelectionButton(i, fButtonComposite, listener);
            }
            int nRows = nButtons / fGroupNumberOfColumns;
            int nFillElements = nRows * fGroupNumberOfColumns - nButtons;
            for (int i = 0; i < nFillElements; i++) {
                createEmptySpace(formToolkit,fButtonComposite);
            }
        }
        formToolkit.adapt(fButtonComposite);
        return fButtonComposite;
    }

    /**
     * Returns a button from the group or <code>null</code> if not yet created.
     */
    public Button getSelectionButton(int index) {
        if (index >= 0 && index < fButtons.length) {
            return fButtons[index];
        }
        return null;
    }

    private void doWidgetSelected(SelectionEvent e) {
        Button button = (Button) e.widget;
        for (int i = 0; i < fButtons.length; i++) {
            if (fButtons[i] == button) {
                fButtonsSelected[i] = button.getSelection();
                dialogFieldChanged();
                return;
            }
        }
    }

    // ------ model access

    /**
     * Returns the selection state of a button contained in the group.
     * 
     * @param index The index of the button
     */
    public boolean isSelected(int index) {
        if (index >= 0 && index < fButtonsSelected.length) {
            return fButtonsSelected[index];
        }
        return false;
    }

    /**
     * Sets the selection state of a button contained in the group.
     */
    public void setSelection(int index, boolean selected) {
        if (index >= 0 && index < fButtonsSelected.length) {
            if (fButtonsSelected[index] != selected) {
                fButtonsSelected[index] = selected;
                if (fButtons != null) {
                    Button button = fButtons[index];
                    if (isOkToUse(button)) {
                        button.setSelection(selected);
                    }
                }
            }
        }
    }

    // ------ enable / disable management

    protected void updateEnableState() {
        super.updateEnableState();
        if (fButtons != null) {
            boolean enabled = isEnabled();
            for (int i = 0; i < fButtons.length; i++) {
                Button button = fButtons[i];
                if (isOkToUse(button)) {
                    button.setEnabled(enabled && fButtonsEnabled[i]);
                }
            }
        }
    }

    /**
     * Sets the enable state of a button contained in the group.
     */
    public void enableSelectionButton(int index, boolean enable) {
        if (index >= 0 && index < fButtonsEnabled.length) {
            fButtonsEnabled[index] = enable;
            if (fButtons != null) {
                Button button = fButtons[index];
                if (isOkToUse(button)) {
                    button.setEnabled(isEnabled() && enable);
                }
            }
        }
    }

    /**
     * @see DialogField#refresh()
     */
    public void refresh() {
        super.refresh();
        for (int i = 0; i < fButtons.length; i++) {
            Button button = fButtons[i];
            if (isOkToUse(button)) {
                button.setSelection(fButtonsSelected[i]);
            }
        }
    }

}