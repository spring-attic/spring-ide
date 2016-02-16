/*******************************************************************************
 * Copyright (c) 2006, 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * Abstract base class for beans configuration handlers
 *
 * @author Tomasz Zarna
 * @since 3.1.0
 */
public abstract class AbstractBeansConfigEditorHandler extends AbstractHandler {

	public ITextEditor getTextEditor(ExecutionEvent event) {
		IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
		return SpringUIUtils.getTextEditor(activeEditor);
	}

	public IFile getConfigFile(ExecutionEvent event) {
		ITextEditor editor = getTextEditor(event);
		if (editor != null) {
			return BeansUIUtils.getConfigFile(editor);
		}
		return null;
	}

	protected ITextSelection getCurrentSelection(ExecutionEvent event) {
		ISelectionProvider provider = getTextEditor(event).getSelectionProvider();
		if (provider != null) {
			ISelection selection = provider.getSelection();
			if (selection instanceof ITextSelection)
				return (ITextSelection) selection;
		}
		return TextSelection.emptySelection();
	}

}
