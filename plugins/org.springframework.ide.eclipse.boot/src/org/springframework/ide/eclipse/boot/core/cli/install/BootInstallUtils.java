/*******************************************************************************
 *  Copyright (c) 2017, 2018 Pivotal Software, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core.cli.install;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.springsource.ide.eclipse.commons.core.preferences.StsProperties;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * Utility methods for Spring Boot CLI installation
 *
 * @author Alex Boyko
 *
 */
public class BootInstallUtils {

	/**
	 * Extension type class to maven coordinates prefix (excludes version) map
	 */
	static final Map<Class<? extends IBootInstallExtension>, String> EXTENSION_TO_MAVEN_PREFIX_MAP = new HashMap<>();
	static {
		EXTENSION_TO_MAVEN_PREFIX_MAP.put(CloudCliInstall.class, "org.springframework.cloud:spring-cloud-cli:");
	}

	/**
	 * Extension type class to extension full name map
	 */
	public static final ImmutableMap<Class<? extends IBootInstallExtension>, String> EXTENSION_TO_TITLE_MAP;
	static {
		Builder<Class<? extends IBootInstallExtension>, String> builder = ImmutableMap.<Class<? extends IBootInstallExtension>, String>builder();
		builder.put(CloudCliInstall.class, "Spring Cloud CLI");
		EXTENSION_TO_TITLE_MAP = builder.build();
	}

	/**
	 * Calculates the latest compatible Spring Cloud CLI version provided the Boot CLI version
	 * @param bootVersion Spring Boot CLI version
	 * @return latest compatible version of Cloud CLI
	 */
	public static Version getCloudCliVersion(Version bootVersion) {
		if (bootVersion == null) {
			throw new IllegalArgumentException();
		}
		if (VersionRange.valueOf("2.0.0").includes(bootVersion)) {
			return Version.valueOf(StsProperties.getInstance().get("spring.boot.cloud.default.version"));
		} else if (VersionRange.valueOf("[1.5.3,2.0.0)").includes(bootVersion)) {
			return Version.valueOf("1.4.0.RELEASE");
		} else if (VersionRange.valueOf("[1.4.4,1.5.3)").includes(bootVersion)) {
			return Version.valueOf("1.3.2.RELEASE");
		} else if (VersionRange.valueOf("[1.2.2,1.4.4)").includes(bootVersion)) {
			return Version.valueOf("1.2.2.RELEASE");
//		} else if (VersionRange.valueOf("[1.3.7, 1.4.1)").includes(bootVersion)) {
//			return Version.valueOf("1.1.6.RELEASE");
//		} else if (VersionRange.valueOf("[1.3.5,1.3.7)").includes(bootVersion)) {
//			return Version.valueOf("1.1.5.RELEASE");
//		} else if (VersionRange.valueOf("[1.2.8,1.3.5)").includes(bootVersion)) {
//			return Version.valueOf("1.0.6.RELEASE");
//		} else if (VersionRange.valueOf("[1.2.6,1.2.8)").includes(bootVersion)) {
//			return Version.valueOf("1.0.4.RELEASE");
//		} else if (VersionRange.valueOf("[1.2.4,1.2.6)").includes(bootVersion)) {
//			return Version.valueOf("1.0.3.RELEASE");
//		} else if (VersionRange.valueOf("[1.2.3,1.2.4)").includes(bootVersion)) {
//			return Version.valueOf("1.0.2.RELEASE");
//		} else if (VersionRange.valueOf("[1.2.2,1.2.3)").includes(bootVersion)) {
//			return Version.valueOf("1.0.0.RELEASE");
		} else {
			return null;
		}
	}

}
