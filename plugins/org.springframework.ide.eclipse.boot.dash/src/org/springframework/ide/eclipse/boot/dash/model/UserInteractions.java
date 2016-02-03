/*******************************************************************************
 * Copyright (c) 2015-2016 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import java.util.Collection;
import java.util.Map;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.IInputValidator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.deployment.CloudApplicationDeploymentProperties;
import org.springframework.ide.eclipse.boot.dash.dialogs.ToggleFiltersDialogModel;

/**
 * An instance of this interface handles interactions with the GUI code from
 * the model code. It's main purpose is to provide a convenient handle for
 * 'mocking' GUI interactions in test code, using, for example, mockito.
 *
 * @author Kris De Volder
 */
public interface UserInteractions {
	ILaunchConfiguration chooseConfigurationDialog(String dialogTitle, String message, Collection<ILaunchConfiguration> configs);
	IType chooseMainType(IType[] mainTypes, String dialogTitle, String message);
	void errorPopup(String title, String message);
	void openLaunchConfigurationDialogOnGroup(ILaunchConfiguration selection, String launchGroup);
	void openUrl(String url);
	boolean confirmOperation(String title, String message);
	String updatePassword(String userName, String targetId);
	void openDialog(ToggleFiltersDialogModel model);
	String selectRemoteEureka(BootDashViewModel model, String title, String message, String initialValue, IInputValidator validator);

	/**
	 * Brings up the UI to enter application deployment manifest
	 * @param defaultData
	 * @param project
	 * @param manifest
	 * @param defaultYaml
	 * @param readOnly
	 * @param noModeSwicth
	 * @return
	 * @throws OperationCanceledException
	 */
	CloudApplicationDeploymentProperties promptApplicationDeploymentProperties(Map<String, Object> defaultData,
			IProject project, IFile manifest, String defaultYaml, boolean readOnly,
			boolean noModeSwicth) throws OperationCanceledException;

	/**
	 * Ask the user to select a file.
	 * @param title The title of the open file dialog
	 * @param file The default path/file that should be used when opening the dialog
	 * @return The full path of the selected file
	 */
	String chooseFile(String title, String file);

	/**
	 * Ask the user to confirm or cancel an operation, with a toggle option.
	 *
	 *  @param propertyKey a preference name that will be used to remember the state of the 'toggle' option.
	 *  @param title Title for the dialog
	 *  @param message Detailed message
	 *  @param toggleMessage Message for the 'togle switch'.
	 */
	boolean confirmWithToggle(String propertyKey, String title, String message, String toggleMessage);


	/**
	 * Ask the user to answer 'yes' or 'no' to a question with a 'toggle' to optionally remember the answer.
	 *
	 *  @param propertyKey a preference name that will be used to remember the state of the 'toggle' and 'answer'.
	 *  @param title Title for the dialog
	 *  @param message Detailed message
	 *  @param toggleMessage Message for the 'togle switch'.
	 */
	boolean yesNoWithToggle(String propertyKey, String title, String message, String toggleMessage);

	/**
	 * Opens manifest compare dialog based on the supplied input
	 * @param the input for the compare dialog
	 * @return yes/no/cancel ids yes - use updated manifest, no - stop using manifest
	 */
	int openManifestCompareDialog(CompareEditorInput input) throws CoreException;
}
