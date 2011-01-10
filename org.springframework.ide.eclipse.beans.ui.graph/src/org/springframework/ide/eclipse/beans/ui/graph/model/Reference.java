/*******************************************************************************
 * Copyright (c) 2004, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.graph.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.draw2d.graph.Edge;
import org.eclipse.draw2d.graph.Node;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansConnection.BeanType;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;

/**
 * This edge connect two beans. Optionally a node (constructor argument or property) describing the
 * origin of this edge can be attached.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @see org.springframework.ide.eclipse.beans.ui.graph.model.Bean
 * @see org.eclipse.draw2d.graph.Node
 */
public class Reference extends Edge implements IAdaptable {

	private BeanType type;

	private Node node;
	
	private boolean isInner;
	
	private IResourceModelElement sourceLocation;

	public Reference(BeanType type, Bean source, Bean target, boolean isInner) {
		this(type, source, target, null, isInner);
	}

	public Reference(BeanType type, Bean source, Bean target, Node node, boolean isInner) {
		super(source, target);
		this.type = type;
		this.node = node;
		this.isInner = isInner;
	}

	public Reference(BeanType type, Bean source, Bean target, Node node, boolean isInner, IResourceModelElement sourceLocation) {
		super(source, target);
		this.type = type;
		this.node = node;
		this.isInner = isInner;
		this.sourceLocation = sourceLocation;
	}

	public BeanType getType() {
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
	
	public boolean isInner() {
		return isInner;
	}

	/**
	 * Returns the associated beans model element.
	 */
	public IResourceModelElement getResourceElement() {
		if (sourceLocation != null) {
			return sourceLocation;
		}
		else if (node instanceof Property) {
			return ((Property) node).getBean().getBean();
		}
		else if (node instanceof ConstructorArgument) {
			return ((ConstructorArgument) node).getBeanConstructorArgument();
		}
		return getSourceBean().getBean();
	}

	public int getStartLine() {
		if (node instanceof Property) {
			return ((Property) node).getBeanProperty().getElementStartLine();
		}
		else if (node instanceof ConstructorArgument) {
			return ((ConstructorArgument) node).getBeanConstructorArgument().getElementStartLine();
		}
		return getSourceBean().getStartLine();
	}

	public Object getAdapter(Class adapter) {
		if (node instanceof Property) {
			return ((Property) node).getAdapter(adapter);
		}
		else if (node instanceof ConstructorArgument) {
			return ((ConstructorArgument) node).getAdapter(adapter);
		}
//		else if (sourceLocation != null) {
//			return sourceLocation.getAdapter(adapter);
//		}
		return getSourceBean().getAdapter(adapter);
	}

	@Override
	public String toString() {
		return "Reference from '" + source + "' to '" + target + "'";
	}
}
