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
package org.springframework.ide.eclipse.beans.ui.graph.internal.navigator.actions;

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
import org.springframework.ide.eclipse.beans.core.model.IBeansComponent;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.ui.graph.editor.GraphEditor;
import org.springframework.ide.eclipse.beans.ui.graph.editor.GraphEditorInput;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * Shows the BeansGraph for the currently selected {@link IModelElement}.
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

	@Override
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
							|| tElement instanceof IBeansComponent
							|| tElement instanceof IBean) {
						rElement = (IResourceModelElement) tElement;
					}
				} else if (tElement instanceof IFile) {
					rElement = BeansCorePlugin.getModel().getConfig(
							(IFile) tElement);
				} else if (tElement instanceof ZipEntryStorage) {
					rElement = BeansModelUtils
							.getConfig((ZipEntryStorage) tElement);
				}
				if (rElement != null) {
					element = rElement;
					path = tSelection.getPaths()[0];
					return true;
				}
			}
		}
		return false;
	}

	@Override
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
