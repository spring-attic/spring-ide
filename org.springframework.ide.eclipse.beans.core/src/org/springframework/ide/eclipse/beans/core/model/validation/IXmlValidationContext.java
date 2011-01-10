/*******************************************************************************
 * Copyright (c) 2008, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.model.validation;

import java.util.List;

import org.springframework.ide.eclipse.beans.core.namespaces.ToolAnnotationUtils.ToolAnnotationData;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblem;
import org.springframework.ide.eclipse.core.model.validation.ValidationProblemAttribute;
import org.w3c.dom.Node;

/**
 * Extension to {@link IBeansValidationContext} that allows to report errors against {@link Node}s.
 * @author Christian Dupuis
 * @since 2.0.4
 */
public interface IXmlValidationContext extends IBeansValidationContext {

	/**
	 * Reports a {@link ValidationProblem} of severity error.
	 * <p>
	 * This method should be used to report simple information messages.
	 * @param node the current node
	 * @param problemId a unique id of the problem
	 * @param message the string message displayed
	 * @param attributes some optional meta attributes which can be useful for the implementing a quick fix for this
	 * problem
	 */
	void error(Node node, String problemId, String message, ValidationProblemAttribute... attributes);

	/**
	 * Reports a {@link ValidationProblem} of severity warning.
	 * <p>
	 * This method should be used to report simple information messages.
	 * @param node the current node
	 * @param problemId a unique id of the problem
	 * @param message the string message displayed
	 * @param attributes some optional meta attributes which can be useful for the implementing a quick fix for this
	 * problem
	 */
	void warning(Node node, String problemId, String message, ValidationProblemAttribute... attributes);

	/**
	 * Reports a {@link ValidationProblem} of severity info.
	 * <p>
	 * This method should be used to report simple information messages.
	 * @param node the current node
	 * @param problemId a unique id of the problem
	 * @param message the string message displayed
	 * @param attributes some optional meta attributes which can be useful for the implementing a quick fix for this
	 * problem
	 */
	void info(Node node, String problemId, String message, ValidationProblemAttribute... attributes);
	
	/**
	 * Returns the list of attached Spring Tool XSD annotations.
	 * @since 2.2.7 
	 */
	List<ToolAnnotationData> getToolAnnotation(Node n, String attributeName);
}