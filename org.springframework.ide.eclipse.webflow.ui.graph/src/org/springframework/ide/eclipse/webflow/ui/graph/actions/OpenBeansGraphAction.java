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

import org.eclipse.ui.IEditorPart;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.graph.editor.GraphEditor;
import org.springframework.ide.eclipse.beans.ui.graph.editor.GraphEditorInput;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

/**
 * 
 */
public class OpenBeansGraphAction extends AbstractBeansEditorPartAction {

	/**
	 * 
	 */
	public static final String OPEN_FILE_REQUEST = "Open_beans_graph";

	/**
	 * 
	 */
	public static final String OPEN_FILE = "Open_beans_graph";

	/**
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
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() {
		IBean bean = getBean();
		if (bean != null) {
			GraphEditorInput graphEditorInput = new GraphEditorInput(bean
					.getElementID());
			SpringUIUtils.openInEditor(graphEditorInput, GraphEditor.EDITOR_ID);
		}
	}
}
