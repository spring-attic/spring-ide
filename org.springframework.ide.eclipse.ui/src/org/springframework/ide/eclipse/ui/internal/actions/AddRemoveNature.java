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
package org.springframework.ide.eclipse.ui.internal.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.ui.SpringUIMessages;
import org.springframework.ide.eclipse.ui.SpringUIPlugin;

/**
 * This action toggles the selected project's Spring project nature.
 * 
 * @author Torsten Juergeleit
 */
public class AddRemoveNature implements IObjectActionDelegate {

	private List<IProject> selected = new ArrayList<IProject>();

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    }

	public void selectionChanged(IAction action, ISelection selection) {
		selected.clear();
		if (selection instanceof IStructuredSelection) {
			boolean enabled = true;
			Iterator iter = ((IStructuredSelection)selection).iterator();
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof IJavaProject) {
					obj = ((IJavaProject) obj).getProject();
				}
				if (obj instanceof IProject) {
					IProject project = (IProject) obj;
					if (!project.isOpen()) {
						enabled = false;
						break;
					} else {
						selected.add(project);
					}
				} else {
					enabled = false;
					break;
				}
			}
			action.setEnabled(enabled);
		}
	}

	public void run(IAction action) {
		Iterator iter = selected.iterator();
		while (iter.hasNext()) {
			IProject project = (IProject) iter.next();
			if (SpringCoreUtils.isSpringProject(project)) {
				try {
					SpringCoreUtils.removeProjectNature(project,
							  SpringCore.NATURE_ID, new NullProgressMonitor());
				} catch (CoreException e) {
					MessageDialog.openError(
							SpringUIPlugin.getActiveWorkbenchShell(),
							SpringUIMessages.ProjectNature_errorMessage,
							NLS.bind(
								  SpringUIMessages.ProjectNature_removeError,
								  project.getName(), e.getLocalizedMessage()));
				}
			} else {
				try {
					SpringCoreUtils.addProjectNature(project,
							  SpringCore.NATURE_ID, new NullProgressMonitor());
				} catch (CoreException e) {
					MessageDialog.openError(
							SpringUIPlugin.getActiveWorkbenchShell(),
							SpringUIMessages.ProjectNature_errorMessage,
							NLS.bind(
								  SpringUIMessages.ProjectNature_addError,
								  project.getName(), e.getLocalizedMessage()));
				}
			}
		}
	}
}
