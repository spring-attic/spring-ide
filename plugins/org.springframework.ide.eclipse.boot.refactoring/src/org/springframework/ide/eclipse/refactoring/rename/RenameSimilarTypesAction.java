/*******************************************************************************
 * Copyright (c) 2016 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.refactoring.rename;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import org.eclipse.jdt.internal.debug.ui.actions.ObjectActionDelegate;

/**
 * @author Kris De Volder
 */
@SuppressWarnings("restriction")
public class RenameSimilarTypesAction extends ObjectActionDelegate implements IWorkbenchWindowActionDelegate {

	public void run(IAction action) {
		Shell shell = getShell();
		if (fTarget != null && shell != null) {
			RenameSimilarTypesModel model = new RenameSimilarTypesModel();
			model.setTarget(fTarget);
			model.setNewName(fTarget.getElementName());
			String dialogTitle = model.getDialogTitle();
			run(new RenameSimilarTypesWizard(model, dialogTitle), shell, dialogTitle);
		}
	}

	public void run(RefactoringWizard wizard, Shell parent, String dialogTitle) {
		try {
			RefactoringWizardOpenOperation operation= new RefactoringWizardOpenOperation(wizard);
			operation.run(parent, dialogTitle);
		} catch (InterruptedException exception) {
			// Do nothing
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		fTarget = null;
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection extended= (IStructuredSelection) selection;
			Object[] elements= extended.toArray();
			if (elements.length == 1 && elements[0] instanceof IType) {
				fTarget = (IType) elements[0];
			}
		}
		try {
			action.setEnabled(fTarget != null && fTarget.exists() && fTarget.isStructureKnown() && !fTarget.isAnnotation());
		} catch (JavaModelException exception) {
			action.setEnabled(false);
		}
	}

	//////// cruft below //////////////////////////
	
	private IType fTarget = null;

	private IWorkbenchWindow window= null;

	public void dispose() {
		// Do nothing
	}

	public void init(IWorkbenchWindow window) {
		this.window= window;
	}

	@Override
	protected IWorkbenchWindow getWorkbenchWindow() {
		if (window!=null) {
			return window;
		}
		return super.getWorkbenchWindow();
	}
	
	private Shell getShell() {
		IWorkbenchWindow w = getWorkbenchWindow();
		if (w!=null) {
			return w.getShell();
		}
		return null;
	}
}
