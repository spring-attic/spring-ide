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
package org.springframework.ide.eclipse.quickfix.tests;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigEditor;
import org.springsource.ide.eclipse.commons.tests.util.StsTestCase;


public class AbstractCompilationUnitTestCase extends StsTestCase {

	public AbstractConfigEditor cuEditor;

	@Override
	protected String getBundleName() {
		return "org.springframework.ide.eclipse.quickfix.tests";
	}

	@SuppressWarnings("restriction")
	protected CompilationUnitEditor openFileInEditor(IFile file) throws CoreException, IOException {

		if (file.exists()) {
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			if (window != null) {
				IWorkbenchPage page = window.getActivePage();
				if (page != null) {
					IEditorPart editor = IDE.openEditor(page, file);
					if (editor != null) {
						return ((CompilationUnitEditor) editor);
					}
				}
			}
		}
		return null;
	}
}
