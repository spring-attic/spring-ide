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

	private static final String DESCRIPTION = "description";

	private static final String GROUP_ID = "groupId";

	private static final String ARTIFACT_ID = "artifactId";

	private static final String JAVA_VERSION = "javaVerion";

	private static final String VERSION = "version";

	private static final String PACKAGE_NAME = "packageName";

	private String language = "java"; // Default language

	private List<Dependency> dependencies;

	private String groupId;

	private String artifactId;

	private String version;

	private String description;

	private String name;

	private String buildType;

	private String packaging;

	private String bootVersion;

	private String javaVersion;

	private String packageName;

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
		try {
			this.name = bootProject.getProject().getName();
			this.groupId = bootProject.groupId();
			this.artifactId = bootProject.artifactId();
			this.version = bootProject.version();
			this.description = bootProject.description();
			this.javaVersion = bootProject.javaVersion();
			this.bootVersion = bootProject.getBootVersion();
			this.packaging = bootProject.getPackaging();
			this.buildType = bootProject.buildType();
			this.packageName = bootProject.packageName();
		} catch (CoreException e) {
			throw new IllegalArgumentException(e);
		}
		return this;
	}

	public String build() throws Exception {
		String baseUrl = resolveBaseUrl(initializrUrl);

		SimpleUriBuilder uriBuilder = new SimpleUriBuilder(baseUrl);

		if (name != null) {
			uriBuilder.addParameter(NAME, name);
		}

		if (buildType != null) {
			uriBuilder.addParameter(TYPE, buildType);
		}

		if (packaging != null) {
			uriBuilder.addParameter(PACKAGING, packaging);
		}

		if (bootVersion != null) {
			uriBuilder.addParameter(BOOT_VERSION, bootVersion);
		}

		if (groupId != null) {
			uriBuilder.addParameter(GROUP_ID, groupId);
		}

		if (artifactId != null) {
			uriBuilder.addParameter(ARTIFACT_ID, artifactId);
		}

		if (version != null) {
			uriBuilder.addParameter(VERSION, version);
		}

		if (description != null) {
			uriBuilder.addParameter(DESCRIPTION, description);
		}

		if (javaVersion != null) {
			uriBuilder.addParameter(JAVA_VERSION, javaVersion);
		}

		if (language != null) {
			uriBuilder.addParameter(LANGUAGE, language);
		}

		if (packageName != null) {
			uriBuilder.addParameter(PACKAGE_NAME, packageName);
		}

		if (dependencies != null) {
			for (Dependency dep : dependencies) {
				uriBuilder.addParameter(DEPENDENCIES, dep.getId());
			}
		}

		return uriBuilder.toString();
	}

	public InitializrUrlBuilder bootVersion(String bootVersion) {
		this.bootVersion = bootVersion;
		return this;
	}

	public InitializrUrlBuilder packaging(String packaging) {
		this.packaging = packaging;
		return this;
	}

	public InitializrUrlBuilder name(String name) {
		this.name = name;
		return this;
	}

	public InitializrUrlBuilder buildType(String buildType) {
		this.buildType = buildType;
		return this;
	}

	public InitializrUrlBuilder groupId(String groupId) {
		this.groupId = groupId;
		return this;
	}

	public InitializrUrlBuilder artifactId(String artifactId) {
		this.artifactId = artifactId;
		return this;
	}

	public InitializrUrlBuilder version(String version) {
		this.version = version;
		return this;
	}

	public InitializrUrlBuilder description(String description) {
		this.description = description;
		return this;
	}

	public InitializrUrlBuilder javaVersion(String javaVersion) {
		this.javaVersion = javaVersion;
		return this;
	}

	public InitializrUrlBuilder packageName(String packageName) {
		this.packageName = packageName;
		return this;
	}
}
