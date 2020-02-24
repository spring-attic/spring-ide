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

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.springframework.ide.eclipse.boot.core.ISpringBootProject;
import org.springframework.ide.eclipse.boot.core.SimpleUriBuilder;
import org.springframework.ide.eclipse.boot.core.initializr.InitializrServiceSpec.Dependency;

/**
 * Builds a URL for downloading content from Initializr. For example, this
 * builder will build a URL for downloading a generated project. The builder can
 * create a URL from a list of dependencies, as well as information from an
 * existing project (e.g. the project's name, build type, boot version...)
 *
 */
public class InitializrUrlBuilder {

	private static final String DEPENDENCIES = "dependencies";

	private static final String NAME = "name";

	private static final String TYPE = "type";

	private static final String PACKAGING = "packaging";

	private static final String BOOT_VERSION = "bootVersion";

	private static final String LANGUAGE = "language";

	private String language = "java"; // Default language

	private List<Dependency> dependencies;

	private ISpringBootProject bootProject;

	private final String initializrUrl;

	public InitializrUrlBuilder(String initializrUrl) {
		this.initializrUrl = initializrUrl;
	}

	protected String resolveBaseUrl(String initializrUrl) {
		String bUrl = initializrUrl;
		if (bUrl == null) {
			bUrl = "";
		} else {
			bUrl = bUrl.trim();
		}
		return bUrl;
	}

	public InitializrUrlBuilder dependencies(List<Dependency> dependencies) {
		this.dependencies = dependencies;
		return this;
	}


	public InitializrUrlBuilder project(ISpringBootProject bootProject) {
		this.bootProject = bootProject;
		return this;
	}

	protected String getBuildType() {
		return null;
	}

	public String build() throws Exception {
		String baseUrl = resolveBaseUrl(initializrUrl);

		SimpleUriBuilder uriBuilder = new SimpleUriBuilder(baseUrl);

		String name = getName();

		if (name != null) {
			uriBuilder.addParameter(NAME, name);
		}

		String buildType = getBuildType();
		if (buildType != null) {
			uriBuilder.addParameter(TYPE, buildType);
		}

		String packaging = getPackaging();
		if (packaging != null) {
			uriBuilder.addParameter(PACKAGING, packaging);
		}

		String bootVersion = getBootVersion();
		if (bootVersion != null) {
			uriBuilder.addParameter(BOOT_VERSION, bootVersion);
		}

		uriBuilder.addParameter(LANGUAGE, language);

		if (dependencies != null) {
			for (Dependency dep : dependencies) {
				uriBuilder.addParameter(DEPENDENCIES, dep.getId());
			}
		}

		return uriBuilder.toString();
	}

	private String getBootVersion() {
		return bootProject != null ? bootProject.getBootVersion() : null;
	}

	private String getPackaging() throws CoreException {
		return bootProject != null ? bootProject.getPackaging() : null;
	}

	private String getName() {
		return bootProject != null ? bootProject.getProject().getName() : null;
	}
}
