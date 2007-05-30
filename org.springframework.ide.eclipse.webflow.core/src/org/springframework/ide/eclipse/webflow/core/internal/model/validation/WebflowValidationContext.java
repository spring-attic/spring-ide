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

import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.core.model.validation.AbstractValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowConfig;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModelElement;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowState;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowValidationContext extends AbstractValidationContext
		implements IValidationContext<IWebflowModelElement> {

	private final IWebflowConfig webflowConfig;
	
	private final IWebflowState webflowState;

	public WebflowValidationContext(IResource resource,
			IWebflowState webflowState, IWebflowConfig webflowConfig) {
		super(resource);
		this.webflowState = webflowState;
		this.webflowConfig = webflowConfig;
	}

	public IWebflowModelElement getRootElement() {
		return this.webflowState;
	}

	public IWebflowConfig getWebflowConfig() {
		return webflowConfig;
	}

	public IWebflowState getWebflowState() {
		return webflowState;
	}
}
