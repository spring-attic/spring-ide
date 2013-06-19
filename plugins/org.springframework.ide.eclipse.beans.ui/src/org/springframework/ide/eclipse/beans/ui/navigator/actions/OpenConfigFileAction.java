/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.navigator.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.generators.BeansConfigFactory;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.core.io.ZipEntryStorage;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;
import org.springframework.ide.eclipse.ui.navigator.actions.AbstractNavigatorAction;

/**
 * Opens the file for currently selected {@link IBeansConfig}.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class OpenConfigFileAction extends AbstractNavigatorAction {

	private IResourceModelElement element;

	public OpenConfigFileAction(ICommonActionExtensionSite site) {
		super(site);
		setText("Op&en"); // TODO externalize text
	}

	public boolean isEnabled(IStructuredSelection selection) {
		if (selection.size() == 1) {
			Object sElement = selection.getFirstElement();
			IResourceModelElement rElement = null;
			if (sElement instanceof IResourceModelElement) {
				rElement = ((IResourceModelElement) selection.getFirstElement());
			}
			else if (sElement instanceof IFile) {
				if (BeansUIPlugin.SPRING_EXPLORER_CONTENT_PROVIDER_ID.equals(getActionSite()
						.getExtensionId())) {
					rElement = BeansCorePlugin.getModel().getConfig(BeansConfigFactory.getConfigId((IFile) sElement), true);

				}
			}
			else if (sElement instanceof ZipEntryStorage) {
				rElement = BeansModelUtils.getConfig((ZipEntryStorage) sElement);
			}
			else if (sElement instanceof IAdaptable
					&& ((IAdaptable) sElement).getAdapter(IBean.class) != null) {
				rElement = (IBean) ((IAdaptable) sElement).getAdapter(IBean.class);
			}
			if (rElement instanceof ISourceModelElement || rElement instanceof IBeansConfig) {
				// Check if the selected element can be adapted to IBean
				if (rElement instanceof IAdaptable
						&& ((IAdaptable) rElement).getAdapter(IBean.class) != null) {
					rElement = (IBean) ((IAdaptable) rElement).getAdapter(IBean.class);
				}
				element = rElement;
				return true;
			}
		}
		return false;
	}

	@Override
	public final void run() {
		BeansUIUtils.openInEditor(element);
	}
}
