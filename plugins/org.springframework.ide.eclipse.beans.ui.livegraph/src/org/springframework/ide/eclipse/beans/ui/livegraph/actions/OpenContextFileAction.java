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
package org.springframework.ide.eclipse.beans.ui.livegraph.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.BaseSelectionListenerAction;
import org.springframework.ide.eclipse.beans.ui.livegraph.LiveGraphUiPlugin;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBean;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;
import org.springsource.ide.eclipse.commons.core.StatusHandler;
import org.springsource.ide.eclipse.commons.ui.SpringUIUtils;

/**
 * @author Leo Dos Santos
 */
public class OpenContextFileAction extends BaseSelectionListenerAction {

	public OpenContextFileAction() {
		super("Open Context File");
	}

	@Override
	public void run() {
		IStructuredSelection selection = getStructuredSelection();
		List elements = selection.toList();
		final List<String> contexts = new ArrayList<String>();
		for (Object obj : elements) {
			if (obj instanceof LiveBean) {
				LiveBean bean = (LiveBean) obj;
				final String appContext = bean.getApplicationContext();
				if (appContext != null && appContext.trim().length() > 0) {
					contexts.add(appContext);
				}
			}
		}

		// find the app contexts in the workspace and open them
		try {
			// need a project mapper in place of hard-coded sample
			IProject project = SpringCoreUtils.createProject("org.springframework.samples.petclinic", null,
					new NullProgressMonitor());
			project.accept(new IResourceVisitor() {
				public boolean visit(final IResource resource) throws CoreException {
					if (resource instanceof IFile) {
						for (String appContext : contexts) {
							if (appContext.equals(resource.getName().trim())) {
								SpringUIUtils.openInEditor((IFile) resource, 0);
							}
						}
						return false;
					}
					return true;
				}
			});
		}
		catch (CoreException e) {
			StatusHandler.log(new Status(IStatus.ERROR, LiveGraphUiPlugin.PLUGIN_ID,
					"An error occurred while attempting to open an application context file."));
		}

	}

	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (!selection.isEmpty()) {
			List elements = selection.toList();
			for (Object obj : elements) {
				if (obj instanceof LiveBean) {
					LiveBean bean = (LiveBean) obj;
					String appContext = bean.getApplicationContext();
					if (appContext != null && appContext.trim().length() > 0) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
