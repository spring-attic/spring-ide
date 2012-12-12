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
package org.springframework.ide.eclipse.data.internal.validation;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ITypeRoot;
import org.springframework.ide.eclipse.core.model.AbstractSourceModelElement;
import org.springframework.ide.eclipse.core.model.IModelElement;
import org.springframework.util.Assert;

/**
 * Model element that holds data for an {@link ICompilationUnit}.
 *
 * @author Tomasz Zarna
 *
 */
public class CompilationUnit extends AbstractSourceModelElement {

	private ICompilationUnit cu;

	public CompilationUnit(ICompilationUnit cu, IModelElement parent,
			String name) {
		super(parent, name, null /* to be set during validation */);
		Assert.notNull(cu);
		this.cu = cu;
	}

	public int getElementType() {
		return ISpringDataModelElementTypes.COMPILATION_UNIT_TYPE;
	}

	public ITypeRoot getTypeRoot() {
		return cu;
	}

	@Override
	public IResource getElementResource() {
		return cu.getResource();
	}

}
