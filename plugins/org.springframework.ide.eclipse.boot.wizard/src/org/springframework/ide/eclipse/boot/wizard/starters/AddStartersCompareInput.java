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

import org.eclipse.compare.CompareConfiguration;

public class AddStartersCompareInput {

	private final LocalResource localResource;
	private final GeneratedResource generatedResource;

	public AddStartersCompareInput(LocalResource localResource, GeneratedResource generatedResource) {
		this.localResource = localResource;
		this.generatedResource = generatedResource;
	}

	public GeneratedResource getGeneratedResource() {
		return generatedResource;
	}

	public LocalResource getLocalResource() {
		return localResource;
	}

	public CompareConfiguration getConfiguration() {
		CompareConfiguration config = new CompareConfiguration();

		config.setLeftLabel(localResource.getLabel());
		config.setLeftImage(localResource.getImage());
		config.setRightLabel("Spring Initializr: " + generatedResource.getName());
		config.setRightImage(generatedResource.getImage());
		config.setLeftEditable(localResource.isEditable());
		return config;
	}

}
