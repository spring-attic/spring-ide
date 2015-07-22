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
}
