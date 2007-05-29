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
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IIf;
import org.springframework.ide.eclipse.webflow.core.model.IIfTransition;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;

/**
 * 
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class If extends AbstractModelElement implements IIf,
		ICloneableModelElement<IIf> {

	/**
	 * The else transition.
	 */
	private IIfTransition elseTransition;

	/**
	 * Then transition.
	 */
	private IIfTransition thenTransition;

	/**
	 * The webflow state.
	 */
	private IWebflowState webflowState;

	/**
	 * The Constructor.
	 * 
	 * @param webflowState the webflow state
	 */
	public If(IWebflowState webflowState) {
		this.webflowState = webflowState;
	}

	/**
	 * Init.
	 * 
	 * @param node the node
	 * @param parent the parent
	 */
	@Override
	public void init(IDOMNode node, IWebflowModelElement parent) {
		super.init(node, parent);

		String then = getAttribute("then");
		String els = getAttribute("else");

		if (then != null) {
			this.thenTransition = new IfTransition(webflowState, true);
			this.thenTransition.init(node, this);
		}
		if (els != null) {
			this.elseTransition = new IfTransition(webflowState, false);
			this.elseTransition.init(node, this);
		}
	}

	/**
	 * Gets the test.
	 * 
	 * @return the test
	 */
	public String getTest() {
		return getAttribute("test");
	}

	/**
	 * Sets the else transition.
	 * 
	 * @param theElse the else transition
	 */
	public void setElseTransition(IIfTransition theElse) {
		IIfTransition oldValue = this.elseTransition;
		this.elseTransition = theElse;
		this.elseTransition.setThen(false);
		super.firePropertyChange(OUTPUTS, oldValue, theElse);
	}

	/**
	 * Sets the test.
	 * 
	 * @param test the test
	 */
	public void setTest(String test) {
		setAttribute("test", test);
	}

	/**
	 * Sets the then transition.
	 * 
	 * @param then then transition
	 */
	public void setThenTransition(IIfTransition then) {
		IIfTransition oldValue = this.thenTransition;
		this.thenTransition = then;
		this.thenTransition.setThen(true);
		super.firePropertyChange(OUTPUTS, oldValue, then);
	}

	/**
	 * Gets the else transition.
	 * 
	 * @return the else transition
	 */
	public IIfTransition getElseTransition() {
		return this.elseTransition;
	}

	/**
	 * Gets the then transition.
	 * 
	 * @return the then transition
	 */
	public IIfTransition getThenTransition() {
		return this.thenTransition;
	}

	/**
	 * Removes the else transition.
	 */
	public void removeElseTransition() {
		IIfTransition oldValue = this.elseTransition;
		this.elseTransition = null;
		super.firePropertyChange(OUTPUTS, oldValue, null);
	}

	/**
	 * Removes the then transition.
	 */
	public void removeThenTransition() {
		IIfTransition oldValue = this.thenTransition;
		this.thenTransition = null;
		super.firePropertyChange(OUTPUTS, oldValue, null);
	}

	/**
	 * Clone model element.
	 * 
	 * @return the i if
	 */
	public IIf cloneModelElement() {
		If state = new If(webflowState);
		state.init((IDOMNode) this.node.cloneNode(true), parent);
		return state;
	}

	/**
	 * Apply clone values.
	 * 
	 * @param element the element
	 */
	public void applyCloneValues(IIf element) {
		if (element != null) {
			if (this.node.getParentNode() != null) {
				this.parent.getNode()
						.replaceChild(element.getNode(), this.node);
			}
			init(element.getNode(), parent);
			setTest(element.getTest());
			super.fireStructureChange(MOVE_CHILDREN, new Integer(1));
		}
	}

	public void accept(IModelElementVisitor visitor,
			IProgressMonitor monitor) {
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {
			if (getThenTransition() != null) {
				getThenTransition().accept(visitor, monitor);
			}
			if (getElseTransition() != null) {
				getElseTransition().accept(visitor, monitor);
			}
		}
	}
	
	public IModelElement[] getElementChildren() {
		List<IModelElement> children = new ArrayList<IModelElement>();
		children.add(getThenTransition());
		children.add(getElseTransition());
		return children.toArray(new IModelElement[children.size()]);
	}
}
