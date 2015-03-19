package org.springframework.ide.eclipse.boot.validation;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.ITypeRoot;
import org.springframework.ide.eclipse.core.model.AbstractSourceModelElement;
import org.springframework.ide.eclipse.core.model.IModelElement;

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
		Assert.isNotNull(cu);
		this.cu = cu;
	}

	public int getElementType() {
		return IBootModelElementTypes.COMPILATION_UNIT_TYPE;
	}

	public ITypeRoot getTypeRoot() {
		return cu;
	}

	@Override
	public IResource getElementResource() {
		return cu.getResource();
	}

}
