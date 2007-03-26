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
package org.springframework.ide.eclipse.webflow.core.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.webflow.core.model.IArgument;
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IMethodArguments;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElementVisitor;
import org.w3c.dom.NodeList;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class MethodArguments extends AbstractModelElement implements
		IMethodArguments {

	/**
	 * The arguments.
	 */
	private List<IArgument> arguments;

	/**
	 * Init.
	 * 
	 * @param node the node
	 * @param parent the parent
	 */
	@Override
	public void init(IDOMNode node, IWebflowModelElement parent) {
		super.init(node, parent);
		this.arguments = new ArrayList<IArgument>();

		NodeList children = node.getChildNodes();
		if (children != null && children.getLength() > 0) {
			for (int i = 0; i < children.getLength(); i++) {
				IDOMNode child = (IDOMNode) children.item(i);
				if ("argument".equals(child.getLocalName())) {
					IArgument arg = new Argument();
					arg.init(child, this);
					this.arguments.add(arg);
				}
			}
		}
	}

	/**
	 * Adds the argument.
	 * 
	 * @param arg the arg
	 */
	public void addArgument(IArgument arg) {
		if (!this.arguments.contains(arg)) {
			this.arguments.add(arg);
			WebflowModelXmlUtils.insertNode(arg.getNode(), getNode());
			super.firePropertyChange(ADD_CHILDREN, new Integer(this.arguments
					.indexOf(arg)), arg);
		}
	}

	/**
	 * Gets the arguments.
	 * 
	 * @return the arguments
	 */
	public List<IArgument> getArguments() {
		return this.arguments;
	}

	/**
	 * Removes the argument.
	 * 
	 * @param arg the arg
	 */
	public void removeArgument(IArgument arg) {
		if (this.arguments.contains(arg)) {
			this.arguments.remove(arg);
			getNode().removeChild(arg.getNode());
			super.fireStructureChange(REMOVE_CHILDREN, arg);
		}
	}

	/**
	 * Creates the new.
	 * 
	 * @param parent the parent
	 */
	public void createNew(IWebflowModelElement parent) {
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("method-arguments");
		init(node, parent);
	}

	/**
	 * Removes the all.
	 */
	public void removeAll() {
		for (IArgument action : this.arguments) {
			getNode().removeChild(action.getNode());
		}
		this.arguments = new ArrayList<IArgument>();
	}

	public void accept(IWebflowModelElementVisitor visitor,
			IProgressMonitor monitor) {
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {

			for (IAttribute state : getAttributes()) {
				if (monitor.isCanceled()) {
					return;
				}
				state.accept(visitor, monitor);
			}
			for (IArgument state : getArguments()) {
				if (monitor.isCanceled()) {
					return;
				}
				state.accept(visitor, monitor);
			}
		}
	}
}
