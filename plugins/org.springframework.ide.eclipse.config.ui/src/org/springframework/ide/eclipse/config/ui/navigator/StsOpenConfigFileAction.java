/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.ui.navigator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.wst.sse.ui.StructuredTextEditor;
import org.springframework.ide.eclipse.config.ui.editors.AbstractConfigEditor;
import org.springframework.ide.eclipse.config.ui.editors.webflow.SpringWebFlowEditor;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.ui.navigator.actions.OpenConfigFileAction;

/**
 * @author Leo Dos Santos
 * @since 2.5.2
 */
public class StsOpenConfigFileAction extends OpenConfigFileAction {

	public static final String WEBFLOW_SPRING_EXPLORER_ACTION_PROVIDER_ID = "com.springsource.sts.config.ui.navigator.webflow.springExplorerActions"; //$NON-NLS-1$

	private IFile file;

	public StsOpenConfigFileAction(ICommonActionExtensionSite site) {
		super(site);
	}

	@Override
	public boolean isEnabled(IStructuredSelection selection) {
		if (selection.size() == 1) {
			Object sElement = selection.getFirstElement();
			if (sElement instanceof IWebflowConfig) {
				file = ((IWebflowConfig) sElement).getResource();
				return true;
			}
			else if (sElement instanceof IFile) {
				if (WebflowModelUtils.isWebflowConfig((IFile) sElement)
						&& WEBFLOW_SPRING_EXPLORER_ACTION_PROVIDER_ID.equals(getActionSite().getExtensionId())) {
					file = (IFile) sElement;
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void run() {
		IEditorInput input = new FileEditorInput(file);
		IEditorPart part = SpringUIUtils.openInEditor(input, SpringWebFlowEditor.ID_EDITOR);
		if (part instanceof AbstractConfigEditor) {
			AbstractConfigEditor cEditor = (AbstractConfigEditor) part;
			StructuredTextEditor source = cEditor.getSourcePage();
			if (source != null) {
				cEditor.setActiveEditor(source);
			}
		}
	}

}
