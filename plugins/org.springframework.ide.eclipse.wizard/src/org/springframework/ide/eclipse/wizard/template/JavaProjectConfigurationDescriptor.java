/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template;

import java.util.Set;

public class JavaProjectConfigurationDescriptor implements IProjectConfigurationDescriptor {

	private final NewSpringProjectCreationPage page;

	public JavaProjectConfigurationDescriptor(NewSpringProjectCreationPage page) {
		this.page = page;
	}

	public Set<String> getConfigSuffixes() {
		return page.getConfigSuffixes();
	}

	public boolean enableImports() {
		return page.enableImports();
	}

	public boolean enableProjectFacets() {
		return page.enableProjectFacets();
	}

	public boolean ignoreMissingNamespaceHandlers() {
		return page.ignoreMissingNamespaceHandlers();
	}

	public boolean loadHandlerFromClasspath() {
		return page.loadHandlerFromClasspath();
	}

	public boolean disableNamespaceCaching() {
		return page.disableNamespaceCaching();
	}

	public boolean useHighestXsdVersion() {
		return page.useHighestXsdVersion();
	}

	public boolean useProjectSettings() {
		return page.useProjectSettings();
	}
}
