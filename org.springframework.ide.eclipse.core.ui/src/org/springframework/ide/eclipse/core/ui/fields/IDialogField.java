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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;


/**
 * Définition of a Dialog field.
 * @author Pierre-Antoine Grégoire
 */
public interface IDialogField {

	/**
	 * Sets the label of the dialog field.
	 */
	public abstract void setLabelText(String labeltext);

	/**
	 * Defines the listener for this dialog field.
	 */
	public abstract void setDialogFieldListener(IDialogFieldListener listener);

	/**
	 * Programatical invocation of a dialog field change.
	 */
	public abstract void dialogFieldChanged();

	/**
	 * Tries to set the focus to the dialog field. Returns <code>true</code> if the dialog field can take focus. To be reimplemented by dialog field implementors.
	 */
	public abstract boolean setFocus();

	/**
	 * Posts <code>setFocus</code> to the display event queue.
	 */
	public abstract void postSetFocusOnDialogField(Display display);

	/**
	 * Returns the number of columns of the dialog field. To be reimplemented by dialog field implementors.
	 */
	public abstract int getNumberOfControls();

	/**
	 * Creates or returns the created label widget.
	 * 
	 * @param parent The parent composite or <code>null</code> if the widget has already been created.
	 */
	public abstract Label getLabelControl(Composite parent);

	/**
	 * Sets the enable state of the dialog field.
	 */
	public abstract void setEnabled(boolean enabled);

	/**
	 * Brings the UI in sync with the model. Only needed when model was changed in different thread whil UI was lready created.
	 */
	public abstract void refresh();

	/**
	 * Gets the enable state of the dialog field.
	 */
	public abstract boolean isEnabled();

}