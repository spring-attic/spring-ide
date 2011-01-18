/*******************************************************************************
 * Copyright (c) 2004, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.ui.dialogs;

import org.eclipse.core.resources.IFile;

/**
 * Validates the selection if only instances of <code>IFile</code> are
 * selected.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class FileSelectionValidator extends AbstractSelectionValidator {

	/**
	 * Creates a new instance of the validator.
	 * 
	 * @param multiSelect <code>true</code> if multi selection is allowed.
	 * 	<code>false</code> if only single selection is allowed. 
	 */
	public FileSelectionValidator(boolean multiSelect) {
		super(multiSelect);
	}

	@Override
	public boolean isValid(Object selection) {
		return (selection instanceof IFile);
	}
}
