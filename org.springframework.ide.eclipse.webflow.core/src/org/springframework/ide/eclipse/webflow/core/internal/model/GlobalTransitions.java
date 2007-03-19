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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.webflow.core.model.IGlobalTransitions;
import org.springframework.ide.eclipse.webflow.core.model.IStateTransition;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElementVisitor;
import org.w3c.dom.NodeList;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class GlobalTransitions extends WebflowModelElement implements
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

	public void accept(IWebflowModelElementVisitor visitor,
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
}
