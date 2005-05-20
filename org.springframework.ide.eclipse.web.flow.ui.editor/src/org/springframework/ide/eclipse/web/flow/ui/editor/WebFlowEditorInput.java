/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ide.eclipse.web.flow.ui.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.web.flow.core.WebFlowCorePlugin;
import org.springframework.ide.eclipse.web.flow.core.WebFlowDefinitionException;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfig;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfigSet;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowProject;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;
import org.springframework.ide.eclipse.web.flow.ui.model.ConfigNode;
import org.springframework.ide.eclipse.web.flow.ui.model.ConfigSetNode;
import org.springframework.ide.eclipse.web.flow.ui.model.INode;

public class WebFlowEditorInput implements IEditorInput {

    private IFile file;

    private String name;

    private INode node;

    private INode parent;

    private String toolTip;

    public WebFlowEditorInput(IFile node) {
        this.file = node;
    }

    public WebFlowEditorInput(INode node) {
        this.node = node;

        if (node instanceof ConfigNode) {
            name = node.getName();
            toolTip = node.getName();
            file = ((ConfigNode) node).getConfigFile();
            parent = ((ConfigNode) node).getParent();
        }
    }

    public boolean equals(Object obj) {
        if (obj instanceof WebFlowEditorInput) {
            return ((WebFlowEditorInput) obj).getFile().equals(this.getFile());
        }
        return false;
    }

    public boolean exists() {
        return false;
    }

    public Object getAdapter(Class adapter) {
        if (node != null)
            return node.getAdapter(adapter);
        else
            return null;
    }

    public IBeansConfigSet getBeansConfigSet() {
        IWebFlowConfigSet configSet = this.getWebFlowConfigSet();
        if (configSet != null) {
            return configSet.getBeansConfigSet();
        }
        return null;
    }

    public IFile getFile() {
        return this.file;
    }

    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    public String getName() {
        return name;
    }

    public IPersistableElement getPersistable() {
        return null;
    }

    public IWebFlowState getRootState() throws WebFlowDefinitionException {
        IWebFlowProject project = WebFlowCorePlugin.getModel().getProject(
                file.getProject());
        IWebFlowConfig config = project.getConfig(file);
        IWebFlowState state = (IWebFlowState) config.getState(true);

        if (config.getException() != null) {
            throw config.getException();
        }
        return state;
    }

    public String getToolTipText() {
        return toolTip;
    }

    public IWebFlowConfigSet getWebFlowConfigSet() {
        if (parent != null && parent instanceof ConfigSetNode) {
            return ((ConfigSetNode) parent).getWebFlowConfigSet();
        }
        return null;
    }

    public int hashCode() {
        return this.getFile().hashCode();
    }
}