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

package org.springframework.ide.eclipse.web.flow.ui.editor.dialogs;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.springframework.ide.eclipse.web.flow.core.model.IPropertyEnabled;
import org.springframework.ide.eclipse.web.flow.core.model.IState;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelElement;

class PropertiesContentProvider implements IStructuredContentProvider,
        PropertyChangeListener {

    private IWebFlowModelElement project;

    private TableViewer viewer;

    public PropertiesContentProvider(IPropertyEnabled project, TableViewer viewer) {
        this.project = project;
        this.viewer = viewer;
    }

    public PropertiesContentProvider(IState project, TableViewer viewer) {
        this.project = project;
        this.viewer = viewer;
    }

    public void dispose() {
        ((IWebFlowModelElement) project).removePropertyChangeListener(this);
    }

    public Object[] getElements(Object obj) {
        if (project instanceof IPropertyEnabled) {
            return ((IPropertyEnabled) project).getProperties().toArray();
        }
        else 
            return null;
    }

    public void inputChanged(Viewer v, Object oldInput, Object newInput) {
        if (newInput != null)
            ((IWebFlowModelElement) newInput).addPropertyChangeListener(this);
        if (oldInput != null)
            ((IWebFlowModelElement) oldInput)
                    .removePropertyChangeListener(this);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(IWebFlowModelElement.ADD_CHILDREN)) {
            if (evt.getNewValue() != null) {
                this.viewer.add(evt.getNewValue());
            }
        }
        else if (evt.getPropertyName().equals(
                IWebFlowModelElement.REMOVE_CHILDREN)) {
            if (evt.getNewValue() != null) {
                this.viewer.remove(evt.getNewValue());
            }
        }
    }
}