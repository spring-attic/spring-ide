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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.ISources;
import org.eclipse.ui.handlers.HandlerUtil;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;

/**
 * @author Leo Dos Santos
 */
public class RemoveFromConfigSetHandler extends AbstractHandler {
	
	private IBeansConfigSet configSet;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		return null;
	}

	@Override
	public boolean isEnabled() {
		return configSet != null && configSet.getConfigs().size() > 0;
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		configSet = null;
		Object selection = HandlerUtil.getVariable(evaluationContext, ISources.ACTIVE_CURRENT_SELECTION_NAME);
		if (selection instanceof StructuredSelection) {
			Object element = ((StructuredSelection) selection).getFirstElement();
			if (element instanceof IBeansConfigSet) {
				configSet = (IBeansConfigSet) element;
			}
		}
	}
	
}
