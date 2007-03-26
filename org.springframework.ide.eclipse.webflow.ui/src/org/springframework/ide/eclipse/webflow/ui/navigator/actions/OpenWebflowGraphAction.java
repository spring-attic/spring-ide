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
package org.springframework.ide.eclipse.webflow.ui.navigator.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow.WebflowUIImages;
import org.springframework.ide.eclipse.webflow.ui.graph.WebflowEditor;
import org.springframework.ide.eclipse.webflow.ui.graph.WebflowEditorInput;

/**
 * Shows the WebflowEditor for the currently selected {@link IWebflowConfig}
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
public class OpenWebflowGraphAction extends Action {

	private ICommonActionExtensionSite site;

	private IWebflowConfig element;

	public OpenWebflowGraphAction(ICommonActionExtensionSite site) {
		this.site = site;
		setText("Open &Graphical Editor"); // TODO externalize text
		setImageDescriptor(WebflowUIImages.DESC_OBJS_WEBFLOW)
	;}

	@Override
	public boolean isEnabled() {
		ISelection selection = site.getViewSite().getSelectionProvider()
				.getSelection();
		if (selection instanceof ITreeSelection) {
			ITreeSelection tSelection = (ITreeSelection) selection;
			if (tSelection.size() == 1) {
				Object tElement = tSelection.getFirstElement();
				if (tElement instanceof IWebflowConfig) {
					element = (IWebflowConfig) tElement;
					return true;
				}
				else if (tElement instanceof IFile) {
					if (WebflowModelUtils.isWebflowConfig((IFile) tElement)) {
						element = WebflowModelUtils
								.getWebflowConfig((IFile) tElement);
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void run() {
		IEditorInput input = new WebflowEditorInput(element);
		SpringUIUtils.openInEditor(input, WebflowEditor.EDITOR_ID);
	}
}
