/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.beans.ui.graph.actions;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.actions.EditorPartAction;
import org.eclipse.ui.IEditorPart;
import org.springframework.ide.eclipse.beans.ui.graph.BeansGraphPlugin;
import org.springframework.ide.eclipse.beans.ui.graph.editor.GraphEditor;
import org.springframework.ide.eclipse.beans.ui.graph.model.Bean;
import org.springframework.ide.eclipse.beans.ui.graph.parts.BeanPart;
import org.springframework.ide.eclipse.ui.SpringUIUtils;

public class OpenConfigFile extends EditorPartAction {

	public OpenConfigFile(IEditorPart editor) {
		super(editor);
	}

	protected void init() {
		setId(GraphActionConstants.OPEN_FILE);
		setText(BeansGraphPlugin.getResourceString(
												  "ContextMenu.OpenFile.text"));
	}

	protected boolean calculateEnabled() {
		return true;
	}

	public boolean isEnabled() {
		EditPart part = getFirstSelectedEditPart();
		if (part instanceof BeanPart) {
			return true;
		}
		return false;
	}

	public void run() {
		Bean bean = ((BeanPart) getFirstSelectedEditPart()).getBean();
		IFile file = bean.getConfigFile();
		if (file != null && file.exists()) {
			SpringUIUtils.openInEditor(file, bean.getStartLine());
		}
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
