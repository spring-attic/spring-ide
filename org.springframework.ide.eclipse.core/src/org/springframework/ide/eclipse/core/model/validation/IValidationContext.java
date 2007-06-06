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

import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.ide.eclipse.core.model.IResourceModelElement;

/**
 * Context that gets passed to an {@link IValidationRule}, encapsulating a list
 * {@link ValidationProblem}s created during validation.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public interface IValidationContext {

	IResourceModelElement getRootElement();

	IResourceModelElement getContextElement();

	void setCurrentRuleId(String ruleId);

	Set<ValidationProblem> getProblems();

	void warning(IModelElement element, String problemId, String message,
			ValidationProblemAttribute... attributes);

	void error(IModelElement element, String problemId, String message,
			ValidationProblemAttribute... attributes);
}
