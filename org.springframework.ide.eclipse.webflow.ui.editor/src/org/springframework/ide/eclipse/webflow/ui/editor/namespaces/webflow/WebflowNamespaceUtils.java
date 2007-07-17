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
package org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;
import org.w3c.dom.Node;

public class WebflowNamespaceUtils {

	public static Node locateFlowRootNode(Node node) {
		if (!"flow".equals(node.getLocalName()) && node.getParentNode() != null) {
			return locateFlowRootNode(node.getParentNode());
		}
		else {
			return node;
		}
	}

	public static IWebflowConfig getActiveWebflowConfig() {
		IEditorInput editorInput = SpringUIUtils.getActiveEditor()
				.getEditorInput();
		if (editorInput != null && editorInput instanceof IFileEditorInput) {
			IWebflowProject project = org.springframework.ide.eclipse.webflow.core.Activator
					.getModel().getProject(
							((IFileEditorInput) editorInput).getFile()
									.getProject());
			if (project != null) {
				return project.getConfig(((IFileEditorInput) editorInput).getFile());
			}
		}
		return null;
	}

	public static String[] getWebflowConfigNames() {
		IWebflowProject project = getActiveWebflowConfig().getProject();
		return WebflowModelUtils.getWebflowConfigNames(project).toArray(
				new String[0]);
	}
}
