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

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.springframework.ide.eclipse.beans.ui.graph.BeansGraphImages;
import org.springframework.ide.eclipse.beans.ui.graph.BeansGraphPlugin;
import org.springframework.ide.eclipse.beans.ui.model.BeanNode;
import org.springframework.ide.eclipse.beans.ui.model.ConfigNode;
import org.springframework.ide.eclipse.beans.ui.model.ConfigSetNode;
import org.springframework.ide.eclipse.beans.ui.model.INode;

public class GraphEditorInput implements IEditorInput {

	private INode node;
	private String name;

	public GraphEditorInput(INode node) {
		this.node = node;
		if (node instanceof ConfigNode) {
			name = BeansGraphPlugin.getResourceString(
								"ShowGraphAction.name.config") + node.getName();
		} else if (node instanceof ConfigSetNode) {
			name = BeansGraphPlugin.getResourceString(
							 "ShowGraphAction.name.configSet") + node.getName();
		} else {
			name = BeansGraphPlugin.getResourceString(
								  "ShowGraphAction.name.bean") + node.getName();
		}
	}

	public String getName() {
		return name;
	}

	public INode getNode() {
		return node;
	}

	public boolean exists() {
		return false;
	}

	public ImageDescriptor getImageDescriptor() {
		if (node instanceof ConfigNode) {
			return BeansGraphImages.DESC_OBJS_CONFIG;
		} else if (node instanceof ConfigSetNode) {
			return BeansGraphImages.DESC_OBJS_CONFIG_SET;
		} else {
			if (((BeanNode) node).isRootBean()) {
				return BeansGraphImages.DESC_OBJS_ROOT_BEAN;
			} else {
				return BeansGraphImages.DESC_OBJS_CHILD_BEAN;
			}
		}
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return name;
	}

	public Object getAdapter(Class adapter) {
		return node.getAdapter(adapter);
	}
}
