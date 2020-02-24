/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core.initializr;

import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.internal.MavenSpringBootProject;

/**
 * Factory for creating an Initializr URL builder
 *
 */
public class InitializrUrlBuilders {

	public InitializrUrlBuilder getBuilder(ISpringBootProject bootProject, String initializrUrl) {
		if (bootProject instanceof MavenSpringBootProject) {
			return new MavenInitializrUrlBuilder(initializrUrl).project(bootProject);
		}
		return null;
	}

}
