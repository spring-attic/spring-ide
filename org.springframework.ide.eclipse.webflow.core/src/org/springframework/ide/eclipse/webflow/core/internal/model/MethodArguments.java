/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.ide.eclipse.webflow.core.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.webflow.core.model.IArgument;
import org.springframework.ide.eclipse.webflow.core.model.IMethodArguments;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
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
			WebflowModelUtils.insertNode(arg.getNode(), getNode());
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
}
