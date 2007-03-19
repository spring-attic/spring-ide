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

package org.springframework.ide.eclipse.webflow.ui.graph.dialogs;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.springframework.ide.eclipse.webflow.core.model.IState;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;

/**
 * 
 */
public class ImportContentProvider implements IStructuredContentProvider,
        PropertyChangeListener {

    /**
     * 
     */
    private IWebflowModelElement project;

    /**
     * 
     */
    private TableViewer viewer;

    /**
     * 
     * 
     * @param viewer 
     * @param project 
     */
    public ImportContentProvider(IWebflowState project, TableViewer viewer) {
        this.project = project;
        this.viewer = viewer;
    }

    /**
     * 
     * 
     * @param viewer 
     * @param project 
     */
    public ImportContentProvider(IState project, TableViewer viewer) {
        this.project = project;
        this.viewer = viewer;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose() {
    	if (project != null) {
    		((IWebflowModelElement) project).removePropertyChangeListener(this);
    	}
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object obj) {
        if (project instanceof IWebflowState) { 
            return ((IWebflowState) project).getImports().toArray();
        }
        else 
            return new Object[0];
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        if (newInput != null)
            ((IWebflowModelElement) newInput).addPropertyChangeListener(this);
        if (oldInput != null)
            ((IWebflowModelElement) oldInput)
                    .removePropertyChangeListener(this);
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(IWebflowModelElement.ADD_CHILDREN)) {
            if (evt.getNewValue() != null) {
                this.viewer.add(evt.getNewValue());
            }
        }
        else if (evt.getPropertyName().equals(
                IWebflowModelElement.REMOVE_CHILDREN)) {
            if (evt.getNewValue() != null) {
                this.viewer.remove(evt.getNewValue());
            }
        }
    }
}