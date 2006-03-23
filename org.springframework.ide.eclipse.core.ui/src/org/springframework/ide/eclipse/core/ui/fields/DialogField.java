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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;

/**
 * Base class of all dialog fields.<BR>
 * Dialog fields manage controls together with the model, independed from the creation time of the widgets. <BR>
 * <ul>
 * <li>support for automated layouting.
 * <li> enable / disable, set focus a concept of the base class.
 * </ul>
 * DialogField have a label.
 * This version's base on the form toolkit.
 * @author Pierre-Antoine Grégoire
 */
public class DialogField implements IDialogField {

	private Label fLabel;

	protected String fLabelText;

	private IDialogFieldListener fDialogFieldListener;

	private boolean fEnabled;

	protected FormToolkit formToolkit;

	public DialogField(FormToolkit formToolkit) {
		this.formToolkit = formToolkit;
		fEnabled = true;
		fLabel = null;
		fLabelText = ""; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ec.ep.dit.isp.foundry.eclipse.platform.utils.dialogs.formfields.IDialogField#setLabelText(java.lang.String)
	 */
	public void setLabelText(String labeltext) {
		fLabelText = labeltext;
		if (isOkToUse(fLabel)) {
			fLabel.setText(labeltext);
		}
	}

	// ------ change listener

	/*
	 * (non-Javadoc)
	 * 
	 * @see ec.ep.dit.isp.foundry.eclipse.platform.utils.dialogs.formfields.IDialogField#setDialogFieldListener(ec.ep.dit.isp.foundry.eclipse.platform.utils.dialogs.IDialogFieldListener)
	 */
	public final void setDialogFieldListener(IDialogFieldListener listener) {
		fDialogFieldListener = listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ec.ep.dit.isp.foundry.eclipse.platform.utils.dialogs.formfields.IDialogField#dialogFieldChanged()
	 */
	public void dialogFieldChanged() {
		if (fDialogFieldListener != null) {
			fDialogFieldListener.dialogFieldChanged(this);
		}
	}

	// ------- focus management

	/*
	 * (non-Javadoc)
	 * 
	 * @see ec.ep.dit.isp.foundry.eclipse.platform.utils.dialogs.formfields.IDialogField#setFocus()
	 */
	public boolean setFocus() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ec.ep.dit.isp.foundry.eclipse.platform.utils.dialogs.formfields.IDialogField#postSetFocusOnDialogField(org.eclipse.swt.widgets.Display)
	 */
	public void postSetFocusOnDialogField(Display display) {
		if (display != null) {
			display.asyncExec(new Runnable() {
				public void run() {
					setFocus();
				}
			});
		}
	}

	// ------- layout helpers

	/*
	 * (non-Javadoc)
	 * 
	 * @see ec.ep.dit.isp.foundry.eclipse.platform.utils.dialogs.formfields.IDialogField#doFillIntoGrid(org.eclipse.swt.widgets.Composite, int)
	 */
	public Control[] doFillIntoTable(Composite parent, int nColumns) {

		Label label = getLabelControl(parent);
		label.setLayoutData(tableWrapDataForLabel(nColumns));

		return new Control[] { label };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ec.ep.dit.isp.foundry.eclipse.platform.utils.dialogs.formfields.IDialogField#getNumberOfControls()
	 */
	public int getNumberOfControls() {
		return 1;
	}

	protected static TableWrapData tableWrapDataForLabel(int span) {
		TableWrapData td = new TableWrapData();
		td.align = TableWrapData.RIGHT;
		td.colspan = span;
		return td;
	}

	// ------- ui creation

	/*
	 * (non-Javadoc)
	 * 
	 * @see ec.ep.dit.isp.foundry.eclipse.platform.utils.dialogs.formfields.IDialogField#getLabelControl(org.eclipse.swt.widgets.Composite)
	 */
	public Label getLabelControl(Composite parent) {
		if (fLabel == null) {
			assertCompositeNotNull(parent);

			fLabel = formToolkit.createLabel(parent, "", SWT.WRAP);
			fLabel.setFont(parent.getFont());
			fLabel.setEnabled(fEnabled);
			if (fLabelText != null && !"".equals(fLabelText)) { //$NON-NLS-1$
				fLabel.setText(fLabelText);
			} else {
				// XXX: to avoid a 16 pixel wide empty label - revisit
				fLabel.setText("."); //$NON-NLS-1$
				fLabel.setVisible(false);
			}
		}
		return fLabel;
	}

	/**
	 * Tests is the control is not <code>null</code> and not disposed.
	 */
	protected final boolean isOkToUse(Control control) {
		return (control != null) && (Display.getCurrent() != null) && !control.isDisposed();
	}

	// --------- enable / disable management

	/*
	 * (non-Javadoc)
	 * 
	 * @see ec.ep.dit.isp.foundry.eclipse.platform.utils.dialogs.formfields.IDialogField#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		if (enabled != fEnabled) {
			fEnabled = enabled;
			updateEnableState();
		}
	}

	/**
	 * Called when the enable state changed. To be extended by dialog field implementors.
	 */
	protected void updateEnableState() {
		if (fLabel != null) {
			fLabel.setEnabled(fEnabled);
		}
	}

	/**
	 * Creates a spacer control.
	 * 
	 * @param parent
	 *            The parent composite
	 */
	public static Control createEmptySpace(FormToolkit formToolkit, Composite parent) {
		return createEmptySpace(formToolkit, parent, 1);
	}

	/**
	 * Creates a spacer control with the given span. The composite is assumed to have <code>MGridLayout</code> as layout.
	 * 
	 * @param parent
	 *            The parent composite
	 */
	public static Control createEmptySpace(FormToolkit formToolkit, Composite parent, int span) {
		Label label = formToolkit.createLabel(parent, "", SWT.LEFT);
		TableWrapData td = new TableWrapData();
		td.colspan = span;
		td.heightHint = 0;
		label.setLayoutData(td);
		return label;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ec.ep.dit.isp.foundry.eclipse.platform.utils.dialogs.formfields.IDialogField#refresh()
	 */
	public void refresh() {
		updateEnableState();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ec.ep.dit.isp.foundry.eclipse.platform.utils.dialogs.formfields.IDialogField#isEnabled()
	 */
	public final boolean isEnabled() {
		return fEnabled;
	}

	protected final void assertCompositeNotNull(Composite comp) {
		Assert.isNotNull(comp, "uncreated control requested with composite null"); //$NON-NLS-1$
	}

}
