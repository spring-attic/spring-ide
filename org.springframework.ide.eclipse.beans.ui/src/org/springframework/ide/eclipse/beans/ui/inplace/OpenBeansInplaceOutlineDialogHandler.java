/*******************************************************************************
 * Copyright (c) 2005, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.inplace;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.ui.SpringUIPlugin;
import org.springframework.ide.eclipse.ui.dialogs.WrappingStructuredSelection;

/**
 * {@link IWorkbenchWindowActionDelegate} implementation that opens the
 * {@link BeansInplaceOutlineDialog}.
 * @author Christian Dupuis
 * @since 2.0.1
 */
@SuppressWarnings("restriction")
public class OpenBeansInplaceOutlineDialogHandler extends AbstractHandler {

	private BeansInplaceOutlineDialog dialog;

	public void dispose() {
		dialog = null;
	}
 
	public void init(IWorkbenchWindow window) {
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorInput input = SpringUIPlugin.getActiveWorkbenchPage().getActiveEditor()
				.getEditorInput();
		if (input instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput) input).getFile();
			// Only open for BeansConfig
			if (BeansCoreUtils.isBeansConfig(file, true)) {
				Shell parent = JavaPlugin.getActiveWorkbenchShell();
				dialog = new BeansInplaceOutlineDialog(parent);
				
				dialog.setLastSelection(getCurrentSelection());
				dialog.setWorkbenchPart(JavaPlugin.getActiveWorkbenchWindow().getActivePage()
						.getActivePart());
				dialog.open();
			}
		}
		return null; 
	}

//	public void selectionChanged(IAction action, ISelection selection) {
//		// Have selected something in the editor - therefore
//		// want to close the inplace view if haven't already done so
//		if (selection != null && dialog != null && dialog.isOpen() && !(selection instanceof TreeSelection)) {
//			dialog.dispose();
//			dialog = null;
//		}
//	}

	public static ISelection getCurrentSelection() {
		IWorkbenchWindow window = JavaPlugin.getActiveWorkbenchWindow();
		if (window != null) {
			if (window.getSelectionService().getSelection() instanceof IStructuredSelection) {
				return new WrappingStructuredSelection((IStructuredSelection) window
						.getSelectionService().getSelection());
			}
			return window.getSelectionService().getSelection();
		}
		return null;
	}

}
