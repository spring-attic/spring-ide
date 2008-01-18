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
package org.springframework.ide.eclipse.core.internal.model.validation;

import org.springframework.ide.eclipse.core.model.IResourceModelElement;
import org.springframework.ide.eclipse.core.model.ISpringProject;
import org.springframework.ide.eclipse.core.model.validation.AbstractValidationContext;
import org.springframework.ide.eclipse.core.model.validation.IValidationContext;

/**
 * Basic {@link IValidationContext} implementation used to validate a
 * {@link ISpringProject}.
 * @author Christian Dupuis
 * @since 2.0.3
 */
public class SpringValidationContext extends AbstractValidationContext
		implements IValidationContext {

	/**
	 * Constructor that simply delegates to the super class' constructor.
	 * @param rootElement the root element to validate
	 * @param contextElement the context of validation
	 */
	public SpringValidationContext(IResourceModelElement rootElement,
			IResourceModelElement contextElement) {
		super(rootElement, contextElement);
	}

}
