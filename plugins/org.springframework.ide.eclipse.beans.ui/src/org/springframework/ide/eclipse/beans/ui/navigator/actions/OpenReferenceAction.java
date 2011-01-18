/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.navigator.actions;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeanAlias;
import org.springframework.ide.eclipse.beans.core.model.IBeanReference;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.ui.navigator.actions.AbstractNavigatorAction;

/**
 * Opens the config file of the referenced bean for the currently selected
 * {@link IBeanAlias} or {@link IBeanReference}.
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class OpenReferenceAction extends AbstractNavigatorAction {

	private IBean bean;

	public OpenReferenceAction(ICommonActionExtensionSite site) {
		super(site);
		setText("Open &Reference");	// TODO externalize text
	}

	public boolean isEnabled(IStructuredSelection selection) {
		if (selection.size() == 1) {
			Object element = selection.getFirstElement();
			String beanName = null;
			IBeansConfig config = null;
			if (element instanceof IBeanAlias) {
				IBeanAlias alias = (IBeanAlias) element;
				beanName = alias.getBeanName();
				config = BeansModelUtils.getConfig(alias);
			}
			else if (element instanceof IBeanReference) {
				IBeanReference reference = (IBeanReference) element;
				beanName = reference.getBeanName();
				config = BeansModelUtils.getConfig(reference);
			}
			if (beanName != null) {
				if (selection instanceof ITreeSelection) {
					IModelElement context = BeansUIUtils
							.getContext((ITreeSelection) selection);
					if (context instanceof IBeansConfig) {
						bean = BeansModelUtils.getBeanWithConfigSets(beanName,
								(IBeansConfig) context);
					}
					else {
						bean = BeansModelUtils.getBean(beanName, context);
					}
				}
				else {
					bean = config.getBean(beanName);
				}
				if (bean != null) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public final void run() {
		BeansUIUtils.openInEditor(bean);
	}
}
