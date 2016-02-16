/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.internal.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.internal.model.resources.SpringResourceChangeEventsAdapter;
import org.springframework.ide.eclipse.webflow.core.Activator;
import org.springframework.ide.eclipse.webflow.core.internal.model.resources.IWebflowResourceChangeEvents;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;

/**
 * @author Christian Dupuis
 * @since 2.0
 */
public class WebflowResourceChangeEvents extends
		SpringResourceChangeEventsAdapter implements
		IWebflowResourceChangeEvents {

	public boolean isSpringProject(IProject project, int eventType) {
		return !SpringCoreUtils.isSpringProject(project);
	}

	public void projectClosed(IProject project, int eventType) {
		clearProject(project);
	}

	private void clearProject(IProject project) {
		Activator.getModel().removeProject(project);
	}

	public void projectDeleted(IProject project, int eventType) {
		clearProject(project);
	}

	public void springNatureRemoved(IProject project, int eventType) {
		clearProject(project);
	}

	public void springNatureAdded(IProject project, int eventType) {
		clearProject(project);
	}

	public void configAdded(IFile file, int eventType) {
	}

	public void configChanged(IFile file, int eventType) {
		IProject project = file.getProject();
		IWebflowProject wfp = Activator.getModel().getProject(project);
		if (wfp != null) {
			Activator.getModel().fireModelChangedEvent(wfp);
		}
	}

	public void configRemoved(IFile file, int eventType) {
	}

	public void projectDescriptionChanged(IFile file, int eventType) {
		// trigger build of project
		SpringCoreUtils.buildProject(file.getProject());
	}
}