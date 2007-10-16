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

import org.eclipse.jdt.core.IMethod;

/**
 * Implementations of this interface are able to filter given {@link IMethod}s
 * during a search.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public interface IMethodFilter {
	
	/**
	 * Check if the given {@link IMethod} matches this filter.
	 * @param method the method to filter
	 * @param prefix an optional method name prefix
	 * @return true if this filter matches on the given {@link IMethod}
	 */
	boolean matches(IMethod method, String prefix);
}
