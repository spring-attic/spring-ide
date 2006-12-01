/*
 * Copyright 2002-2004 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.views;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.ui.views.model.BeanNode;
import org.springframework.ide.eclipse.beans.ui.views.model.ConfigNode;
import org.springframework.ide.eclipse.beans.ui.views.model.ConfigSetNode;
import org.springframework.ide.eclipse.beans.ui.views.model.ConstructorArgumentNode;
import org.springframework.ide.eclipse.beans.ui.views.model.INode;
import org.springframework.ide.eclipse.beans.ui.views.model.ProjectNode;
import org.springframework.ide.eclipse.beans.ui.views.model.PropertyNode;
import org.springframework.ide.eclipse.beans.ui.views.model.RootNode;
import org.springframework.ide.eclipse.core.model.IModelChangeListener;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent;

class BeansViewContentProvider implements ITreeContentProvider {

	protected static final Object[] NO_CHILDREN = new Object[0];

	private IModelChangeListener listener;
	private RootNode rootNode;

	public BeansViewContentProvider(final IBeansView view) {
		listener = new IModelChangeListener() {
			public void elementChanged(ModelChangeEvent event) {
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
		} else if (newInput instanceof RootNode) {
			rootNode = (RootNode) newInput;
			BeansCorePlugin.getModel().addChangeListener(listener);
		}
	}

	public void dispose() {
		rootNode = null;
		BeansCorePlugin.getModel().removeChangeListener(listener);
	}

	public Object[] getElements(Object parent) {
		return getChildren(parent);
	}

	public Object getParent(Object child) {
		return (child instanceof INode? ((INode) child).getParent() : null);
	}

	public Object[] getChildren(Object parent) {
		if (parent instanceof RootNode) {
			return ((RootNode) parent).getProjects();
		} else if (parent instanceof ProjectNode) {
			ProjectNode project = (ProjectNode) parent;
			Set<INode> nodes = new LinkedHashSet<INode>(); 
			nodes.addAll(project.getConfigs());
			nodes.addAll(project.getConfigSets());
			return nodes.toArray(new INode[nodes.size()]);
		} else if (parent instanceof ConfigSetNode) {
			return ((ConfigSetNode) parent).getBeans(true);
		} else if (parent instanceof ConfigNode) {
			return ((ConfigNode) parent).getBeans(true);
		} else if (parent instanceof BeanNode) {
			BeanNode bean = (BeanNode) parent;
			Set<INode> nodes = new LinkedHashSet<INode>(); 
			nodes.addAll(bean.getConstructorArguments());
			nodes.addAll(bean.getProperties());
			return nodes.toArray(new INode[nodes.size()]);
		}
		return null;
	}

	public boolean hasChildren(Object parent) {
		return !(parent instanceof PropertyNode ||
				parent instanceof ConstructorArgumentNode);
	}
}
