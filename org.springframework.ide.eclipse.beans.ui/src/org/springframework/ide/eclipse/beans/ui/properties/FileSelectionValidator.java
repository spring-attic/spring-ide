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

package org.springframework.ide.eclipse.beans.ui.properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;

/**
 * Validates the selection if only files are selected.
 */
public class FileSelectionValidator implements ISelectionStatusValidator {

	private boolean multiSelect;

	/**
	 * Creates a new instance of the validator.
	 * 
	 * @param multiSelect <code>true</code> if multi selection is allowed.
	 * 	<code>false</code> if only single selection is allowed. 
	 */
	public FileSelectionValidator(boolean multiSelect) {
		this.multiSelect = multiSelect;
	}

	public IStatus validate(Object[] selection) {
		int selected = selection.length;
		if (selected == 0 || (selected > 1 && multiSelect == false)) {
			return createStatus(IStatus.ERROR);
		}
		for (int i = 0; i < selected; i++) {
			if (!(selection[i] instanceof IFile)) {
				return createStatus(IStatus.ERROR);
			}
		}
		return createStatus(IStatus.OK);
	}

	private static IStatus createStatus(int code) {
		return new Status(code, BeansUIPlugin.PLUGIN_ID, IStatus.OK, "", null);
	}
}
