/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.model;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.core.BootPropertyTester;
import org.springframework.ide.eclipse.boot.dash.util.ProjectRunStateTracker;

public class DefaultBootDashElementFactory implements BootDashElementFactory {

	private ProjectRunStateTracker projectRunStates;

	public DefaultBootDashElementFactory(ProjectRunStateTracker runStateTracker) {
		this.projectRunStates = runStateTracker;
	}

	@Override
	public BootDashElement create(IProject p) {
		if (BootPropertyTester.isBootProject(p)) {
			return new BootProjectDashElement(p, projectRunStates);
		}
		return null;
	}

	public void dispose() {
		//Nothing todo
	}

}
