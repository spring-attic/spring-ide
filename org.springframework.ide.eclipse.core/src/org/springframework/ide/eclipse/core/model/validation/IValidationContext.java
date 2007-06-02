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
package org.springframework.ide.eclipse.core.model.validation;

import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * Context that gets passed to an {@link IValidationRule}, encapsulating a list
 * {@link ValidationProblem}s created during validation.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public interface IValidationContext {
	
	int SEVERITY_ERROR = IMarker.SEVERITY_ERROR;
	int SEVERITY_WARNING = IMarker.SEVERITY_WARNING;

	Set<ValidationProblem> getProblems();

	Set<IModelElement> getRootElements();

	void setCurrentRootElement(IModelElement element);

	IModelElement getCurrentRootElement();

	void setCurrentRuleId(String ruleId);

	String getCurrentRuleId();

	void warning(IModelElement element, String problemId, String message,
			ValidationProblemAttribute... attributes);

	void error(IModelElement element, String problemId, String message,
			ValidationProblemAttribute... attributes);
}
