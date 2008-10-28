/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.ajdt.ui.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.internal.UIPlugin;

/**
 * @author Andrew Eisenberg
 * @since 2.2.1
 */
@SuppressWarnings("restriction")
public class SpringAspectsAction implements IWorkbenchWindowActionDelegate {
    
    private IProject[] projects;
    
    public void run(IAction action) {
	    Shell shell = UIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
	    if (projects != null && shell != null) {
	        boolean ask = true;
	        for (IProject project : projects) {
	            // only ask if AJ editor is not set to default
                ask = ! new SpringAspectsToolingEnabler(project, shell, ask).run();
            }
	    }
	}

	public void selectionChanged(IAction action, ISelection selection) {
	    if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            projects = new IProject[ss.size()];
            int cnt = 0;
            for (Object item : ss.toList()) {
                if (item instanceof IProject) {
                    projects[cnt++] = (IProject) item;
                } else {
                    projects = null;
                    break;
                }
            }
        }
	}

	public void dispose() {
	    projects = null;
	}

	public void init(IWorkbenchWindow window) {
	}
}