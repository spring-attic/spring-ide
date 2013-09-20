/*******************************************************************************
 *  Copyright (c) 2012 - 2013 GoPivotal, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.roo.ui.internal.actions;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.springframework.ide.eclipse.roo.core.RooCoreActivator;
import org.springframework.ide.eclipse.roo.ui.internal.RooInplaceDialog;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;
import org.springsource.ide.eclipse.commons.ui.SpringUIUtils;

/**
 * @author Christian Dupuis
 */
@SuppressWarnings("restriction")
public class OpenRooInplaceDialogActionDelegate implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;

	private RooInplaceDialog dialog;

	private IProject selected = null;

	public void dispose() {
		dialog = null;
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run(IAction action) {
		if (selected != null && window != null) {
			Shell parent = JavaPlugin.getActiveWorkbenchShell();
			dialog = new RooInplaceDialog(parent);
			dialog.setSelectedProject(selected);
			dialog.setWorkbenchPart(JavaPlugin.getActiveWorkbenchWindow().getActivePage().getActivePart());
			dialog.open();
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {

		selected = null;

		if (selection instanceof IStructuredSelection) {
			Iterator<?> iter = ((IStructuredSelection) selection).iterator();
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof IJavaProject) {
					obj = ((IJavaProject) obj).getProject();
				}
				else if (obj instanceof IAdaptable) {
					obj = ((IAdaptable) obj).getAdapter(IResource.class);
				}
				if (obj instanceof IResource) {
					IResource project = (IResource) obj;
					if (!project.getProject().isOpen()) {
						break;
					}
					else {
						selected = project.getProject();
					}
				}
			}
		}
		else {
			if (SpringUIUtils.getActiveEditor() != null) {
				if (SpringUIUtils.getActiveEditor().getEditorInput() instanceof IFileEditorInput) {
					selected = (((IFileEditorInput) SpringUIUtils.getActiveEditor().getEditorInput()).getFile()
							.getProject());
				}
			}
		}

		// Test if the selected project has Grails nature
		if (!SpringCoreUtils.hasNature(selected, RooCoreActivator.NATURE_ID)) {
			selected = null;
		}

		action.setEnabled(selected != null);

		// Have selected something in the editor - therefore
		// want to close the inplace view if haven't already done so
		if (selection != null && dialog != null && dialog.isOpen() && !(selection instanceof TreeSelection)) {
			dialog.dispose();
			dialog = null;
		}
	}

}
