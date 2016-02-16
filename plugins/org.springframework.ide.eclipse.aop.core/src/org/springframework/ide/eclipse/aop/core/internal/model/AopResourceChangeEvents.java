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
package org.springframework.ide.eclipse.aop.core.internal.model;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.core.internal.model.resources.SpringResourceChangeEventsAdapter;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * @author Christian Dupuis
 * @since 2.0 
 */
public class AopResourceChangeEvents extends SpringResourceChangeEventsAdapter {

	private void clearProject(IProject project) {
		IJavaProject jp = JdtUtils.getJavaProject(project);
		Activator.getModel().removeProject(jp);
		// commented because of workspace locking
		// AopReferenceModelMarkerUtils.deleteProblemMarkers(project);
	}

	public boolean isSpringProject(IProject project, int eventType) {
		IJavaProject jp = JdtUtils.getJavaProject(project);
		return jp != null && Activator.getModel().getProject(jp) != null;
	}

	public void projectClosed(IProject project, int eventType) {
		clearProject(project);
	}

	public void projectDeleted(IProject project, int eventType) {
		clearProject(project);
	}

	public void springNatureRemoved(IProject project, int eventType) {
		clearProject(project);
	}
}