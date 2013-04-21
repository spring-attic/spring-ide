/*******************************************************************************
 * Copyright (c) 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.refactoring.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

/**
 * refactoring participant to rename java-based bean configs in case the referenced Java type is renamed
 * 
 * @author Martin Lippert
 * @since 3.3.0
 */
public class BeansJavaConfigRenameTypeRefactoringParticipant extends RenameParticipant {

	private IType type;

	@Override
	protected boolean initialize(Object element) {
		if (element instanceof IType) {
			type = (IType) element;
			return true;
		}
		return false;
	}

	@Override
	public String getName() {
		return "Update references in Spring project configurations";
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		if (!getArguments().getUpdateReferences()) {
			return null;
		}
		String newName = getArguments().getNewName();
		
		if (type.getDeclaringType() != null) {
			newName = type.getDeclaringType().getFullyQualifiedName() + "$" + newName;
		}
		else if (!type.getPackageFragment().isDefaultPackage()) {
			newName = type.getPackageFragment().getElementName() + "." + newName;
		}
		
		return new BeansJavaConfigTypeChange(type, newName);
	}

}
