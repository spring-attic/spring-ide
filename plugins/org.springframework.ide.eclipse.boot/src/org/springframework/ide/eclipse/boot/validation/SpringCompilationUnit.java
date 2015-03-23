/*******************************************************************************
 * Copyright (c) 2015 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.validation;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.springframework.ide.eclipse.core.model.AbstractSourceModelElement;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * Model element that holds data for an {@link ICompilationUnit}.
 *
 * @author Kris De Volder
 */
public class SpringCompilationUnit extends AbstractSourceModelElement {

	private ICompilationUnit cu;
	private IClasspathEntry[] classpath;

	public SpringCompilationUnit(ICompilationUnit cu, IModelElement parent,
			String name) {
		super(parent, name, null /* to be set during validation */);
		Assert.isNotNull(cu);
		this.cu = cu;
	}

	public int getElementType() {
		return IBootModelElementTypes.COMPILATION_UNIT_TYPE;
	}

	public ICompilationUnit getCompilationUnit() {
		return cu;
	}

	@Override
	public IResource getElementResource() {
		return cu.getResource();
	}

	/**
	 * Fetch the resolved classpath of the project this CU belong to.
	 */
	public IClasspathEntry[] getClasspath() {
		try {
			if (classpath==null) {
					classpath = cu.getJavaProject().getResolvedClasspath(false);
			}
			} catch (Exception e) {
				//silently ignore problems resolving classpath. Rules depending on classpath should
				// not execute if classpath is not known, for whatever reason. Do not spam the error
				// log with messages about that.
			}
		return classpath;
	}

}
