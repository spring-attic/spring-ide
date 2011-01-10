/*******************************************************************************
 * Copyright (c) 2008 Spring IDE Developers
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

/**
 * {@link IMethodFilter} implementation that wraps several {@link IMethodFilter} instances in order
 * to chain calls to {@link #matches(org.eclipse.jdt.core.IMethod,String)} by subclasses. 
 * @author Christian Dupuis
 * @since 2.2.1
 */
public abstract class AbstractCompositeMethodFilter implements IMethodFilter {

	/**
	 * Internal wrapped filters
	 */
	private Set<IMethodFilter> filters = new HashSet<IMethodFilter>();

	/**
	 * Default constructor
	 */
	public AbstractCompositeMethodFilter() {
	}

	/**
	 * Constructor that initializes the internal list of {@link IMethodFilter}
	 * with the given.
	 */
	public AbstractCompositeMethodFilter(Set<IMethodFilter> filters) {
		this.filters = filters;
	}

	/**
	 * Add a supplied {@link IMethodFilter} to the internal list.
	 */
	public void addMethodFilter(IMethodFilter filter) {
		filters.add(filter);
	}
	
	protected Set<IMethodFilter> getMethodFilters() {
		return filters;
	}
	
}
