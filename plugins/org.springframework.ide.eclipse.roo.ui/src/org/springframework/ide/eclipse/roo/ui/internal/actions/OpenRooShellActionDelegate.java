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
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.springframework.ide.eclipse.roo.ui.internal.RooShellView;


/**
 * @author Christian Dupuis
 */
public class OpenRooShellActionDelegate implements IObjectActionDelegate {

	private final List<IProject> selected = new ArrayList<IProject>();

	private IWorkbenchPart workbenchPart;

	public void run(IAction action) {
		try {
			RooShellView view = (RooShellView) workbenchPart.getSite().getPage().showView(
					RooShellView.VIEW_ID, null, IWorkbenchPage.VIEW_ACTIVATE);
			Iterator<IProject> iter = selected.iterator();
			while (iter.hasNext()) {
				IProject project = iter.next();
				view.openShell(project);
			}
		}
		catch (PartInitException e) {
		}

	}

	public void selectionChanged(IAction action, ISelection selection) {
		selected.clear();
		boolean enabled = true;
		if (selection instanceof IStructuredSelection) {
			Iterator<?> iter = ((IStructuredSelection) selection).iterator();
			while (iter.hasNext()) {
				Object obj = iter.next();
				if (obj instanceof IResource) {
					IResource project = (IResource) obj;
					if (!project.getProject().isOpen()) {
						enabled = false;
						break;
					}
					else {
						selected.add(project.getProject());
					}
				}
				else {
					enabled = false;
					break;
				}
			}
		}
		action.setEnabled(enabled);
	}

	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.workbenchPart = targetPart;
	}

}
