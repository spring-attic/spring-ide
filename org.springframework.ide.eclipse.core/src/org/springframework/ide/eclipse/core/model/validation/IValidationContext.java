/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.model.validation;

import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;

/**
 * Context that gets passed to an {@link IValidationRule}, encapsulating a list {@link ValidationProblem}s created
 * during validation.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public interface IValidationContext {

	/**
	 * Returns the root element of the current validation.
	 * <p>
	 * Usually a {@link IResourceModelElement} that has a associated {@link IResource}.
	 */
	IResourceModelElement getRootElement();

	/**
	 * Returns the element under which the root element should be validated
	 */
	IResourceModelElement getContextElement();

	/**
	 * Sets the id of the current executing rule
	 * @param ruleId the rule id
	 */
	void setCurrentRuleId(String ruleId);

	/**
	 * Returns the set of reported {@link ValidationProblem}s.
	 * @return a set of {@link ValidationProblem}s
	 */
	Set<ValidationProblem> getProblems();

	/**
	 * Reports a {@link ValidationProblem} of severity info.
	 * <p>
	 * This method should be used to report simple information messages.
	 * @param element the current element
	 * @param problemId a unique id of the problem
	 * @param message the string message displayed
	 * @param attributes some optional meta attributes which can be useful for the implementing a quick fix for this
	 * problem
	 * @since 2.0.2
	 */
	void info(IResourceModelElement element, String problemId, String message, ValidationProblemAttribute... attributes);

	/**
	 * Reports a {@link ValidationProblem} of warning info.
	 * <p>
	 * This method should be used to report messages that express warnings which don't prevent an application from
	 * working.
	 * @param element the current element
	 * @param problemId a unique id of the problem
	 * @param message the string message displayed
	 * @param attributes some optional meta attributes which can be useful for the implementing a quick fix for this
	 * problem
	 */
	void warning(IResourceModelElement element, String problemId, String message,
			ValidationProblemAttribute... attributes);

	/**
	 * Reports a {@link ValidationProblem} of severity info.
	 * <p>
	 * This method should be used to report critical errors.
	 * @param element the current element
	 * @param problemId a unique id of the problem
	 * @param message the string message displayed
	 * @param attributes some optional meta attributes which can be useful for the implementing a quick fix for this
	 * problem
	 */
	void error(IResourceModelElement element, String problemId, String message,
			ValidationProblemAttribute... attributes);

	/**
	 * Add {@link ValidationProblem} instances to the existing internal list.
	 * @param problems the problems to add
	 * @since 2.3.0
	 */
	void addProblems(ValidationProblem... problems);

}
