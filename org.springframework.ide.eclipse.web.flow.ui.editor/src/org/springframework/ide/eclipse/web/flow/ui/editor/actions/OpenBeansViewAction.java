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

import org.eclipse.core.resources.IFile;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.actions.EditorPartAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.springframework.ide.eclipse.beans.core.internal.model.Bean;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.beans.ui.views.BeansViewLocation;
import org.springframework.ide.eclipse.web.flow.core.model.IAction;
import org.springframework.ide.eclipse.web.flow.core.model.IAttributeMapper;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowEditor;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowEditorInput;
import org.springframework.ide.eclipse.web.flow.ui.editor.WebFlowUtils;

public class OpenBeansViewAction extends EditorPartAction {

    public static final String OPEN_FILE_REQUEST = "Open_beans_view";

    public static final String OPEN_FILE = "Open_beans_view";

    public OpenBeansViewAction(IEditorPart editor) {
        super(editor);
    }

    protected void init() {
        setId(OpenBeansViewAction.OPEN_FILE);
        setText("Open Beans View");
        setToolTipText("Open element in beans view");
    }

    protected boolean calculateEnabled() {
        return true;
    }

    public boolean isEnabled() {
        Object flowModelElement = getFirstSelectedEditPart().getModel();
        WebFlowEditorInput input = WebFlowUtils.getActiveFlowEditorInput();
        IBeansConfigSet beansConfig = input.getBeansConfigSet();
        if (beansConfig != null) {
            if (flowModelElement instanceof IAction) {
                IAction action = (IAction) flowModelElement;
                if (action.getBean() != null || action.getBeanClass() != null) {
                    return true;
                }
            }
            else if (flowModelElement instanceof IAttributeMapper) {
                IAttributeMapper mapper = (IAttributeMapper) flowModelElement;
                if (mapper.getBean() != null || mapper.getBeanClass() != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public void run() {
        Object flowModelElement = getFirstSelectedEditPart().getModel();
        WebFlowEditorInput input = WebFlowUtils.getActiveFlowEditorInput();
        IBeansConfigSet beansConfig = input.getBeansConfigSet();
        IBean bean = null;
        if (flowModelElement instanceof IAction) {
            IAction action = (IAction) flowModelElement;
            if (action.getBean() != null) {
                bean = beansConfig.getBean(action.getBean());
            }
            else if (action.getBeanClass() != null) {
                Collection beans = beansConfig.getBeans(action.getBeanClass());
                if (beans != null && beans.size() > 0) {
                    bean = (IBean) beans.toArray()[0];
                }
            }

        }
        else if (flowModelElement instanceof IAttributeMapper) {
            IAttributeMapper mapper = (IAttributeMapper) flowModelElement;
            if (mapper.getBean() != null) {
                bean = beansConfig.getBean(mapper.getBean());
            }
            else if (mapper.getBeanClass() != null) {
                Collection beans = beansConfig.getBeans(mapper.getBeanClass());
                if (beans != null && beans.size() > 0) {
                    bean = (IBean) beans.toArray()[0];
                }
            }
        }
        if (bean != null && bean instanceof Bean) {
            IFile file = (IFile) bean.getElementResource();
            if (file != null && file.exists()) {
                BeansViewLocation location = new BeansViewLocation();
                location.setProjectName(file.getProject().getName());
                location.setConfigName(file.getProjectRelativePath().toString());
                location.setBeanName(bean.getElementName());
                location.show();
            }
        }
        else {
            MessageDialog.openError(getWorkbenchPart().getSite().getShell(),
                    "Error opening Beans View",
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