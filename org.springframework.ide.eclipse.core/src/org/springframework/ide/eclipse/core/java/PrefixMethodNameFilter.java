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

import org.eclipse.jdt.core.IMethod;

/**
 * {@link IMethodFilter} implementation that only cares about the method name.
 * @author Christian Dupuis
 * @since 2.0.2
 */
public class PrefixMethodNameFilter implements IMethodFilter {

	/**
	 * This method calls {@link #matchesMethodPrefix(IMethod, String)}.
	 * <p>
	 * This method is intended to be overridden by subclasses.
	 */
	public boolean matches(IMethod method, String prefix) {
		return matchesMethodPrefix(method, prefix);
	}

	/**
	 * Checks if the given method's name starts with the given prefix.
	 * @param method the method to check
	 * @param prefix the prefix to match against the method's name
	 * @return true if the method's name starts with the prefix
	 */
	protected final boolean matchesMethodPrefix(IMethod method, String prefix) {
		// make sure that static initializers don't come up
		return !"<clinit>".equals(method.getElementName())
				&& method.getElementName().startsWith(prefix);
	}
}
