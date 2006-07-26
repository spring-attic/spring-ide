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

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.IWorkbenchPage;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * Opens the project's property page for currently selected
 * {@link IModelElement} in Project Explorer.
 * @author Torsten Juergeleit
 */
public class OpenPropertiesAction extends Action {

	private ISelectionProvider provider;
	private IProject project;
	private int block = 0;

	public OpenPropertiesAction(IWorkbenchPage page,
			ISelectionProvider provider) {
		setText("&Properties");	// TODO externalize text
		this.provider = provider;
    }

	public boolean isEnabled() {
		ISelection selection = provider.getSelection();
		if (selection instanceof ITreeSelection) {
			ITreeSelection tSelection = (ITreeSelection) selection;
			if (tSelection.size() == 1
					&& tSelection.getFirstElement() instanceof IModelElement) {
				IModelElement element = ((IModelElement) tSelection
						.getFirstElement());
				project = BeansModelUtils.getProject(element).getProject();
				block = getProjectPropertyPageBlock(tSelection.getPaths()[0]);
				return true;
			}
		}
		return false;
	}

	public void run() {
		BeansUIUtils.showProjectPropertyPage(project, block);
	}

	/**
	 * Returns 1 if given path contains a segment of type
	 * {@link IBeansConfigSet} else 0.
	 */
	private int getProjectPropertyPageBlock(TreePath path) {
		for (int i = path.getSegmentCount() - 1; i > 0; i--) {
			Object segment = path.getSegment(i);
			if (segment instanceof IBeansConfigSet) {
				return 1;
			}
		}
		return 0;
	}
}
