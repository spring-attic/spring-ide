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
public class VarContentProvider implements IStructuredContentProvider,
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
    public VarContentProvider(IWebflowState project, TableViewer viewer) {
        this.project = project;
        this.viewer = viewer;
    }

    /**
     * 
     * 
     * @param viewer 
     * @param project 
     */
    public VarContentProvider(IState project, TableViewer viewer) {
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
            return ((IWebflowState) project).getVars().toArray();
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
