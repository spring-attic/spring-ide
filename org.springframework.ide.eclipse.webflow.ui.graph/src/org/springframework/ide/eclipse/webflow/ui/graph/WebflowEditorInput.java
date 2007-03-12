/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.ide.eclipse.webflow.ui.graph;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowEditorInput implements IEditorInput, IPersistableElement {

    private IWebflowConfig config;

    private boolean isValid = true;
    
    private String name;
    
    private String tooltip;

    public WebflowEditorInput(IWebflowConfig config) {
    	this.config = config;
    	this.tooltip = config.getResource().getFullPath().makeRelative().toString();
        this.name = config.getResource().getFullPath().makeRelative().toString();
    }

    public boolean equals(Object obj) {
        if (obj instanceof WebflowEditorInput) {
            return ((WebflowEditorInput) obj).getFile().equals(this.getFile());
        }
        return false;
    }

    public boolean exists() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IPersistableElement#getFactoryId()
     */
    public String getFactoryId() {
        return WebflowEditorInputFactory.getFactoryId();
    }

    public IFile getFile() {
        return this.config.getResource();
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

    public String getToolTipText() {
        return tooltip;
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
        if (this.isValid) {
            WebflowEditorInputFactory.saveState(memento, this);
        }
    }
    
    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

	public Object getAdapter(Class adapter) {
		return this.config.getResource().getAdapter(adapter);
	}
}