/*******************************************************************************
 * Copyright (c) 2017 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core.cli;

import java.io.File;

import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.springframework.ide.eclipse.boot.core.cli.install.IBootInstall;

/**
 * Spring Cloud CLI installation utility methods
 * 
 * @author Alex Boyko
 *
 */
public class CloudCliUtils {
	
	public static final VersionRange CLOUD_CLI_JAVA_OPTS_SUPPORTING_VERSIONS = new VersionRange("1.2.0");
	
	private static final String CLOUD_CLI_LIB_PREFIX = "spring-cloud-cli";
	
	/**
	 * Determines Spring Cloud CLI version from the given Spring Boot CLI installation
	 * @param install Spring Boot CLI installation
	 * @return version of Spring Cloud CLI
	 */
	public static Version getVersion(IBootInstall install) {
		File[] springCloudJars = BootCliUtils.findExtensionJars(install, CLOUD_CLI_LIB_PREFIX);
		Version version = null;
		for (File jar : springCloudJars) {
			String v = BootCliUtils.getSpringBootCliJarVersion(jar.getName());
			if (v != null) {
				Version otherVersion = Version.valueOf(v);
				if (version == null) {
					version = otherVersion;
				} else if (otherVersion.compareTo(version) > 0) {
					version = otherVersion;
				}
			}
		}
		return version;
	}

}
