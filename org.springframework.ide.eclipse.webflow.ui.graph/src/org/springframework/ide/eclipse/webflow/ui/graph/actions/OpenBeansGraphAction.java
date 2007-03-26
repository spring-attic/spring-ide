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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.ui.graph.editor.GraphEditor;
import org.springframework.ide.eclipse.beans.ui.graph.editor.GraphEditorInput;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.webflow.core.internal.model.Action;
import org.springframework.ide.eclipse.webflow.core.internal.model.AttributeMapper;
import org.springframework.ide.eclipse.webflow.core.internal.model.BeanAction;
import org.springframework.ide.eclipse.webflow.core.internal.model.ExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.ui.graph.WebflowEditor;
import org.springframework.ide.eclipse.webflow.ui.graph.WebflowUtils;

/**
 * 
 */
public class OpenBeansGraphAction extends EditorPartAction {

	/**
	 * 
	 */
	public static final String OPEN_FILE_REQUEST = "Open_beans_graph";

	/**
	 * 
	 */
	public static final String OPEN_FILE = "Open_beans_graph";

	/**
	 * 
	 * 
	 * @param editor
	 */
	public OpenBeansGraphAction(IEditorPart editor) {
		super(editor);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#init()
	 */
	protected void init() {
		setId(OpenBeansGraphAction.OPEN_FILE);
		setText("Open Beans Graph");
		setToolTipText("Open element in beans graph");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
	 */
	protected boolean calculateEnabled() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#isEnabled()
	 */
	public boolean isEnabled() {
		if (getFirstSelectedEditPart() != null) {
			Object flowModelElement = getFirstSelectedEditPart().getModel();
			IWebflowConfig config = WebflowUtils.getActiveWebflowConfig();
			if (config != null && config.getBeansConfigs() != null
					&& config.getBeansConfigs().size() > 0) {
				if (flowModelElement instanceof Action) {
					Action action = (Action) flowModelElement;
					return action.getBean() != null;
				}
				else if (flowModelElement instanceof BeanAction) {
					BeanAction action = (BeanAction) flowModelElement;
					return action.getBean() != null;
				}
				else if (flowModelElement instanceof ExceptionHandler) {
					ExceptionHandler action = (ExceptionHandler) flowModelElement;
					return action.getBean() != null;
				}
				else if (flowModelElement instanceof AttributeMapper) {
					AttributeMapper action = (AttributeMapper) flowModelElement;
					return action.getBean() != null;
				}
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		Object flowModelElement = getFirstSelectedEditPart().getModel();
		IBean bean = null;
		IBeansConfig beansConfig = null;
		String beanId = null;
		IWebflowConfig config = WebflowUtils.getActiveWebflowConfig();
		if (config != null && config.getBeansConfigs() != null
				&& config.getBeansConfigs().size() > 0) {
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
			Set<IBeansConfig> configs = config.getBeansConfigs();
			for (IBeansConfig bc : configs) {
				if (bc.getBean(beanId) != null) {
					bean = bc.getBean(beanId);
					beansConfig = bc;
				}
			}
		}
		if (bean != null) {
			GraphEditorInput graphEditorInput = new GraphEditorInput(bean,
					beansConfig);
			SpringUIUtils.openInEditor(graphEditorInput, GraphEditor.EDITOR_ID);
		}
		else {
			MessageDialog.openError(getWorkbenchPart().getSite().getShell(),
					"Error opening Beans Graph",
					"The referenced bean cannot be located in Beans ConfigSet '"
							+ beansConfig.getElementName() + "'");
		}

	}

	/**
	 * 
	 * 
	 * @return
	 */
	protected EditPart getFirstSelectedEditPart() {
		GraphicalViewer viewer = ((WebflowEditor) getWorkbenchPart())
				.getGraphViewer();
		List list = viewer.getSelectedEditParts();
		if (!list.isEmpty()) {
			return (EditPart) list.get(0);
		}
		return null;
	}
}
