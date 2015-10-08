/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.properties.editor.quickfix;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.preference.IPreferenceStore;
import org.springframework.ide.eclipse.boot.properties.editor.ui.UserInteractions;

/**
 * Provides access to additional context info and objects that quickfixes might
 * need in order to be able to apply themselves.
 *
 * @author Kris De Volder
 */
public interface QuickfixContext {
	IProject getProject();
	IPreferenceStore getWorkspacePreferences();
	IPreferenceStore getProjectPreferences();
	IJavaProject getJavaProject();
	UserInteractions getUI();
}
