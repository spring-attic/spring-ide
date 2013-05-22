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

public class JavaProjectConfigurationDescriptor {

	private final Set<String> suffixes;

	private final boolean enableImports;

	private final boolean enableProjectFacets;

	private final boolean ignoreMissingNamespaceHandlers;

	private final boolean disableNamespaceCaching;

	private final boolean useHighestXsdVersion;

	private final boolean useProjectSettings;

	private final boolean loadHandlerFromClasspath;

	public JavaProjectConfigurationDescriptor(Set<String> suffixes, boolean enableImports, boolean enableProjectFacets,
			boolean ignoreMissingNamespaceHandlers, boolean disableNamespaceCaching, boolean useHighestXsdVersion,
			boolean useProjectSettings, boolean loadHandlerFromClasspath) {
		this.suffixes = suffixes;
		this.enableImports = enableImports;
		this.enableProjectFacets = enableProjectFacets;
		this.ignoreMissingNamespaceHandlers = ignoreMissingNamespaceHandlers;
		this.disableNamespaceCaching = disableNamespaceCaching;
		this.useHighestXsdVersion = useHighestXsdVersion;
		this.useProjectSettings = useProjectSettings;
		this.loadHandlerFromClasspath = loadHandlerFromClasspath;

	}

	public Set<String> getConfigSuffixes() {
		return suffixes;
	}

	public boolean enableImports() {
		return enableImports;
	}

	public boolean enableProjectFacets() {
		return enableProjectFacets;
	}

	public boolean ignoreMissingNamespaceHandlers() {
		return ignoreMissingNamespaceHandlers;
	}

	public boolean loadHandlerFromClasspath() {
		return loadHandlerFromClasspath;
	}

	public boolean disableNamespaceCaching() {
		return disableNamespaceCaching;
	}

	public boolean useHighestXsdVersion() {
		return useHighestXsdVersion;
	}

	public boolean useProjectSettings() {
		return useProjectSettings;
	}
}
