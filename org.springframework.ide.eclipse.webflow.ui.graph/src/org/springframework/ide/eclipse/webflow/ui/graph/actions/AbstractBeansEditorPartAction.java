/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph.actions;

import java.util.List;
import java.util.Set;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.actions.EditorPartAction;
import org.eclipse.ui.IEditorPart;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.webflow.core.internal.model.Action;
import org.springframework.ide.eclipse.webflow.core.internal.model.AttributeMapper;
import org.springframework.ide.eclipse.webflow.core.internal.model.BeanAction;
import org.springframework.ide.eclipse.webflow.core.internal.model.ExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.ui.graph.WebflowEditor;
import org.springframework.ide.eclipse.webflow.ui.graph.WebflowUtils;

/**
 * Abstract class that calculates enablement of {@link WebflowEditor} actions
 * that interact with the {@link BeansModel}.
 * 
 * @author Christian Dupuis
 */
public abstract class AbstractBeansEditorPartAction extends EditorPartAction {

	public AbstractBeansEditorPartAction(IEditorPart editor) {
		super(editor);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#isEnabled()
	 */
	public final boolean calculateEnabled() {
		return getBean() != null;
	}

	protected final IBean getBean() {
		IBean bean = null;
		if (getFirstSelectedEditPart() != null) {
			Object flowModelElement = getFirstSelectedEditPart().getModel();
			String beanId = null;
			IWebflowConfig config = WebflowUtils.getActiveWebflowConfig();
			if (config != null && config.getBeansConfigs() != null && config.getBeansConfigs().size() > 0) {
				if (flowModelElement instanceof Action) {
					Action action = (Action) flowModelElement;
					beanId = action.getBean();
				}
				else if (flowModelElement instanceof BeanAction) {
					BeanAction action = (BeanAction) flowModelElement;
					beanId = action.getBean();
				}
				else if (flowModelElement instanceof ExceptionHandler) {
					ExceptionHandler action = (ExceptionHandler) flowModelElement;
					beanId = action.getBean();
				}
				else if (flowModelElement instanceof AttributeMapper) {
					AttributeMapper action = (AttributeMapper) flowModelElement;
					beanId = action.getBean();
				}
			}
			if (beanId != null) {
				Set<IModelElement> configs = config.getBeansConfigs();
				for (IModelElement bc : configs) {
					if (BeansModelUtils.getBean(beanId, bc) != null) {
						bean = BeansModelUtils.getBean(beanId, bc);
						break;
					}
				}
			}
		}
		return bean;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	protected final EditPart getFirstSelectedEditPart() {
		GraphicalViewer viewer = ((WebflowEditor) getWorkbenchPart()).getGraphViewer();
		List list = viewer.getSelectedEditParts();
		if (!list.isEmpty()) {
			return (EditPart) list.get(0);
		}
		return null;
	}
}
