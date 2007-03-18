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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;
import org.springframework.ide.eclipse.webflow.core.model.IBeanAction;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IMethodArguments;
import org.springframework.ide.eclipse.webflow.core.model.IMethodResult;
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
public class BeanAction extends AbstractAction implements IBeanAction,
		ICloneableModelElement<BeanAction> {

	/**
	 * The method arguments.
	 */
	private IMethodArguments methodArguments;

	/**
	 * The method result.
	 */
	private IMethodResult methodResult;

	/**
	 * Init.
	 * 
	 * @param node the node
	 * @param parent the parent
	 */
	@Override
	public void init(IDOMNode node, IWebflowModelElement parent) {
		super.init(node, parent);

		NodeList children = node.getChildNodes();
		if (children != null && children.getLength() > 0) {
			for (int i = 0; i < children.getLength(); i++) {
				IDOMNode child = (IDOMNode) children.item(i);
				if ("method-arguments".equals(child.getLocalName())) {
					this.methodArguments = new MethodArguments();
					this.methodArguments.init(child, this);
				}
				else if ("method-result".equals(child.getLocalName())) {
					this.methodResult = new MethodResult();
					this.methodResult.init(child, this);
				}
			}
		}
	}

	/**
	 * Gets the method arguments.
	 * 
	 * @return the method arguments
	 */
	public IMethodArguments getMethodArguments() {
		return this.methodArguments;
	}

	/**
	 * Gets the method result.
	 * 
	 * @return the method result
	 */
	public IMethodResult getMethodResult() {
		return this.methodResult;
	}

	/**
	 * Sets the method arguments.
	 * 
	 * @param methodArguments the method arguments
	 */
	public void setMethodArguments(IMethodArguments methodArguments) {
		if (this.methodArguments != null) {
			getNode().removeChild(this.methodArguments.getNode());
		}
		this.methodArguments = methodArguments;
		if (methodArguments != null) {
			WebflowModelXmlUtils.insertNode(methodArguments.getNode(),
					getNode());
		}
		super.fireStructureChange(MOVE_CHILDREN, new Integer(1));
	}

	/**
	 * Sets the method result.
	 * 
	 * @param methodResult the method result
	 */
	public void setMethodResult(IMethodResult methodResult) {
		if (this.methodResult != null) {
			getNode().removeChild(this.methodResult.getNode());
		}
		this.methodResult = methodResult;
		if (methodResult != null) {
			WebflowModelXmlUtils.insertNode(methodResult.getNode(), getNode());
		}
		super.fireStructureChange(MOVE_CHILDREN, new Integer(1));
	}

	/**
	 * Clone model element.
	 * 
	 * @return the bean action
	 */
	public BeanAction cloneModelElement() {
		BeanAction state = new BeanAction();
		state.init((IDOMNode) this.node.cloneNode(true), parent);
		state.setType(getType());
		return state;
	}

	/**
	 * Apply clone values.
	 * 
	 * @param element the element
	 */
	public void applyCloneValues(BeanAction element) {
		if (element != null) {
			if (this.node.getParentNode() != null) {
				this.parent.getNode()
						.replaceChild(element.getNode(), this.node);
			}
			setType(element.getType());
			init(element.getNode(), parent);
			super.fireStructureChange(MOVE_CHILDREN, new Integer(0));
			super.firePropertyChange(PROPS);
		}
	}

	/**
	 * Creates the new.
	 * 
	 * @param parent the parent
	 */
	public void createNew(IWebflowModelElement parent) {
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("bean-action");
		init(node, parent);
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
			if (monitor.isCanceled()) {
				return;
			}
			if (getMethodArguments() != null) {
				getMethodArguments().accept(visitor, monitor);
			}
			if (monitor.isCanceled()) {
				return;
			}
			if (getMethodResult() != null) {
				getMethodResult().accept(visitor, monitor);
			}
		}
	}
}
