/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.graph.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.Node;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.beans.core.model.IBean;

/**
 * This edge connect two beans. Optionally a node (constructor argument or
 * property) describing the origin of this edge can be attached.
 * @see org.springframework.ide.eclipse.beans.ui.graph.model.Bean
 * @see org.eclipse.draw2d.graph.Node 
 * @author Torsten Juergeleit
 */
public class Reference extends Edge implements IAdaptable {

	private int type;
	private Node node;

	public Reference(int type, Bean source, Bean target) {
		this(type, source, target, null);
	}

	public Reference(int type, Bean source, Bean target, Node node) {
		super(source, target);
		this.type = type;
		this.node = node;
	}

	public int getType() {
		return type;
	}

	public Bean getSourceBean() {
		return (Bean) super.source;
	}

	public Bean getTargetBean() {
		return (Bean) super.target;
	}

	public Node getNode() {
		return node;
	}

	public IFile getConfigFile() {
		if (node instanceof Property) {
			IBean bean = ((Property) node).getBean().getBean();
			return BeansModelUtils.getConfig(bean).getConfigFile();
		} else if (node instanceof ConstructorArgument) {
			IBean bean = ((ConstructorArgument) node).getBean().getBean();
			return BeansModelUtils.getConfig(bean).getConfigFile();
		}
		return getSourceBean().getConfigFile();
	}

	public int getStartLine() {
		if (node instanceof Property) {
			return ((Property) node).getBeanProperty().getElementStartLine();
		} else if (node instanceof ConstructorArgument) {
			return ((ConstructorArgument)
					   node).getBeanConstructorArgument().getElementStartLine();
		}
		return getSourceBean().getStartLine();
	}

	public Object getAdapter(Class adapter) {
		if (node instanceof Property) {
			return ((Property) node).getAdapter(adapter);
		} else if (node instanceof ConstructorArgument) {
			return ((ConstructorArgument) node).getAdapter(adapter);
		}
		return getSourceBean().getAdapter(adapter);
	}

	public String toString() {
		return "Reference from '" + ((Bean) source).getName() + "' to '" +
											   ((Bean) target).getName() + "'";
	}
}
