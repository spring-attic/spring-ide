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

package org.springframework.ide.eclipse.beans.ui.views.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.ui.views.properties.IPropertySource;
import org.springframework.ide.eclipse.beans.ui.BeansUIUtils;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.ISourceModelElement;

public abstract class AbstractNode implements INode {

	private INode parent;
	private String name;
	private IModelElement element;
	private int flags;
	private int startLine;

	/**
	 * Creates a new node with the given name
	 * 
	 * @param name the new node's name
	 */
	public AbstractNode(String name) {
		this(null, name);
	}

	/**
	 * Creates a new node with the given parent and the given name
	 * 
	 * @param parent the new node's parent node
	 * @param name the new node's name
	 */
	public AbstractNode(INode parent, String name) {
		this.parent = parent;
		this.name = name;

		flags = 0;
		startLine = -1;
	}

	/**
	 * Returns this node's parent or <code>null</code> if none.
	 * 
	 * @return this node's parent node
	 */
	public INode getParent() {
		return parent;
	}

	/**
	 * Sets this node's parent node to the given node
	 * 
	 * @param parent the parent node
	 */
	public final void setParent(INode parent) {
		this.parent = parent;
	}

	public final INode[] getChildren() {
		if (this instanceof RootNode) {
			return ((RootNode) this).getProjects();
		} else if (this instanceof ProjectNode) {
			ProjectNode project = (ProjectNode) this;
			List nodes = project.getConfigs();
			nodes.addAll(project.getConfigSets());
			return (INode[]) nodes.toArray(new INode[nodes.size()]);
		} else if (this instanceof ConfigSetNode) {
			return ((ConfigSetNode) this).getBeans(true);
		} else if (this instanceof ConfigNode) {
			return ((ConfigNode) this).getBeans(true);
		} else if (this instanceof BeanNode) {
			BeanNode bean = (BeanNode) this;
			List nodes = new ArrayList(Arrays.asList(
											   bean.getConstructorArguments()));
			nodes.addAll(Arrays.asList(bean.getProperties()));
			return (INode[]) nodes.toArray(new INode[nodes.size()]);
		}
		return NO_CHILDREN;
	}

	public final boolean hasChildren() {
		return !(this instanceof PropertyNode ||
				this instanceof ConstructorArgumentNode);
	}

	public final String getID() {
		StringBuffer id = new StringBuffer();
		if (getParent() != null) {
			id.append(getParent().getID());
			id.append(IModelElement.ID_DELIMITER);
		}
		if (getElement() != null) {
			id.append(getElement().getElementType());
		}
		id.append(IModelElement.ID_SEPARATOR);
		if (getName() != null) {
			id.append(getName());
		} else {
			id.append(this.hashCode());
		}
		return id.toString();
	}

	/**
	 * Sets this node's name to the given name
	 * 
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/**
	 * Sets this node's model element and start line (retrieved from model
	 * element).
	 * 
	 * @param element  the model element associated with this node
	 */
	public final void setElement(IModelElement element) {
		this.element = element;
		this.startLine = (element instanceof ISourceModelElement ?
				   ((ISourceModelElement) element).getElementStartLine() : -1);
	}

	public final IModelElement getElement() {
		return element;
	}

	public final void setFlags(int flags) {
		this.flags |= flags;

		// Propagate modification of flags to parent
		if (parent != null && parent instanceof AbstractNode) {
			((AbstractNode) parent).setFlags(flags);
		}
	}

	public final void clearFlags(int flags) {
		this.flags &= ~flags;

		// Propagate modification of flags to parent
		if (parent != null && parent instanceof AbstractNode) {
			((AbstractNode) parent).clearFlags(flags);
		}
	}

	public final int getFlags() {
		return flags;
	}

	/**
	 * Sets this node's start line.
	 * 
	 * @param startLine  the start line of this node
	 */
	public final void setStartLine(int startLine) {
		this.startLine = startLine;
	}

	public final int getStartLine() {
		return startLine;
	}

	public final INode getNode(String id) {
		IModelElement element = getElement();
		int sepPos = id.indexOf(IModelElement.ID_SEPARATOR);
		if (element != null && sepPos > 0) {
			try {
				int type = Integer.valueOf(id.substring(0, sepPos)).intValue();
				if (type == element.getElementType()) {
					int delPos = id.indexOf(IModelElement.ID_DELIMITER);
					if (delPos > 0) {
						String name = id.substring(sepPos + 1, delPos);
						if (name.equals(element.getElementName())) {

							// Ask all children for the remaining part of the
							// element ID
							id = id.substring(delPos + 1);
							INode[] children = getChildren();
							for (int i = 0; i < children.length; i++) {
								INode child = children[i];
								if (child instanceof AbstractNode) {
									INode node = ((AbstractNode)
															child).getNode(id);
									if (node != null) {
										return node;
									}
								}
							}
						}
					} else {
						String name = id.substring(sepPos + 1);
						if (name.equals(element.getElementName())) {
							return this;
						}
					}
				}
			} catch (NumberFormatException e) {
				// ignore
			}
		}
		return null;
	}

	public void propertyChanged(INode node, int propertyId) {
		AbstractNode parent = (AbstractNode) getParent();
		if (parent != null) {
			parent.propertyChanged(node, propertyId);
		}
	}

	public void refreshViewer() {
		if (parent != null && parent instanceof AbstractNode) {
			((AbstractNode) parent).refreshViewer();
		}
	}

	/**
	 * Returns an adapter for <code>IPropertySource</code> and
	 * <code>IModelElement</code>.
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			return BeansUIUtils.getPropertySource(element);
		} else if (adapter == IModelElement.class) {
			return element;
		}
		return null;
	}

	public String toString() {
		return name;
	}
}
