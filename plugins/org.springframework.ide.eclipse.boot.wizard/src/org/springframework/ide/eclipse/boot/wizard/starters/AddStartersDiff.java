/*******************************************************************************
 * Copyright (c) 2020 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.wizard.starters;

import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;
import org.springframework.ide.eclipse.boot.wizard.HierarchicalMultiSelectionFieldModel;

public class AddStartersDiff {

	private final ISpringBootProject bootProject;
	private final HierarchicalMultiSelectionFieldModel<Dependency> dependencies;

	public AddStartersDiff(ISpringBootProject bootProject,
			HierarchicalMultiSelectionFieldModel<Dependency> dependencies) {
		this.bootProject = bootProject;
		this.dependencies = dependencies;
	}

	public AddStartersCompareInput getCompareInput() throws Exception {
		IProject project = bootProject.getProject();

		boolean editable = true;
		LocalResource localResource = new LocalResource(project, getSelectedResource(), editable);

		GeneratedResource generatedResource = new GeneratedResource(getSelectedResource(), localResource.getImage(),
				bootProject.generatePom(dependencies.getCurrentSelection()));

		return new AddStartersCompareInput(localResource, generatedResource);
	}

	private String getSelectedResource() {
		return "pom.xml";
	}

}
