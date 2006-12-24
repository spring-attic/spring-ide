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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.ui.graph.editor.GraphEditor;
import org.springframework.ide.eclipse.beans.ui.graph.editor.GraphEditorInput;
import org.springframework.ide.eclipse.beans.ui.navigator.BeansExplorer;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * Shows the BeansGraph for the currently selected {@link IModelElement} in the
 * Project Explorer.
 * 
 * @author Torsten Juergeleit
 */
public class ShowBeansGraphAction extends Action {

	private ICommonActionExtensionSite site;
	private IResourceModelElement element;
	private TreePath path;

	public ShowBeansGraphAction(ICommonActionExtensionSite site) {
		this.site = site;
		setText("Show &Graph");	// TODO externalize text
    }

	public boolean isEnabled() {
		ISelection selection = site.getViewSite().getSelectionProvider()
				.getSelection();
		if (selection instanceof ITreeSelection) {
			ITreeSelection tSelection = (ITreeSelection) selection;
			if (tSelection.size() == 1) {
				Object tElement = tSelection.getFirstElement();
				IResourceModelElement rElement = null;
				if (tElement instanceof IResourceModelElement) {
					if (tElement instanceof IBeansConfig
							|| tElement instanceof IBeansConfigSet
							|| tElement instanceof IBean) {
						rElement = (IResourceModelElement) tElement;
					}
				} else if (tElement instanceof IFile) {
					if (site.getViewSite().getId().equals(
							BeansExplorer.BEANS_EXPLORER_ID)) {
						rElement = BeansCorePlugin.getModel().getConfig(
								(IFile) tElement);
					}
				} else if (tElement instanceof ZipEntryStorage) {
					rElement = BeansModelUtils
							.getConfig((ZipEntryStorage) tElement);
				}
				if (rElement != null) {
					element = (IResourceModelElement) rElement;
					path = tSelection.getPaths()[0];
					return true;
				}
			}
		}
		return false;
	}

	public void run() {
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
		SpringUIUtils.openInEditor(input, GraphEditor.EDITOR_ID);
	}
}
