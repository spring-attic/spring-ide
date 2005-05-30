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

package org.springframework.ide.eclipse.web.flow.ui.editor;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfigSet;
import org.springframework.ide.eclipse.core.model.IModel;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.web.flow.core.WebFlowCorePlugin;
import org.springframework.ide.eclipse.web.flow.core.WebFlowDefinitionException;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfig;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowConfigSet;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowProject;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowState;
import org.springframework.ide.eclipse.web.flow.ui.model.ConfigNode;
import org.springframework.ide.eclipse.web.flow.ui.model.ConfigSetNode;
import org.springframework.ide.eclipse.web.flow.ui.model.INode;

public class WebFlowEditorInput implements IEditorInput, IPersistableElement {

    private String elementId;

    private IFile file;

    private String name;

    private INode node;

    private INode parent;

    private String toolTip;

    public WebFlowEditorInput(IFile node) {
        this(node, null);
    }
     
    public WebFlowEditorInput(IFile node, String elementId) {
        this.file = node;
        this.elementId = elementId;
        this.toolTip = file.getFullPath().makeRelative().toString();
    }

    public WebFlowEditorInput(INode node) {
        this.node = node;

        if (node instanceof ConfigNode) {
            name = node.getName();
            toolTip = node.getName();
            file = ((ConfigNode) node).getConfigFile();
            this.toolTip = file.getFullPath().makeRelative().toString();
            parent = ((ConfigNode) node).getParent();

            if (this.getWebFlowConfigSet() != null
                    && this.getWebFlowConfigSet().getBeansConfigSet() != null) {
                this.elementId = this.getWebFlowConfigSet().getBeansConfigSet()
                        .getElementID();
            }

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
        if (this.elementId != null) {
            IModel model = BeansCorePlugin.getModel(); 
            IModelElement element = model.getElement(elementId);
            return (IBeansConfigSet) element;
        }
        return null;
    }
    
    public String getBeansConfigSetElementId() {
        return this.elementId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPersistableElement#getFactoryId()
     */
    public String getFactoryId() {
        return WebFlowEditorInputFactory.getFactoryId();
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
        return this;
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

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPersistableElement#saveState(org.eclipse.ui.IMemento)
     */
    public void saveState(IMemento memento) {
        WebFlowEditorInputFactory.saveState(memento, this);
    }
    
    /**
     * @return Returns the elementId.
     */
    public String getElementId() {
        return elementId;
    }
}