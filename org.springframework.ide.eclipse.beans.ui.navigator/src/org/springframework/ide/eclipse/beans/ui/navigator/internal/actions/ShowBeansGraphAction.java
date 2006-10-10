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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.ui.graph.editor.GraphEditor;
import org.springframework.ide.eclipse.beans.ui.graph.editor.GraphEditorInput;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * Shows the BeansGraph for the currently selected {@link IModelElement} in
 * the Project Explorer.
 * @author Torsten Juergeleit
 */
public class ShowBeansGraphAction extends Action {

	private ISelectionProvider provider;
	private IEditorInput input;

	public ShowBeansGraphAction(IWorkbenchPage page,
			ISelectionProvider provider) {
		setText("Show &Graph");	// TODO externalize text
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
				input = getEditorInput(element, tSelection.getPaths()[0]);
				if (input != null) {
					return true;
				}
			}
		}
		return false;
	}

	public void run() {
		SpringUIUtils.openInEditor(input, GraphEditor.EDITOR_ID);
	}

	private IEditorInput getEditorInput(IModelElement element, TreePath path) {
		IEditorInput input;
		if (element instanceof IBeansConfig
				|| element instanceof IBeansConfigSet) {
			input = new GraphEditorInput(element);
		} else {
			IModelElement context = null;
			for (int i = path.getSegmentCount() - 1; i > 0; i--) {
				Object segment = path.getSegment(i);
				if (segment instanceof IBeansConfigSet
						|| segment instanceof IBeansConfig) {
					context = (IModelElement) segment;
					break;
				}
			}
			input = new GraphEditorInput(element, context);
		}
		return input;
	}
}
