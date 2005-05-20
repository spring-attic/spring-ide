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

package org.springframework.ide.eclipse.web.flow.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.springframework.ide.eclipse.web.flow.core.WebFlowCorePlugin;
import org.springframework.ide.eclipse.web.flow.core.model.IWebFlowModelChangedListener;
import org.springframework.ide.eclipse.web.flow.core.model.WebFlowModelChangedEvent;
import org.springframework.ide.eclipse.web.flow.ui.model.ConfigNode;
import org.springframework.ide.eclipse.web.flow.ui.model.ConfigSetNode;
import org.springframework.ide.eclipse.web.flow.ui.model.INode;
import org.springframework.ide.eclipse.web.flow.ui.model.ProjectNode;
import org.springframework.ide.eclipse.web.flow.ui.model.RootNode;

class WebFlowViewContentProvider implements ITreeContentProvider {

    protected static final Object[] NO_CHILDREN = new Object[0];

    private IWebFlowView view;

    private IWebFlowModelChangedListener listener;

    private RootNode rootNode;

    public WebFlowViewContentProvider(final IWebFlowView view) {
        this.view = view;
        listener = new IWebFlowModelChangedListener() {
            public void elementChanged(WebFlowModelChangedEvent event) {
                if (rootNode != null) {
                    rootNode.reloadConfigs();
                    view.refresh();
                }
            }
        };
        rootNode = null;
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (newInput == null) {
            dispose();
        }
        else if (newInput instanceof RootNode) {
            rootNode = (RootNode) newInput;
            WebFlowCorePlugin.getModel().addChangeListener(listener);
        }
    }

    public void dispose() {
        rootNode = null;
        WebFlowCorePlugin.getModel().removeChangeListener(listener);
    }

    public Object[] getElements(Object parent) {
        return getChildren(parent);
    }

    public Object getParent(Object child) {
        return (child instanceof INode ? ((INode) child).getParent() : null);
    }

    public Object[] getChildren(Object parent) {
        if (parent instanceof RootNode) {
            return ((RootNode) parent).getProjects();
        }
        else if (parent instanceof ProjectNode) {
            ProjectNode project = (ProjectNode) parent;
            List nodes = new ArrayList(); // project.getConfigs();
            nodes.addAll(project.getConfigSets());
            return (INode[]) nodes.toArray(new INode[nodes.size()]);
        }
        else if (parent instanceof ConfigSetNode) {
            List children = ((ConfigSetNode) parent).getConfigs();
            children.add(((ConfigSetNode) parent).getBeansConfigSet());
            return children.toArray();
        }
        else if (parent instanceof ConfigNode) {
            return ((ConfigNode) parent).getState().getStates().toArray();
        } /*else if (parent instanceof StateNode) {
         StateNode bean = (StateNode) parent;
         List nodes = new ArrayList(Arrays.asList(
         bean.getConstructorArguments()));
         nodes.addAll(Arrays.asList(bean.getProperties()));
         return (INode[]) nodes.toArray(new INode[nodes.size()]);
         }*/
        return null;
    }

    public boolean hasChildren(Object parent) {
        return (parent instanceof RootNode || parent instanceof ProjectNode || parent instanceof ConfigSetNode);
    }
}
