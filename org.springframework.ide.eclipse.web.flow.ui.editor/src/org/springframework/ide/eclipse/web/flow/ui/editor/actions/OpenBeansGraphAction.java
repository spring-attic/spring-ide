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

import java.util.Collection;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.actions.EditorPartAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.ui.graph.editor.GraphEditor;
import org.springframework.ide.eclipse.beans.ui.graph.editor.GraphEditorInput;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.ui.SpringUIUtils;
import org.springframework.ide.eclipse.web.flow.core.model.IBeanReference;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowEditor;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowEditorInput;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowUtils;

public class OpenBeansGraphAction extends EditorPartAction {

    public static final String OPEN_FILE_REQUEST = "Open_beans_graph";

    public static final String OPEN_FILE = "Open_beans_graph";

    public OpenBeansGraphAction(IEditorPart editor) {
        super(editor);
    }

    protected void init() {
        setId(OpenBeansGraphAction.OPEN_FILE);
        setText("Open Beans Graph");
        setToolTipText("Open element in beans graph");
    }

    protected boolean calculateEnabled() {
        return true;
    }

    public boolean isEnabled() {
        if (getFirstSelectedEditPart() != null) {
            Object flowModelElement = getFirstSelectedEditPart().getModel();
            WebFlowEditorInput input = WebFlowUtils.getActiveFlowEditorInput();
            IBeansConfigSet beansConfig = input.getBeansConfigSet();
            if (beansConfig != null) {
                if (flowModelElement instanceof IBeanReference) {
                    IBeanReference action = (IBeanReference) flowModelElement;
                    if (action.getBean() != null
                            || action.getBeanClass() != null) {
                        return true;
                    }
                } 
            }
        }
        return false;
    }

    public void run() {
        Object flowModelElement = getFirstSelectedEditPart().getModel();
        WebFlowEditorInput input = WebFlowUtils.getActiveFlowEditorInput();
        IBeansConfigSet beansConfig = input.getBeansConfigSet();
        IModelElement bean = null;
        if (flowModelElement instanceof IBeanReference) {
            IBeanReference action = (IBeanReference) flowModelElement;
            if (action.getBean() != null) {
                bean = beansConfig.getBean(action.getBean());
            } else if (action.getBeanClass() != null) {
                Collection beans = beansConfig.getBeans(action.getBeanClass());
                if (beans != null && beans.size() > 0) {
                    bean = (IBean) beans.toArray()[0];
                }
            }

        } 
        if (bean != null) {
            GraphEditorInput graphEditorInput = new GraphEditorInput(bean,
                    beansConfig);
            SpringUIUtils.openInEditor(graphEditorInput, GraphEditor.EDITOR_ID);
        } else {
            MessageDialog.openError(getWorkbenchPart().getSite().getShell(),
                    "Error opening Beans Graph",
                    "The referenced bean cannot be located in Beans ConfigSet '"
                            + beansConfig.getElementName() + "'");
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