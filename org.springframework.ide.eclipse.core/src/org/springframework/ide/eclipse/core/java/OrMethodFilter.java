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
package org.springframework.ide.eclipse.core.java;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.IMethod;

/**
 * {@link IMethodFilter} that wraps other {@link IMethodFilter}s instances and
 * applies an <code>or</code> pattern.
 * <p>
 * E.g. this class is useful to combine two {@link FlagsMethodFilter} in order
 * to filter methods that are either public or protected:
 * 
 * <pre>
 * 		OrMethodFilter filter = new OrMethodFilter();
 * 		filter.addMethodFilter(new FlagsMethodFilter(FlagsMethodFilter.PUBLIC, 
 * 			0));
 * 		filter.addMethodFilter(new FlagsMethodFilter(FlagsMethodFilter.PROTECTED,
 * 			0));
 * </pre>
 * 
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class OrMethodFilter implements IMethodFilter {

	/**
	 * Internal wrapped filters
	 */
	private Set<IMethodFilter> filters = new HashSet<IMethodFilter>();

	/**
	 * Default constructor
	 */
	public OrMethodFilter() {
	}

	/**
	 * Constructor that initializes the internal list of {@link IMethodFilter}
	 * with the given.
	 */
	public OrMethodFilter(Set<IMethodFilter> filters) {
		this.filters = filters;
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
		for (IMethodFilter filter : filters) {
			if (filter.matches(method, prefix)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Add a supplied {@link IMethodFilter} to the internal list.
	 */
	public void addMethodFilter(IMethodFilter filter) {
		filters.add(filter);
	}
}
