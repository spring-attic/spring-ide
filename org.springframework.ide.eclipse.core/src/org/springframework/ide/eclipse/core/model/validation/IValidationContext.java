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

/**
 * Context that gets passed to an {@link IValidationRule}, encapsulating a list
 * {@link ValidationProblem}s created during validation.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public interface IValidationContext {

	IModelElement getRootElement();

	Set<ValidationProblem> getProblems();

	void warning(IValidationRule rule,
			String errorId, IModelElement element, String message,
			ValidationProblemAttribute... attributes);

	void error(IValidationRule rule,
			String errorId, IModelElement element, String message,
			ValidationProblemAttribute... attributes);
}
