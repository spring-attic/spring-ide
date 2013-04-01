/*******************************************************************************
 *  Copyright (c) 2012 - 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.livegraph.actions;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBean;

/**
 * @author Leo Dos Santos
 */
public class OpenBeanClassAction extends AbstractOpenResourceAction {

	public OpenBeanClassAction() {
		super("Open Bean Class");
	}

	@Override
	public void run() {
		IStructuredSelection selection = getStructuredSelection();
		List elements = selection.toList();
		for (Object obj : elements) {
			if (obj instanceof LiveBean) {
				LiveBean bean = (LiveBean) obj;
				String appName = bean.getApplicationName();
				String beanClass = bean.getBeanType();
				if (appName != null) {
					if (beanClass != null && beanClass.trim().length() > 0) {
						openInEditor(appName, beanClass);
					}
					else {
						String beanId = bean.getId();
						if (beanId != null && beanId.trim().length() > 0) {
							openInEditor(appName, beanId);
						}
					}
				}
			}
		}
	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (!selection.isEmpty()) {
			List elements = selection.toList();
			for (Object obj : elements) {
				if (obj instanceof LiveBean) {
					LiveBean bean = (LiveBean) obj;
					String beanClass = bean.getBeanType();
					if (beanClass != null && beanClass.trim().length() > 0) {
						return true;
					}
					else {
						String beanId = bean.getId();
						if (beanId != null && beanId.trim().length() > 0) {
							return hasType(bean.getApplicationName(), beanId);
						}
					}
				}
			}
		}
		return false;
	}

}
