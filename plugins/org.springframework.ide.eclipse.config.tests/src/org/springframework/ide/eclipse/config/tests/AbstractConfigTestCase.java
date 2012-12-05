/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.tests;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.springframework.ide.eclipse.config.core.preferences.SpringConfigPreferenceConstants;
import org.springframework.ide.eclipse.config.ui.ConfigUiPlugin;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigEditor;
import org.springsource.ide.eclipse.commons.tests.util.StsTestCase;

/**
 * Derived from AbstractBeansCoreTestCase
 * @author Leo Dos Santos
 * @author Terry Denney
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @author Tomasz Zarna
 */
public class AbstractConfigTestCase extends StsTestCase {

	public AbstractConfigEditor cEditor;

	private boolean prefEnableGefPages;

	@Override
	protected String getBundleName() {
		return "org.springframework.ide.eclipse.config.tests";
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		prefEnableGefPages = ConfigUiPlugin.getDefault().getPreferenceStore()
				.getBoolean(SpringConfigPreferenceConstants.PREF_ENABLE_GEF_PAGES);
	}

	@Override
	protected void tearDown() throws Exception {
		ConfigUiPlugin.getDefault().getPreferenceStore()
				.setValue(SpringConfigPreferenceConstants.PREF_ENABLE_GEF_PAGES, prefEnableGefPages);
		super.tearDown();
	}

	protected void enableGefPages(boolean enable) {
		ConfigUiPlugin.getDefault().getPreferenceStore()
				.setValue(SpringConfigPreferenceConstants.PREF_ENABLE_GEF_PAGES, enable);
	}

	protected AbstractConfigEditor openFileInEditor(String path) throws CoreException, IOException {
		IProject project = createPredefinedProject("ConfigTests");
		IFile file = project.getFile(path);
		if (file.exists()) {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window != null) {
				IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					IEditorPart editor = IDE.openEditor(page, file);
					if (editor != null) {
						return ((AbstractConfigEditor) editor);
					}
				}
			}
		}
		return null;
	}

}
