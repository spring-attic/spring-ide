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

import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
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
public class If extends WebflowModelElement implements IIf,
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

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.flow.core.model.IIf#getTest()
	 */
	/**
	 * Gets the test.
	 * 
	 * @return the test
	 */
	public String getTest() {
		return getAttribute("test");
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.flow.core.model.IIf#setElse(org.springframework.ide.eclipse.web.flow.core.model.ITransitionableTo)
	 */
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

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.flow.core.model.IIf#setTest(java.lang.String)
	 */
	/**
	 * Sets the test.
	 * 
	 * @param test the test
	 */
	public void setTest(String test) {
		setAttribute("test", test);
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.flow.core.model.IIf#setThen(org.springframework.ide.eclipse.web.flow.core.model.ITransitionableTo)
	 */
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

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.flow.core.model.IIf#getElseTransition()
	 */
	/**
	 * Gets the else transition.
	 * 
	 * @return the else transition
	 */
	public IIfTransition getElseTransition() {
		return this.elseTransition;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.flow.core.model.IIf#getThenTransition()
	 */
	/**
	 * Gets the then transition.
	 * 
	 * @return the then transition
	 */
	public IIfTransition getThenTransition() {
		return this.thenTransition;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.web.flow.core.model.IIf#removeElseTransition()
	 */
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
}