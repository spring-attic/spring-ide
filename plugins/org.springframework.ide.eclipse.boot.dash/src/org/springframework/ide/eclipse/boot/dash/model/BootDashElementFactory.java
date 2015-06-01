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

public class BootDashElementFactory {

	private BootDashModel model;

	public BootDashElementFactory(BootDashModel model) {
		this.model = model;
	}

	public BootDashElement create(IProject p) {
		if (BootPropertyTester.isBootProject(p)) {
			return new BootProjectDashElement(p, model);
		}
		return null;
	}

	public void dispose() {
		//Nothing todo
	}

}
