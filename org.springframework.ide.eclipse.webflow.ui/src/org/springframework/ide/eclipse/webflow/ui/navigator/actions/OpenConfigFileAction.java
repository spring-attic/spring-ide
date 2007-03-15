/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.ide.eclipse.webflow.ui.navigator.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;

/**
 * Open {@link IWebflowConfig} in the standard Eclipse editor
 * @author Christian Dupuis
 * @since 2.0
 */
public class OpenConfigFileAction extends Action {

	private ICommonActionExtensionSite site;

	private IWebflowConfig element;

	public OpenConfigFileAction(ICommonActionExtensionSite site) {
		this.site = site;
		setText("Op&en"); // TODO externalize text
	}

	@Override
	public boolean isEnabled() {
		ISelection selection = site.getViewSite().getSelectionProvider()
				.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sSelection = (IStructuredSelection) selection;
			if (sSelection.size() == 1) {
				Object sElement = sSelection.getFirstElement();
				if (sElement instanceof IWebflowConfig) {
					element = (IWebflowConfig) sElement;
					return true;
				}
				else if (sElement instanceof IFile) {
					if (WebflowModelUtils.isWebflowConfig((IFile) sElement)) {
						element = WebflowModelUtils.getWebflowConfig((IFile) sElement);
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void run() {
		if (isEnabled()) {
			SpringUIUtils.openInEditor(element.getResource(), -1);
		}
	}
}
