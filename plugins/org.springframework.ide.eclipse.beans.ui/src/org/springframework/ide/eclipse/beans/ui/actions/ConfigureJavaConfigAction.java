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
package org.springframework.ide.eclipse.beans.ui.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansJavaConfig;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.properties.model.PropertiesModel;
import org.springframework.ide.eclipse.beans.ui.properties.model.PropertiesProject;
import org.springframework.ide.eclipse.ui.navigator.actions.AbstractNavigatorAction;

/**
 * @author Leo Dos Santos
 */
public class ConfigureJavaConfigAction extends AbstractNavigatorAction {

	private boolean isAddOperation = true;

	private Object element;

	public ConfigureJavaConfigAction(ICommonActionExtensionSite actionSite) {
		super(actionSite);
		setText("Add as Beans Configuration");
	}

	@Override
	protected boolean isEnabled(IStructuredSelection selection) {
		element = selection.getFirstElement();
		if (element instanceof BeansJavaConfig) {
			setText("Remove as Beans Configuration");
			isAddOperation = false;
			return true;
		}
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void run() {
		if (isAddOperation) {
			
		} else {
			if (element instanceof IBeansConfig) {
				IBeansConfig config = (IBeansConfig) element;
				IFile file = BeansModelUtils.getFile(config);
				IBeansProject project = BeansModelUtils.getProject(config);
				if (file != null && project != null) {
					PropertiesProject modelProject = new PropertiesProject(
							new PropertiesModel(), project);
					modelProject.removeConfig(file);
					modelProject.saveDescription();
				}
			}
		}
	}

}
