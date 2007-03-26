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
package org.springframework.ide.eclipse.beans.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * Opens the Java type at current selection in Java editor.
 * @author Torsten Juergeleit
 */
public abstract class AbstractBeansConfigEditorAction extends ActionDelegate
			 implements IEditorActionDelegate, IWorkbenchWindowActionDelegate {
    private ITextEditor editor;
    private IFile file;
    private IWorkbenchWindow window;

	public ITextEditor getTextEditor() {
		return editor;
	}

	public IFile getConfigFile() {
		return file;
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public final void setActiveEditor(IAction proxyAction, IEditorPart part) {
		editor = SpringUIUtils.getTextEditor(part);
		if (editor != null) {
			file = BeansUIUtils.getConfigFile(editor);
		} else {
			editor = null;
			file = null;
		}

		// Disable action if not a Spring config file is currently edited 
		proxyAction.setEnabled(file != null);

		actionActivated(proxyAction);
	}

	/**
	 * This method is called from <code>setActiveEditor()</code>.
     * This implementation does nothing. Subclasses may reimplement.
     */
	protected void actionActivated(IAction proxyAction) {
		// do nothing
	}

	protected IWorkbenchWindow getWindow() {
		return window;
	}
}
