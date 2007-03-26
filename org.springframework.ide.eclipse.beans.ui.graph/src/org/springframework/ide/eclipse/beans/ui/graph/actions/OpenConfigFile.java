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
package org.springframework.ide.eclipse.beans.ui.graph.actions;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.actions.EditorPartAction;
import org.eclipse.ui.IEditorPart;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.beans.ui.graph.BeansGraphPlugin;
import org.springframework.ide.eclipse.beans.ui.graph.editor.GraphEditor;
import org.springframework.ide.eclipse.beans.ui.graph.model.Bean;
import org.springframework.ide.eclipse.beans.ui.graph.parts.BeanPart;

public class OpenConfigFile extends EditorPartAction {

	public OpenConfigFile(IEditorPart editor) {
		super(editor);
	}

	@Override
	protected void init() {
		setId(GraphActionConstants.OPEN_FILE);
		setText(BeansGraphPlugin.getResourceString(
												  "ContextMenu.OpenFile.text"));
	}

	@Override
	protected boolean calculateEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		EditPart part = getFirstSelectedEditPart();
		if (part instanceof BeanPart) {
			return true;
		}
		return false;
	}

	@Override
	public void run() {
		Bean bean = ((BeanPart) getFirstSelectedEditPart()).getBean();
		BeansUIUtils.openInEditor(bean.getBean());
	}

	protected EditPart getFirstSelectedEditPart() {
		GraphicalViewer viewer = ((GraphEditor)
									   getWorkbenchPart()).getGraphicalViewer();
		List list = viewer.getSelectedEditParts();
		if (!list.isEmpty()) {
			return (EditPart) list.get(0);
		}
		return null;
	}
}
