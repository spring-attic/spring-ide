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
package org.springframework.ide.eclipse.aop.ui.inplace;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.springframework.ide.eclipse.aop.ui.navigator.util.WrappingStructuredSelection;

/**
 * {@link IWorkbenchWindowActionDelegate} implementation that opens the
 * {@link AopReferenceModelInplaceDialog}.
 * 
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("restriction")
public class OpenAopModelReferenceInplaceDialogAction implements
		IWorkbenchWindowActionDelegate {

	private AopReferenceModelInplaceDialog xrefDialog;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		xrefDialog = null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		Shell parent = JavaPlugin.getActiveWorkbenchShell();
		xrefDialog = new AopReferenceModelInplaceDialog(parent);

		xrefDialog.setLastSelection(getCurrentSelection());
		xrefDialog.setWorkbenchPart(JavaPlugin.getActiveWorkbenchWindow()
				.getActivePage().getActivePart());
		xrefDialog.open();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 * org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// Have selected something in the editor - therefore
		// want to close the inplace view if haven't already done so
		if (xrefDialog != null && xrefDialog.isOpen()) {
			xrefDialog.dispose();
			xrefDialog = null;
		}
	}

	/**
	 * Returns the current selection in the workbench
	 */
	@SuppressWarnings("restriction")
	public static ISelection getCurrentSelection() {
		IWorkbenchWindow window = JavaPlugin.getActiveWorkbenchWindow();
		if (window != null) {
			if (window.getSelectionService().getSelection() instanceof IStructuredSelection) {
				return new WrappingStructuredSelection(
						(IStructuredSelection) window.getSelectionService()
								.getSelection());
			}
			return window.getSelectionService().getSelection();
		}
		return null;
	}
}
