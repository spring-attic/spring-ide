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
package org.springframework.ide.eclipse.beans.ui.navigator.internal;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.navigator.ILinkHelper;
import org.eclipse.ui.part.FileEditorInput;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * @author Torsten Juergeleit
 */
public class BeansNavigatorLinkHelper implements ILinkHelper {

	public void activateEditor(IWorkbenchPage page,
			IStructuredSelection selection) {
		if (selection != null && !selection.isEmpty()) {
			Object sElement = selection.getFirstElement();
			if (sElement instanceof ISourceModelElement
					|| sElement instanceof BeansConfig) {
				IResourceModelElement element = (IResourceModelElement)
						sElement;
				IResource resource = element.getElementResource();
				if (resource instanceof IFile && resource.exists()) {
					IEditorInput input = new FileEditorInput((IFile) resource);
					IEditorPart editor = page.findEditor(input);
					if (editor != null) {
						page.bringToTop(editor);
						int line;
						if (element instanceof ISourceModelElement) {
							line = ((ISourceModelElement) element)
									.getElementStartLine();
						} else {
							line = ((BeansConfig) element)
									.getElementStartLine();
						}
						SpringUIUtils.revealInEditor(editor, line);
					}
				}
			}
		}
	}

	public IStructuredSelection findSelection(IEditorInput input) {
		if (input instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput) input).getFile();
			IBeansConfig config = BeansCorePlugin.getModel().getConfig(file);
			if (config != null) {
				IEditorPart editor = SpringUIUtils.getActiveEditor();
				if (editor.getEditorInput() == input
						&& editor.getSite() != null) {
					ISelection selection = editor.getSite()
							.getSelectionProvider().getSelection();
					IModelElement element = BeansUIUtils.getSelectedElement(
							selection, config);
					if (element != null) {
						return new TreeSelection(BeansUIUtils
								.createTreePath(element));
					}
				}
			}
		}
		return StructuredSelection.EMPTY;
	}
}
