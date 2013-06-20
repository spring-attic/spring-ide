/*******************************************************************************
 *  Copyright (c) 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.HandlerUtil;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.beans.ui.properties.ProjectPropertyPage;

/**
 * @author Leo Dos Santos
 */
public class AddToNewConfigSetAction extends AbstractHandler {

	private IBeansConfig config;
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IBeansProject beansProject = BeansModelUtils.getProject(config);
		IProject project = beansProject.getProject();
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(ProjectPropertyPage.SELECTED_RESOURCE, config);
		data.put(ProjectPropertyPage.BLOCK_ID, 1);
		BeansUIUtils.showProjectPropertyPage(project, data);
		return null;
	}

	@Override
	public boolean isEnabled() {
		return config != null;
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		config = null;
		Object selection = HandlerUtil.getVariable(evaluationContext, ISources.ACTIVE_CURRENT_SELECTION_NAME);
		if (selection instanceof StructuredSelection) {
			Object element = ((StructuredSelection) selection).getFirstElement();
			if (element instanceof IBeansConfig) {
				config = (IBeansConfig) element;
			}
		}
	}
	
}
