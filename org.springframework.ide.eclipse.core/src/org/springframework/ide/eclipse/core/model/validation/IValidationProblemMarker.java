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
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public interface IValidationProblemMarker {

	int SEVERITY_WARNING = IMarker.SEVERITY_WARNING;

	int SEVERITY_ERROR = IMarker.SEVERITY_ERROR;
	
	/**
	 * @since 2.0.2
	 */
	int SEVERITY_INFO = IMarker.SEVERITY_INFO;

	/**
	 * Rule ID marker attribute (value <code>"ruleId"</code>).
	 */
	String RULE_ID = "ruleId";

	/**
	 * Error ID marker attribute (value <code>"errorId"</code>).
	 */
	String ERROR_ID = "errorId";
}
