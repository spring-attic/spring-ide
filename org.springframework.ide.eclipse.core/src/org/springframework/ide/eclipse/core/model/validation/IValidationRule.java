/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.model.validation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * This interface defines the protocol used to validate an {@link IModelElement}.
 * {@link ValidationProblem Validation errors} are reported via the problem
 * reporter API provided by the {@link IValidationContext}.
 * <p>
 * A {@link IValidationRule} implementation can be configured by using the
 * following syntax from within the <code>plugin.xml</code>:
 * <pre>
 * &lt;rule id=&quot;methodOverride&quot;
 * class=&quot;FooRule&quot;
 * name=&quot;Foo Rule&quot;
 * description=&quot;Bar&quot; &gt;
 * &lt;property name=&quot;threshold&quot; value=&quot;50&quot; /&gt;
 * &lt;/rule&gt;
 * </pre>
 * This requires the implementation class to have the following setter method:
 * <pre>
 * public void setThreshold(int threshold) {
 * }
 * </pre>
 * Type conversions are being handled automatically by underlying Spring
 * BeanWrapper implementation.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public interface IValidationRule<E extends IModelElement, C extends IValidationContext> {

	/**
	 * Returns <code>true</code> if this rule is able to validate the given
	 * {@link IModelElement} with the specified {@link IValidationContext}.
	 */
	boolean supports(IModelElement element, IValidationContext context);

	/**
	 * Validates the given {@link IModelElement}.
	 * @param element the element to be validated
	 * @param context the context which encapsulates all the information
	 * necessary during validation
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 * reporting and cancellation are not desired
	 */
	void validate(E element, C context, IProgressMonitor monitor);
}
