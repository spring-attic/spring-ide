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

package org.springframework.ide.eclipse.beans.ui.graph.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.springframework.ide.eclipse.beans.ui.graph.BeansGraphImages;
import org.springframework.ide.eclipse.beans.ui.graph.BeansGraphPlugin;
import org.springframework.ide.eclipse.beans.ui.graph.model.Bean;
import org.springframework.ide.eclipse.beans.ui.model.BeanNode;
import org.springframework.ide.eclipse.beans.ui.model.ConfigNode;
import org.springframework.ide.eclipse.beans.ui.model.ConfigSetNode;
import org.springframework.ide.eclipse.beans.ui.model.INode;
import org.springframework.ide.eclipse.beans.ui.model.ProjectNode;

public class GraphEditorInput implements IEditorInput {

	private INode node;
	private String name;
	private String toolTip;
	private Map beans;

	public GraphEditorInput(INode node) {
		this.node = node;

		// Prepare name and tooltip for corresponding node type
		if (node instanceof ConfigNode) {
			IFile file = ((ConfigNode) node).getConfigFile();
			name = file.getName();
			toolTip = BeansGraphPlugin.getResourceString(
				 "ShowGraphAction.name.config") + file.getFullPath().toString();
		} else if (node instanceof ConfigSetNode) {
			ProjectNode project = ((ConfigSetNode) node).getProjectNode(); 
			name = node.getName();
			toolTip = BeansGraphPlugin.getResourceString(
						 "ShowGraphAction.name.configSet") + project.getName() +
						 '/' + node.getName();
		} else {
			name = node.getName();
			StringBuffer buffer = new StringBuffer();
			buffer.append(BeansGraphPlugin.getResourceString(
												  "ShowGraphAction.name.bean"));
			if (node.getParent() instanceof ConfigNode) {
				ConfigNode config = (ConfigNode) node.getParent();
				buffer.append(BeansGraphPlugin.getResourceString(
												"ShowGraphAction.name.config"));
				buffer.append(config.getName());
				buffer.append(": ");
			} else if (node.getParent() instanceof ConfigSetNode) {
				ConfigSetNode configSet = (ConfigSetNode) node.getParent();
				buffer.append(BeansGraphPlugin.getResourceString(
											 "ShowGraphAction.name.configSet"));
				buffer.append(configSet.getProjectNode().getName());
				buffer.append('/');
				buffer.append(configSet.getName());
				buffer.append(": ");
			}
			buffer.append(node.getName());
			toolTip = buffer.toString();
		}

		createBeansMap(node);
	}

	/**
	 * Creates a list with all beans belonging to the graph's config / config
	 * set or being referenced from the graph's node.
	 */
	protected void createBeansMap(INode node) {
		List list = new ArrayList();
		if (node instanceof ConfigNode) {
			BeanNode[] nodes = ((ConfigNode) node).getBeans(false);
			for (int i = 0; i < nodes.length; i++) {
				list.add(nodes[i]);
			}
		} else if (node instanceof ConfigSetNode) {
			BeanNode[] nodes = ((ConfigSetNode) node).getBeans(false);
			for (int i = 0; i < nodes.length; i++) {
				list.add(nodes[i]);
			}
		} else if (node instanceof BeanNode) {
			BeanNode bean = (BeanNode) node;
			list.add(bean);
			list.addAll(bean.getReferencedBeans());
		}

		// Wrap all beans found
		this.beans = new HashMap();
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			BeanNode bean = (BeanNode) iter.next();

			// Skip dummy beans node which are holding an error message only
			if (bean.getBean() != null) {
				this.beans.put(bean.getName(), new Bean(bean.getBean()));
			}
		}
	}

	public String getName() {
		return name;
	}

	public Map getBeans() {
		return beans;
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		return BeansGraphImages.DESC_OBJS_SPRING;
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return toolTip;
	}

	public Object getAdapter(Class adapter) {
		return node.getAdapter(adapter);
	}
}
