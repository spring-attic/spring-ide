/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.ui.dialogs;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.springframework.ide.eclipse.ui.SpringUIPlugin;

/**
 * Base class for validation of selections in a dialog.
 * @author Torsten Juergeleit
 */
public abstract class AbstractSelectionValidator implements
		ISelectionStatusValidator {

	private boolean multiSelect;

	/**
	 * Creates a new instance of the validator.
	 * 
	 * @param multiSelect <code>true</code> if multi selection is allowed.
	 * 	<code>false</code> if only single selection is allowed. 
	 */
	public AbstractSelectionValidator(boolean multiSelect) {
		this.multiSelect = multiSelect;
	}

	public IStatus validate(Object[] selection) {
		int selected = selection.length;
		if (selected == 0 || (selected > 1 && multiSelect == false)) {
			return createStatus(IStatus.ERROR);
		}
		for (int i = 0; i < selected; i++) {
			if (!isValid(selection[i])) {
				return createStatus(IStatus.ERROR);
			}
		}
		return createStatus(IStatus.OK);
	}

	public abstract boolean isValid(Object selection);

	protected static IStatus createStatus(int code) {
		return new Status(code, SpringUIPlugin.PLUGIN_ID, IStatus.OK, "",
				null);
	}
}
