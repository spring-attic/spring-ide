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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.beans.ui.navigator.BeansExplorer;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

/**
 * Opens the file for currently selected {@link IBeansConfig}.
 * 
 * @author Torsten Juergeleit
 */
public class OpenConfigFileAction extends Action {

	private ICommonActionExtensionSite site;
	private IResourceModelElement element;

	public OpenConfigFileAction(ICommonActionExtensionSite site) {
		this.site = site;
		setText("Op&en");	// TODO externalize text
	}

	@Override
	public boolean isEnabled() {
		ISelection selection = site.getViewSite().getSelectionProvider()
				.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sSelection = (IStructuredSelection) selection;
			if (sSelection.size() == 1) {
				Object sElement = sSelection.getFirstElement();
				IResourceModelElement rElement = null;
				if (sElement instanceof IResourceModelElement) {
					rElement = ((IResourceModelElement) sSelection
						.getFirstElement());
				} else if (sElement instanceof IFile) {
					if (site.getViewSite().getId().equals(
							BeansExplorer.BEANS_EXPLORER_ID)) {
						rElement = BeansCorePlugin.getModel().getConfig(
								(IFile) sElement);
					}
				} else if (sElement instanceof ZipEntryStorage) {
					rElement = BeansModelUtils
							.getConfig((ZipEntryStorage) sElement);
				}
				if (rElement instanceof ISourceModelElement
						|| rElement instanceof IBeansConfig) {
					element = rElement;
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void run() {
		if (isEnabled()) {
			BeansUIUtils.openInEditor(element);
		}
	}
}
