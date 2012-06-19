/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.quickfix.rename;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.refactoring.participants.ChangeMethodSignatureParticipant;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;

/**
 * Rename participant for when a method is renamed via the change method
 * signature refactoring. If method is an @bean method, all bean reference to
 * this bean will be renamed
 * 
 * @author Martin Lippert
 * @since 2.6.0
 */
public class ChangeMethodSignatureRenameParticipant extends ChangeMethodSignatureParticipant {

	private final BeanRenameParticipant renameParticipant;

	public ChangeMethodSignatureRenameParticipant() {
		this.renameParticipant = new BeanRenameParticipant();
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
			throws OperationCanceledException {
		return renameParticipant.checkConditions(pm, context);
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		renameParticipant.setNewName(getArguments().getNewName());
		return renameParticipant.createChange(pm);
	}

	@Override
	public String getName() {
		return renameParticipant.getName();
	}

	@Override
	protected boolean initialize(Object element) {
		return renameParticipant.initialize(element);
	}

}
