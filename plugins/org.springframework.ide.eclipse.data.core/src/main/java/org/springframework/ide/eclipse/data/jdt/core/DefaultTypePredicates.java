/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.data.jdt.core;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * Simple implementation of {@link TypePredicates} to make functionality of JdtUtils injectable.
 * 
 * @author Oliver Gierke
 */
class DefaultTypePredicates implements TypePredicates {

	private final IJavaProject project;

	/**
	 * Creates a new {@link DefaultTypePredicates} instance using the given {@link IJavaProject}.
	 * 
	 * @param project
	 */
	public DefaultTypePredicates(IJavaProject project) {
		this.project = project;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.ide.eclipse.data.jdt.core.TypePredicates#typeImplements(org.eclipse.jdt.core.IType, java.lang.String)
	 */
	public boolean typeImplements(IType type, String candidateType) {
		return JdtUtils.doesImplement(project.getResource(), type, candidateType);
	}
}
