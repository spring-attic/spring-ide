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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Torsten Juergeleit
 * @since 2.0
 */
public interface IValidator {

	/**
	 * Returns <code>true</code> if ths validator is able to validate the given
	 * {@link IResource}.
	 */
	boolean supports(IResource resource);

	/**
	 * Validates the given {@link IResource}.
	 * @param resource  the resource to be validated
	 * @param context  the context which encapsulates all the information
	 * 				neccessary during validation
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 * 			reporting and cancellation are not desired
	 */
	void validate(IResource resource, IProgressMonitor monitor);

	/**
	 * Returns a list of validation problems occured during the last call of
	 * {@link #validate(IResource, IProgressMonitor)}.
	 */
	Set<ValidationProblem> getProblems();
}
