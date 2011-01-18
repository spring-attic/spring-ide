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
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IModelElementVisitor;
import org.springframework.ide.eclipse.webflow.core.model.IAttribute;
import org.springframework.ide.eclipse.webflow.core.model.ICloneableModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IEvaluateAction;
import org.springframework.ide.eclipse.webflow.core.model.IEvaluationResult;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.w3c.dom.NodeList;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class EvaluateAction extends AbstractActionElement implements IEvaluateAction,
		ICloneableModelElement<EvaluateAction> {

	/**
	 * 
	 */
	private ACTION_TYPE type;

	/**
	 * 
	 * 
	 * @param type
	 */
	public void setType(ACTION_TYPE type) {
		this.type = type;
	}

	/**
	 * 
	 * 
	 * @return
	 */
	public ACTION_TYPE getType() {
		return this.type;
	}

	/**
	 * The evaluation result.
	 */
	private IEvaluationResult evaluationResult = null;

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
				if ("evaluation-result".equals(child.getLocalName())) {
					this.evaluationResult = new EvaluationResult();
					((EvaluationResult) this.evaluationResult).init(child, this);
				}
			}
		}
	}

	/**
	 * Gets the evaluation result.
	 * 
	 * @return the evaluation result
	 */
	public IEvaluationResult getEvaluationResult() {
		return this.evaluationResult;
	}

	/**
	 * Gets the expression.
	 * 
	 * @return the expression
	 */
	public String getExpression() {
		return getAttribute("expression");
	}

	/**
	 * Sets the evaluation result.
	 * 
	 * @param evaluationResult the evaluation result
	 */
	public void setEvaluationResult(IEvaluationResult evaluationResult) {
		if (this.evaluationResult != null) {
			getNode().removeChild(this.evaluationResult.getNode());
		}
		this.evaluationResult = evaluationResult;
		if (evaluationResult != null) {
			WebflowModelXmlUtils.insertNode(evaluationResult.getNode(), getNode());
		}
		super.fireStructureChange(MOVE_CHILDREN, new Integer(1));
	}

	/**
	 * Sets the expression.
	 * 
	 * @param expression the expression
	 */
	public void setExpression(String expression) {
		setAttribute("expression", expression);
	}

	/**
	 * Gets the name.
	 * 
	 * @return the name
	 */
	public String getName() {
		return getAttribute("name");
	}

	/**
	 * Sets the name.
	 * 
	 * @param name the name
	 */
	public void setName(String name) {
		setAttribute("name", name);
	}

	/**
	 * Clone model element.
	 * 
	 * @return the evaluate action
	 */
	public EvaluateAction cloneModelElement() {
		EvaluateAction state = new EvaluateAction();
		state.init((IDOMNode) this.node.cloneNode(true), parent);
		state.setType(getType());
		return state;
	}

	/**
	 * Apply clone values.
	 * 
	 * @param element the element
	 */
	public void applyCloneValues(EvaluateAction element) {
		if (element != null) {
			if (this.node.getParentNode() != null) {
				this.parent.getNode().replaceChild(element.getNode(), this.node);
			}
			setType(element.getType());
			init(element.getNode(), parent);
			super.fireStructureChange(MOVE_CHILDREN, new Integer(0));
			super.firePropertyChange(PROPS);
		}
	}

	/**
	 * Gets the scope.
	 * 
	 * @return the scope
	 */
	public String getScope() {
		return getAttribute("scope");
	}

	/**
	 * Sets the scope.
	 * 
	 * @param scope the scope
	 */
	public void setScope(String scope) {
		setAttribute("scope", scope);
	}

	/**
	 * Creates the new.
	 * 
	 * @param parent the parent
	 */
	public void createNew(IWebflowModelElement parent) {
		IDOMNode node = null;
		if (WebflowModelXmlUtils.isVersion1Flow(this)) {
			node = (IDOMNode) parent.getNode().getOwnerDocument().createElement("evaluate-action");
		}
		else {
			node = (IDOMNode) parent.getNode().getOwnerDocument().createElement("evaluate");
		}
		init(node, parent);
	}

	public void accept(IModelElementVisitor visitor, IProgressMonitor monitor) {
		if (!monitor.isCanceled() && visitor.visit(this, monitor)) {

			for (IAttribute state : getAttributes()) {
				if (monitor.isCanceled()) {
					return;
				}
				state.accept(visitor, monitor);
			}
			if (getEvaluationResult() != null) {
				getEvaluationResult().accept(visitor, monitor);
			}
		}
	}

	public IModelElement[] getElementChildren() {
		List<IModelElement> children = new ArrayList<IModelElement>();
		children.addAll(getAttributes());
		children.add(getEvaluationResult());
		return children.toArray(new IModelElement[children.size()]);
	}

	public String getResult() {
		return getAttribute("result");
	}

	public String getResultType() {
		return getAttribute("result-type");
	}

	public void setResult(String result) {
		setAttribute("result", result);
	}

	public void setResultType(String resultType) {
		setAttribute("result-type", resultType);
	}
}
