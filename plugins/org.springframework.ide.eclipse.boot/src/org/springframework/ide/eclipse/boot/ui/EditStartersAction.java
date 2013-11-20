/*******************************************************************************
 * Copyright (c) 2013 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.ui;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.springframework.ide.eclipse.boot.core.BootActivator;
import org.springframework.ide.eclipse.boot.core.BootPropertyTester;
import org.springsource.ide.eclipse.commons.frameworks.core.ExceptionUtil;
import org.springsource.ide.eclipse.commons.frameworks.ui.internal.actions.AbstractActionDelegate;

public class EditStartersAction extends AbstractActionDelegate {
	
	@Override
	public void selectionChanged(IAction action, ISelection sel) {
		super.selectionChanged(action, sel);
		action.setEnabled(BootPropertyTester.isBootProject(getSelectedProject()));
	}

	/**
	 * @return The first selected project or null if no project is selected.
	 */
	private IProject getSelectedProject() {
		List<IProject> projects = getSelectedProjects();
		if (projects!=null && !projects.isEmpty()) {
			return projects.get(0);
		}
		return null;
	}

	@Override
	public void run(IAction action) {
		try {
			EditStartersDialog.openFor(getSelectedProject(), getShell());
		} catch (CoreException e) {
			BootActivator.log(e);
			MessageDialog.openError(getShell(), "Error", ExceptionUtil.getMessage(e)+"\n\n"
					+ "Check the error log for more details");
		}
	}

}
