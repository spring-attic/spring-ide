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

package org.springframework.ide.eclipse.beans.ui.graph.model;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.graph.Node;
import org.springframework.ide.eclipse.beans.ui.model.BeanNode;
import org.springframework.ide.eclipse.beans.ui.model.ConstructorArgumentNode;
import org.springframework.ide.eclipse.beans.ui.model.INode;
import org.springframework.ide.eclipse.beans.ui.model.PropertyNode;

public class Bean extends Node implements IAdaptable {

	public int preferredHeight;
	private BeanNode node;

	public Bean(BeanNode node) {
		super(node.getName());
		this.node = node;
	}

	public String getName() {
		return node.getName();
	}

	public String getClassName() {
		return node.getClassName();
	}

	public String getParentName() {
		return node.getParentName();
	}

	public IFile getConfigFile() {
		return node.getConfigNode().getConfigFile();
	}

	public int getStartLine() {
		return node.getStartLine();
	}

	public boolean hasConstructorArguments() {
		return node.hasConstructorArguments();
	}

	public ConstructorArgument[] getConstructorArguments() {
		ConstructorArgumentNode[] cargs = node.getConstructorArguments();
		ArrayList list = new ArrayList(cargs.length);
		for (int i = 0; i < cargs.length; i++) {
			list.add(new ConstructorArgument(this, cargs[i]));
		}
		return (ConstructorArgument[])
								list.toArray(new ConstructorArgument[list.size()]);
	}

	public boolean hasProperties() {
		return node.hasProperties();
	}

	public Property[] getProperties() {
		PropertyNode[] props = node.getProperties();
		ArrayList list = new ArrayList(props.length);
		for (int i = 0; i < props.length; i++) {
			list.add(new Property(this, props[i]));
		}
		return (Property[]) list.toArray(new Property[list.size()]);
	}

	public boolean isRootBean() {
		return node.isRootBean();
	}

	public boolean hasError() {
		return (node.getFlags() & INode.FLAG_HAS_ERRORS) != 0;
	}

	public Object getAdapter(Class adapter) {
		return (node != null ? node.getAdapter(adapter) : null);
	}

	public String toString() {
		return "Bean '" + getName() + "': x=" + x + ", y=" + y + ", width=" +
			   width + ", height=" + height;
	}
}
