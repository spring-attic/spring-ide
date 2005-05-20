/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.springframework.ide.eclipse.web.flow.ui.editor.actions;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.actions.EditorPartAction;
import org.eclipse.ui.IEditorPart;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowEditor;
import org.springframework.ide.eclipse.web.flow.ui.editor.parts.AbstractStatePart;
import org.springframework.ide.eclipse.web.flow.ui.editor.parts.StateTransitionPart;

public class OpenConfigFileAction extends EditorPartAction {

    public static final String OPEN_FILE_REQUEST = "Open_config";

    public static final String OPEN_FILE = "Open_config";

    public OpenConfigFileAction(IEditorPart editor) {
        super(editor);
    }

    protected void init() {
        setId(OpenConfigFileAction.OPEN_FILE);
        setText("Open");
        setToolTipText("Open element in config file");
    }

    protected boolean calculateEnabled() {
        return true;
    }

    public boolean isEnabled() {
        EditPart part = getFirstSelectedEditPart();
        if (part instanceof AbstractStatePart
                || part instanceof StateTransitionPart) {
            return true;
        }
        return false;
    }

    public void run() {
        Object flowModelElement = getFirstSelectedEditPart().getModel();
        if (flowModelElement instanceof IWebFlowModelElement) {
            IWebFlowModelElement element = (IWebFlowModelElement) flowModelElement;
            IResource file = element.getElementResource();
            if (file != null && file.exists()) {
                SpringUIUtils.openInEditor((IFile) file, element
                        .getElementStartLine());
            }
        }
    }

    protected EditPart getFirstSelectedEditPart() {
        GraphicalViewer viewer = ((WebFlowEditor) getWorkbenchPart())
                .getGraphViewer();
        List list = viewer.getSelectedEditParts();
        if (!list.isEmpty()) {
            return (EditPart) list.get(0);
        }
        return null;
    }
}