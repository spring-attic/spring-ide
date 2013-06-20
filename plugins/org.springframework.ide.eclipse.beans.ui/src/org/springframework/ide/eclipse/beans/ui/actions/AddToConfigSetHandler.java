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

import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.HandlerUtil;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

/**
 * @author Leo Dos Santos
 */
public class AddToConfigSetHandler extends AbstractHandler {
	
	private IBeansConfig config;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		return null;
	}

	@Override
	public boolean isEnabled() {
		if (config != null) {
			IBeansProject project = BeansModelUtils.getProject(config);
			int count = 0;
			Set<IBeansConfigSet> configSets = project.getConfigSets();
			for (IBeansConfigSet configSet : configSets) {
				if (!configSet.hasConfig(config.getElementName())) {
					count++;
				}
			}
			return count > 0;
		}
		return false;
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
