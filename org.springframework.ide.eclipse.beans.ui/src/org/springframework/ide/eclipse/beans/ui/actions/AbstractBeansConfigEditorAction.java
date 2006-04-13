/*
 * Copyright 2002-2006 the original author or authors.
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

	public ITextEditor getTextEditor() {
		return editor;
	}

	public IFile getConfigFile() {
		return file;
	}

	public void init(IWorkbenchWindow window) {
		// do nothing
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
}
