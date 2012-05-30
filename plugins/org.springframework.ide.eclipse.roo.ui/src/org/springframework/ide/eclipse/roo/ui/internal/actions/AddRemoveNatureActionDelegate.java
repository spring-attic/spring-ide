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
package org.springframework.ide.eclipse.roo.ui.internal.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.springframework.ide.eclipse.roo.core.RooCoreActivator;
import org.springframework.ide.eclipse.ui.SpringUIMessages;
import org.springframework.ide.eclipse.ui.SpringUIPlugin;


/**
 * @author Christian Dupuis
 */
public class AddRemoveNatureActionDelegate implements IObjectActionDelegate {

	private List<IProject> selected = new ArrayList<IProject>();

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	@SuppressWarnings("unchecked")
	public void selectionChanged(IAction action, ISelection selection) {
		selected.clear();
		if (selection instanceof IStructuredSelection) {
			boolean enabled = true;
			Iterator<Object> iter = ((IStructuredSelection) selection).iterator();
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
					}
					else {
						selected.add(project);
					}
				}
				else {
					enabled = false;
					break;
				}
			}
			action.setEnabled(enabled);
		}
	}

	public void run(IAction action) {
		Iterator<IProject> iter = selected.iterator();
		while (iter.hasNext()) {
			IProject project = iter.next();
			if (SpringCoreUtils.hasNature(project, RooCoreActivator.NATURE_ID)) {
				IProgressMonitor pm = new NullProgressMonitor();
				try {
					SpringCoreUtils.removeProjectNature(project, RooCoreActivator.NATURE_ID, pm);
				}
				catch (CoreException e) {
					MessageDialog.openError(SpringUIPlugin.getActiveWorkbenchShell(),
							SpringUIMessages.ProjectNature_errorMessage, NLS.bind(
									SpringUIMessages.ProjectNature_removeError, project.getName(),
									e.getLocalizedMessage()));
				}
			}
			else {
				IProgressMonitor pm = new NullProgressMonitor();
				try {
					SpringCoreUtils.addProjectNature(project, SpringCore.NATURE_ID, pm);
					SpringCoreUtils.addProjectNature(project, RooCoreActivator.NATURE_ID, pm);
				}
				catch (CoreException e) {
					MessageDialog.openError(SpringUIPlugin.getActiveWorkbenchShell(),
							SpringUIMessages.ProjectNature_errorMessage, NLS.bind(
									SpringUIMessages.ProjectNature_addError, project.getName(), e
											.getLocalizedMessage()));
				}
			}
		}
	}
}
