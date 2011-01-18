/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
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
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.webflow.core.model.IIf;
import org.springframework.ide.eclipse.webflow.core.model.IIfTransition;
import org.springframework.ide.eclipse.webflow.core.model.ITransitionableTo;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;
import org.w3c.dom.Node;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class IfTransition extends Transition implements IIfTransition {

	/**
	 * The is then.
	 */
	private boolean isThen;

	/**
	 * The Constructor.
	 * @param webflowState the webflow state
	 * @param isThen the is then
	 */
	public IfTransition(IWebflowState webflowState, boolean isThen) {
		super(webflowState);
		this.isThen = isThen;
	}

	/**
	 * Checks if is then.
	 * @return true, if is then
	 */
	public boolean isThen() {
		return isThen;
	}

	/**
	 * Sets the then.
	 * @param isThen then
	 */
	public void setThen(boolean isThen) {
		boolean oldValue = this.isThen;
		this.isThen = isThen;
		super.firePropertyChange(PROPS, new Boolean(oldValue), new Boolean(
				isThen));
	}

	/**
	 * Gets the from if.
	 * @return the from if
	 */
	public IIf getFromIf() {
		return (IIf) this.parent;
	}

	/**
	 * Sets the from if.
	 * @param fromIf the from if
	 */
	public void setFromIf(IIf fromIf) {
		Node parent = this.node.getParentNode();
		if (parent != null) {
			parent.removeChild(this.node);
		}
		fromIf.getNode().appendChild(this.node);
		this.parent = fromIf;
	}

	/**
	 * Gets the to state id.
	 * @return the to state id
	 */
	@Override
	public String getToStateId() {
		if (isThen) {
			return getAttribute("then");
		}
		else {
			return getAttribute("else");
		}
	}

	/**
	 * Sets the to state.
	 * @param state the to state
	 */
	@Override
	public void setToState(ITransitionableTo state) {
		ITransitionableTo newTargetState = null;
		if (state != null) {
			newTargetState = (ITransitionableTo) WebflowModelXmlUtils
					.getStateById(webflowState, state.getId());
		}
		if (newTargetState != null && !newTargetState.equals(newTargetState)) {
			if (targetState != null) {
				targetState.removeInputTransition(this);
			}
			if (isThen) {
				setAttribute(this.parent.getNode(), "then", state.getId());
			}
			else {
				setAttribute(this.parent.getNode(), "else", state.getId());
			}
			super.fireStructureChange(OUTPUTS, state);
			targetState = newTargetState;
			if (targetState != null) {
				targetState.addInputTransition(this);
			}
		}
		else {
			if (state != null) {
				if (isThen) {
					setAttribute(this.parent.getNode(), "then", state.getId());
				}
				else {
					setAttribute(this.parent.getNode(), "else", state.getId());
				}
			}
			else {
				targetState = newTargetState;
			}
			super.fireStructureChange(OUTPUTS, state);
		}
	}

	public void accept(IModelElementVisitor visitor,
			IProgressMonitor monitor) {
		visitor.visit(this, monitor);
	}

	public void setToStateId(String id) {
		if (this.isThen) {
			setAttribute("then", id);
		}
		else {
			setAttribute("else", id);
		}
	}
	
	public IModelElement[] getElementChildren() {
		List<IModelElement> children = new ArrayList<IModelElement>();
		return children.toArray(new IModelElement[children.size()]);
	}
}
