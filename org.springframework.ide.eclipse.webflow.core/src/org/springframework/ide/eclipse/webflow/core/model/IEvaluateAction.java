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
package org.springframework.ide.eclipse.webflow.core.model;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public interface IEvaluateAction extends IAction {

	/**
	 * Sets the expression.
	 * 
	 * @param expression the expression
	 */
	void setExpression(String expression);

	/**
	 * Gets the expression.
	 * 
	 * @return the expression
	 */
	String getExpression();

	/**
	 * Sets the scope.
	 * 
	 * @param scope the scope
	 */
	void setScope(String scope);

	/**
	 * Gets the scope.
	 * 
	 * @return the scope
	 */
	String getScope();

	/**
	 * Sets the evaluation result.
	 * 
	 * @param evaluationResult the evaluation result
	 */
	void setEvaluationResult(IEvaluationResult evaluationResult);

	/**
	 * Gets the evaluation result.
	 * 
	 * @return the evaluation result
	 */
	IEvaluationResult getEvaluationResult();
	
	void setResult(String result);
	
	String getResult();
	
	void setResultType(String resultType);
	
	String getResultType();
	
}
