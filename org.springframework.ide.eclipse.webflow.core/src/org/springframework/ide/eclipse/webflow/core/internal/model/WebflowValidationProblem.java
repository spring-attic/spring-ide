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
package org.springframework.ide.eclipse.webflow.core.internal.model;

import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;

/**
 * Describes a single validation problem.
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowValidationProblem {
	
	private String message;
	
	private IWebflowModelElement element;

	public WebflowValidationProblem(String message, IWebflowModelElement element) {
		this.message = message;
		this.element = element;
	}

	public IWebflowModelElement getElement() {
		return element;
	}

	public String getMessage() {
		return message;
	}
}
