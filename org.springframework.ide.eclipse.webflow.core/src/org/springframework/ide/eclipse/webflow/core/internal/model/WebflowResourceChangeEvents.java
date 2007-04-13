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
package org.springframework.ide.eclipse.webflow.core.internal.model;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.internal.model.resources.SpringResourceChangeEventsAdapter;
import org.springframework.ide.eclipse.webflow.core.Activator;

/**
 * @author Christian Dupuis
 * @since 2.0 
 */
public class WebflowResourceChangeEvents extends SpringResourceChangeEventsAdapter {

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
}