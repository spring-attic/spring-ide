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
package org.springframework.ide.eclipse.webflow.core.internal.model.validation;

import org.springframework.ide.eclipse.core.model.validation.AbstractValidationContext;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowValidationContext extends AbstractValidationContext {

	private final IWebflowConfig webflowConfig;

	public WebflowValidationContext(IWebflowState state,
			IWebflowConfig webflowConfig) {
		super(state, null);
		this.webflowConfig = webflowConfig;
	}

	public IWebflowConfig getWebflowConfig() {
		return webflowConfig;
	}
}
