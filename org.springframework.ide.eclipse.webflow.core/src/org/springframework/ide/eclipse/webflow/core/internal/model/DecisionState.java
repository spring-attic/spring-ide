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
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IDecisionState;
import org.springframework.ide.eclipse.webflow.core.model.IExceptionHandler;
import org.springframework.ide.eclipse.webflow.core.model.IIf;
import org.springframework.ide.eclipse.webflow.core.model.ITransition;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.w3c.dom.NodeList;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class DecisionState extends AbstractTransitionableFrom implements
		IDecisionState, ICloneableModelElement<IDecisionState> {

	/**
	 * The ifs.
	 */
	private List<IIf> ifs = null;

	/**
	 * 
	 * 
	 * @param node
	 * @param parent
	 */
	@Override
	public void init(IDOMNode node, IWebflowModelElement parent) {
		init(node, parent, true);
	}

	/**
	 * Init.
	 * 
	 * @param initIfs the init ifs
	 * @param node the node
	 * @param parent the parent
	 */
	public void init(IDOMNode node, IWebflowModelElement parent, boolean initIfs) {
		super.init(node, parent);

		if (initIfs) {
			this.ifs = new ArrayList<IIf>();
			initIfs(node, parent);
		}
	}

	/**
	 * Inits the ifs.
	 * 
	 * @param node the node
	 * @param parent the parent
	 */
	private void initIfs(IDOMNode node, IWebflowModelElement parent) {
		NodeList children = node.getChildNodes();
		if (children != null && children.getLength() > 0) {
			for (int i = 0; i < children.getLength(); i++) {
				IDOMNode child = (IDOMNode) children.item(i);
				if ("if".equals(child.getLocalName())) {
					If if_ = new If((IWebflowState) parent);
					if_.init(child, this);
					ifs.add(if_);
				}
			}
		}
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public List<IIf> getIfs() {
		return this.ifs;
	}

	/**
	 * 
	 * 
	 * @param theIf
	 */
	public void addIf(IIf theIf) {
		if (!this.ifs.contains(theIf)) {
			this.ifs.add(theIf);
			super.firePropertyChange(ADD_CHILDREN, new Integer(this.ifs
					.indexOf(theIf)), theIf);
		}
	}

	/**
	 * 
	 * 
	 * @param i
	 * @param theIf
	 */
	public void addIf(IIf theIf, int i) {
		if (!this.ifs.contains(theIf)) {
			this.ifs.add(i, theIf);
			super.firePropertyChange(ADD_CHILDREN, new Integer(i), theIf);
		}
	}

	/**
	 * 
	 * 
	 * @param theIf
	 */
	public void removeIf(IIf theIf) {
		if (this.ifs.contains(theIf)) {
			this.ifs.remove(theIf);
			getNode().removeChild(theIf.getNode());
			super.fireStructureChange(REMOVE_CHILDREN, theIf);
		}
	}

	/**
	 * 
	 * 
	 * @param parent
	 */
	public void createNew(IWebflowState parent) {
		IDOMNode node = (IDOMNode) parent.getNode().getOwnerDocument()
				.createElement("decision-state");
		init(node, parent);
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public IDecisionState cloneModelElement() {
		DecisionState state = new DecisionState();
		state.init((IDOMNode) this.node.cloneNode(true), parent);
		return state;
	}

	/**
	 * 
	 * 
	 * @param element
	 */
	public void applyCloneValues(IDecisionState element) {
		if (element != null) {
			if (this.node.getParentNode() != null) {
				this.parent.getNode()
						.replaceChild(element.getNode(), this.node);
			}

			setId(element.getId());
			init(element.getNode(), parent, false);

			// if (this.node.getParentNode() != null) {
			if (this.ifs != null && this.ifs.size() > 0) {
				for (int i = 0; i < this.ifs.size(); i++) {
					this.ifs.get(i).setTest(element.getIfs().get(i).getTest());
				}
			}
			// }

			super.fireStructureChange(MOVE_CHILDREN, new Integer(1));
		}
	}

	public void accept(IModelElementVisitor visitor,
			IProgressMonitor monitor) {
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {

			for (IAttribute state : getAttributes()) {
				if (monitor.isCanceled()) {
					return;
				}
				state.accept(visitor, monitor);
			}
			if (getEntryActions() != null) {
				getEntryActions().accept(visitor, monitor);
			}
			if (monitor.isCanceled()) {
				return;
			}
			for (IIf state : getIfs()) {
				if (monitor.isCanceled()) {
					return;
				}
				state.accept(visitor, monitor);
			}
			if (monitor.isCanceled()) {
				return;
			}
			if (getExitActions() != null) {
				getExitActions().accept(visitor, monitor);
			}
			for (IExceptionHandler state : getExceptionHandlers()) {
				if (monitor.isCanceled()) {
					return;
				}
				state.accept(visitor, monitor);
			}
			for (ITransition state : getOutputTransitions()) {
				if (monitor.isCanceled()) {
					return;
				}
				state.accept(visitor, monitor);
			}
		}
	}
	
	public IModelElement[] getElementChildren() {
		List<IModelElement> children = new ArrayList<IModelElement>();
		children.addAll(getAttributes());
		children.add(getEntryActions());
		children.addAll(getIfs());
		children.add(getExitActions());
		children.addAll(getExceptionHandlers());
		children.addAll(getOutputTransitions());
		return children.toArray(new IModelElement[children.size()]);
	}
}
