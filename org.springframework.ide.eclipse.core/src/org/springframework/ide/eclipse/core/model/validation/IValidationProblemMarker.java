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

import org.eclipse.core.resources.IMarker;

/**
 * Markers related with Spring validation problems.
 * <p>
 * This interface declares constants only; it is not intended to be implemented
 * or extended.
 * </p>
 * @author Torsten Juergeleit
 * @since 2.0
 */
public interface IValidationProblemMarker {

	int SEVERITY_WARNING = IMarker.SEVERITY_WARNING;
	int SEVERITY_ERROR = IMarker.SEVERITY_ERROR;

	/**
	 * Rule ID marker attribute (value <code>"ruleId"</code>).
	 */
	String RULE_ID = "ruleId";

	/**
	 * Error ID marker attribute (value <code>"errorId"</code>).
	 */
	String ERROR_ID = "errorId";
}
