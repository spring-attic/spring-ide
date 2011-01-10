/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
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
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.webflow.core.model.IGlobalTransitions;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.w3c.dom.NodeList;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class GlobalTransitions extends AbstractModelElement implements
		IGlobalTransitions {

	private List<IStateTransition> globalTransition = null;

	@Override
	public void init(IDOMNode node, IWebflowModelElement parent) {
		super.init(node, parent);

		this.globalTransition = new ArrayList<IStateTransition>();

		NodeList children = node.getChildNodes();
		if (children != null && children.getLength() > 0) {
			for (int i = 0; i < children.getLength(); i++) {
				IDOMNode child = (IDOMNode) children.item(i);
				if ("transition".equals(child.getLocalName())) {
					IStateTransition trans = new StateTransition();
					trans.init(child, this);
					this.globalTransition.add(trans);
				}
			}
		}
	}

	public void addGlobalTransition(IStateTransition action) {
		if (!this.globalTransition.contains(action)) {
			WebflowModelXmlUtils.insertNode(action.getNode(), node);
			this.globalTransition.add(action);
			super.firePropertyChange(ADD_CHILDREN, new Integer(
					this.globalTransition.indexOf(action)), action);
			parent.fireStructureChange(MOVE_CHILDREN, this);
		}
	}

	public void removeGlobalTransition(IStateTransition action) {
		if (this.globalTransition.contains(action)) {
			this.globalTransition.remove(action);
			getNode().removeChild(action.getNode());
			super.fireStructureChange(REMOVE_CHILDREN, action);
			parent.fireStructureChange(MOVE_CHILDREN, this);
		}
	}

	public void addGlobalTransition(IStateTransition action, int i) {
		if (!this.globalTransition.contains(action)) {
			if (this.globalTransition.size() > i) {
				IStateTransition ref = this.globalTransition.get(i);
				WebflowModelXmlUtils.insertBefore(action.getNode(), ref
						.getNode());
			}
			else {
				WebflowModelXmlUtils.insertNode(action.getNode(), node);
			}
			this.globalTransition.add(i, action);
			super.firePropertyChange(ADD_CHILDREN, new Integer(i), action);
			parent.fireStructureChange(MOVE_CHILDREN, this);
		}
	}

	public List<IStateTransition> getGlobalTransitions() {
		return this.globalTransition;
	}

	public void createNew(IWebflowModelElement parent) {
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("global-transitions");
		init(node, parent);
	}

	public void removeAll() {
		for (IStateTransition action : this.globalTransition) {
			getNode().removeChild(action.getNode());
		}
		this.globalTransition = new ArrayList<IStateTransition>();
	}

	public void accept(IModelElementVisitor visitor,
			IProgressMonitor monitor) {
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {

			for (IStateTransition state : getGlobalTransitions()) {
				if (monitor.isCanceled()) {
					return;
				}
				state.accept(visitor, monitor);
			}
		}
	}
	
	public IModelElement[] getElementChildren() {
		List<IModelElement> children = new ArrayList<IModelElement>();
		children.addAll(getGlobalTransitions());
		return children.toArray(new IModelElement[children.size()]);
	}
}
