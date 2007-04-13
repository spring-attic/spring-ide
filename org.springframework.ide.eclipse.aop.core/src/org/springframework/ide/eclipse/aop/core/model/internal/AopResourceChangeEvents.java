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
package org.springframework.ide.eclipse.aop.core.model.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.springframework.ide.eclipse.aop.core.Activator;
import org.springframework.ide.eclipse.aop.core.util.AopReferenceModelMarkerUtils;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModelUtils;
import org.springframework.ide.eclipse.core.internal.model.resources.SpringResourceChangeEventsAdapter;

/**
 * @author Christian Dupuis
 * @since 2.0 
 */
public class AopResourceChangeEvents extends SpringResourceChangeEventsAdapter {

	public boolean isSpringProject(IProject project, int eventType) {
		IJavaProject jp = BeansModelUtils.getJavaProject(project);
		return jp != null && Activator.getModel().getProject(jp) != null;
	}

	public void projectClosed(IProject project, int eventType) {
		clearProject(project);
	}

	private void clearProject(IProject project) {
		IJavaProject jp = BeansModelUtils.getJavaProject(project);
		Activator.getModel().removeProject(jp);
		AopReferenceModelMarkerUtils.deleteProblemMarkers(project);
	}

	public void projectDeleted(IProject project, int eventType) {
		clearProject(project);
	}

	public void springNatureRemoved(IProject project, int eventType) {
		clearProject(project);
	}
}