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

import org.eclipse.core.resources.IStorage;

/**
 * Validates the selection if only instances of <code>IStorage</code> are
 * selected.
 * @author Torsten Juergeleit
 */
public class StorageSelectionValidator extends AbstractSelectionValidator {

	/**
	 * Creates a new instance of the validator.
	 * 
	 * @param multiSelect <code>true</code> if multi selection is allowed.
	 * 	<code>false</code> if only single selection is allowed. 
	 */
	public StorageSelectionValidator(boolean multiSelect) {
		super(multiSelect);
	}

	@Override
	public boolean isValid(Object selection) {
		return (selection instanceof IStorage);
	}
}
