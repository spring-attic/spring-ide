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
package org.springframework.ide.eclipse.core.java;

import java.util.Set;

import org.eclipse.jdt.core.IMethod;

/**
 * {@link IMethodFilter} that wraps other {@link IMethodFilter}s instances and
 * applies an <code>and</code> pattern.
 * <p>
 * @author Christian Dupuis
 * @since 2.2.1
 */
public class AndMethodFilter extends AbstractCompositeMethodFilter implements IMethodFilter {

	/**
	 * Default constructor
	 */
	public AndMethodFilter() {
	}

	/**
	 * Constructor that initializes the internal list of {@link IMethodFilter}
	 * with the given.
	 */
	public AndMethodFilter(Set<IMethodFilter> filters) {
		super(filters);
	}

	/**
	 * Sequentially calls {@link #matches(IMethod, String)} on the wrapped
	 * {@link IMethodFilter}.
	 * <p>
	 * After the first false returned from calling one of the wrapped
	 * {@link IMethodFilter}'s {@link #matches(IMethod, String)} method this
	 * implementation returns <code>false</code>.
	 */
	public boolean matches(IMethod method, String prefix) {
		for (IMethodFilter filter : getMethodFilters()) {
			if (!filter.matches(method, prefix)) {
				return false;
			}
		}
		return true;
	}
	
}
