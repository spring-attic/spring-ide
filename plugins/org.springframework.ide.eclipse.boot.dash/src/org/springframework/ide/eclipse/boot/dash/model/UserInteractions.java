/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.List;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.IInputValidator;
import org.springframework.ide.eclipse.boot.dash.dialogs.ToggleFiltersDialogModel;

/**
 * An instance of this interface handles interactions with the GUI code from
 * the model code. It's main purpose is to provide a convenient handle for
 * 'mocking' GUI interactions in test code, using, for example, mockito.
 *
 * @author Kris De Volder
 */
public interface UserInteractions {
	ILaunchConfiguration chooseConfigurationDialog(String dialogTitle, String message, List<ILaunchConfiguration> configs);
	IType chooseMainType(IType[] mainTypes, String dialogTitle, String message);
	void errorPopup(String title, String message);
	void openLaunchConfigurationDialogOnGroup(ILaunchConfiguration selection, String launchGroup);
	void openUrl(String url);
	boolean confirmOperation(String title, String message);
	String updatePassword(String userName, String targetId);
	void openDialog(ToggleFiltersDialogModel model);

	/**
	 * select a file
	 * @param title The title of the open file dialog
	 * @param file The default path/file that should be used when opening the dialog
	 * @return The full path of the selected file
	 */
	String chooseFile(String title, String file);

	/**
	 * @param title
	 * @param message
	 * @param initialValue
	 * @param validator
	 * @return
	 */
	String inputText(String title, String message, String initialValue, IInputValidator validator);
}
