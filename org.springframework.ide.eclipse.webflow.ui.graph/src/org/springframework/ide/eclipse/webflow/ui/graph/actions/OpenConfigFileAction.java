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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.actions.EditorPartAction;
import org.eclipse.ui.IEditorPart;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.ui.graph.WebflowEditor;
import org.springframework.ide.eclipse.webflow.ui.graph.WebflowEditorInput;
import org.springframework.ide.eclipse.webflow.ui.graph.WebflowUtils;
import org.springframework.ide.eclipse.webflow.ui.graph.parts.AbstractStatePart;
import org.springframework.ide.eclipse.webflow.ui.graph.parts.StateTransitionPart;

/**
 * 
 */
public class OpenConfigFileAction extends EditorPartAction {

	/**
	 * 
	 */
	public static final String OPEN_FILE_REQUEST = "Open_config";

	/**
	 * 
	 */
	public static final String OPEN_FILE = "Open_config";

	/**
	 * @param editor
	 */
	public OpenConfigFileAction(IEditorPart editor) {
		super(editor);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#init()
	 */
	protected void init() {
		setId(OpenConfigFileAction.OPEN_FILE);
		setText("Open Config File");
		setToolTipText("Open element in config file");
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
		EditPart part = getFirstSelectedEditPart();
		if (part instanceof AbstractStatePart
				|| part instanceof StateTransitionPart) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		Object flowModelElement = getFirstSelectedEditPart().getModel();
		if (flowModelElement instanceof IWebflowModelElement) {
			IWebflowModelElement element = (IWebflowModelElement) flowModelElement;
			WebflowEditorInput input = WebflowUtils.getActiveFlowEditorInput();
			IResource file = input.getFile();
			if (file != null && file.exists()) {
				SpringUIUtils.openInEditor((IFile) file, input
						.getElementStartLine(element.getNode()));
			}
		}
	}

	/**
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
