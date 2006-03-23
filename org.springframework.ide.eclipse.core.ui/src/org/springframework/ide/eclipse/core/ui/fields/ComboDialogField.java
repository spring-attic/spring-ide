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
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;

/**
 * Dialog field containing a label and a combo control.<br>
 * this version provides a FormToolkit-compatible layout.
 * @author Pierre-Antoine Grégoire
 */
public class ComboDialogField extends DialogField {

	private String fText;

	private int fSelectionIndex;

	private String[] fItems;

	private CCombo fComboControl;

	private ModifyListener fModifyListener;

	private int fFlags;

	public ComboDialogField(FormToolkit formToolkit, int flags) {
		super(formToolkit);
		fText = ""; //$NON-NLS-1$
		fItems = new String[0];
		fFlags = flags;
		fSelectionIndex = -1;
	}

	// ------- layout helpers

	/*
	 * @see DialogField#doFillIntoGrid
	 */
	public Control[] doFillIntoTable(Composite parent, int nColumns) {

		Label label = getLabelControl(parent);
		label.setLayoutData(tableWrapDataForLabel(1));
		CCombo combo = getComboControl(parent);
		combo.setLayoutData(tableWrapDataForCombo(nColumns - 1));
		formToolkit.paintBordersFor(parent);
		return new Control[] { label, combo };
	}

	/*
	 * @see DialogField#getNumberOfControls
	 */
	public int getNumberOfControls() {
		return 2;
	}

	protected static TableWrapData tableWrapDataForCombo(int span) {
		TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
		td.align = TableWrapData.FILL;
		td.colspan = span;
		return td;
	}

	// ------- focus methods

	/*
	 * @see DialogField#setFocus
	 */
	public boolean setFocus() {
		if (isOkToUse(fComboControl)) {
			fComboControl.setFocus();
		}
		return true;
	}

	// ------- ui creation

	/**
	 * Creates or returns the created combo control.
	 * 
	 * @param parent
	 *            The parent composite or <code>null</code> when the widget has already been created.
	 */
	public CCombo getComboControl(Composite parent) {
		if (fComboControl == null) {
			assertCompositeNotNull(parent);
			fModifyListener = new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					doModifyText(e);
				}
			};
			SelectionListener selectionListener = new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					doSelectionChanged(e);
				}

				public void widgetDefaultSelected(SelectionEvent e) {
				}
			};
			fComboControl = new CCombo(parent, fFlags | SWT.FLAT);
			// moved up due to 1GEUNW2
			fComboControl.setItems(fItems);
			if (fSelectionIndex != -1) {
				fComboControl.select(fSelectionIndex);
			} else {
				fComboControl.setText(fText);
			}
			fComboControl.setFont(parent.getFont());
			fComboControl.addModifyListener(fModifyListener);
			fComboControl.addSelectionListener(selectionListener);
			fComboControl.setEnabled(isEnabled());
		}
		fComboControl.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		formToolkit.adapt(fComboControl, true, true);
		return fComboControl;
	}

	private void doModifyText(ModifyEvent e) {
		if (isOkToUse(fComboControl)) {
			fText = fComboControl.getText();
			fSelectionIndex = fComboControl.getSelectionIndex();
		}
		dialogFieldChanged();
	}

	private void doSelectionChanged(SelectionEvent e) {
		if (isOkToUse(fComboControl)) {
			fItems = fComboControl.getItems();
			fText = fComboControl.getText();
			fSelectionIndex = fComboControl.getSelectionIndex();
		}
		dialogFieldChanged();
	}

	// ------ enable / disable management

	/*
	 * @see DialogField#updateEnableState
	 */
	protected void updateEnableState() {
		super.updateEnableState();
		if (isOkToUse(fComboControl)) {
			fComboControl.setEnabled(isEnabled());
		}
	}

	// ------ text access

	/**
	 * Gets the combo items.
	 */
	public String[] getItems() {
		return fItems;
	}

	/**
	 * Sets the combo items. Triggers a dialog-changed event.
	 */
	public void setItems(String[] items) {
		fItems = items;
		if (isOkToUse(fComboControl)) {
			fComboControl.setItems(items);
		}
		dialogFieldChanged();
	}

	/**
	 * Gets the text.
	 */
	public String getText() {
		return fText;
	}

	/**
	 * Sets the text. Triggers a dialog-changed event.
	 */
	public void setText(String text) {
		fText = text;
		if (isOkToUse(fComboControl)) {
			fComboControl.setText(text);
		} else {
			dialogFieldChanged();
		}
	}

	/**
	 * Selects an item.
	 */
	public boolean selectItem(int index) {
		boolean success = false;
		if (isOkToUse(fComboControl)) {
			fComboControl.select(index);
			fText = fItems[index];
			fSelectionIndex = index;
			success = fComboControl.getSelectionIndex() == index;
		} else {
			if (index >= 0 && index < fItems.length) {
				fText = fItems[index];
				fSelectionIndex = index;
				success = true;
			}
		}
		if (success) {
			dialogFieldChanged();
		}
		return success;
	}

	/**
	 * Selects an item.
	 */
	public boolean selectItem(String name) {
		for (int i = 0; i < fItems.length; i++) {
			if (fItems[i].equals(name)) {
				return selectItem(i);
			}
		}
		return false;
	}

	public int getSelectionIndex() {
		return fSelectionIndex;
	}

	/**
	 * Sets the text without triggering a dialog-changed event.
	 */
	public void setTextWithoutUpdate(String text) {
		fText = text;
		if (isOkToUse(fComboControl)) {
			fComboControl.removeModifyListener(fModifyListener);
			fComboControl.setText(text);
			fComboControl.addModifyListener(fModifyListener);
		}
	}

	/**
	 * @see DialogField#refresh()
	 */
	public void refresh() {
		super.refresh();
		setTextWithoutUpdate(fText);
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}

}