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

package org.springframework.ide.eclipse.beans.ui.navigator.internal.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPage;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * @author Torsten Juergeleit
 */
public class OpenConfigFileAction extends Action {

	private ISelectionProvider provider;
	private ISourceModelElement element;

	public OpenConfigFileAction(IWorkbenchPage page,
			ISelectionProvider provider) {
		setText("Op&en");
		this.provider = provider;
	}

	public boolean isEnabled() {
		ISelection selection = provider.getSelection();
		if (!selection.isEmpty()) {
			IStructuredSelection sSelection = (IStructuredSelection) selection;
			if (sSelection.size() == 1
					&& sSelection.getFirstElement()
							instanceof ISourceModelElement) {
				element = ((ISourceModelElement) sSelection.getFirstElement());
				return true;
			}
		}
		return false;
	}

	public void run() {
		if (isEnabled()) {
			IResource resource = element.getElementResource();
			if (resource instanceof IFile && resource.exists()) {
				int line = element.getElementStartLine();
				SpringUIUtils.openInEditor((IFile) resource, line);
			}
		}
	}
}
