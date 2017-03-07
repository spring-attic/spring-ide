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
import java.util.regex.Pattern;

import org.osgi.framework.Version;
import org.osgi.framework.VersionRange;
import org.springframework.ide.eclipse.boot.core.cli.install.IBootInstall;
import org.springsource.ide.eclipse.commons.livexp.util.ExceptionUtil;

/**
 * Spring Cloud CLI installation utility methods
 * 
 * @author Alex Boyko
 *
 */
public class CloudCliUtils {
	
	public static final VersionRange CLOUD_CLI_JAVA_OPTS_SUPPORTING_VERSIONS = new VersionRange("1.2.0");
	
	private static final String CLOUD_CLI_LIB_PREFIX = "spring-cloud-cli";
	
	private static final Pattern CLOUD_CLI_CMD_ERROR_PATTERN = Pattern.compile("^\\s*Exception in thread ");
	
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
	
	public static boolean isCommandOutputErroneous(String output) {
		return CLOUD_CLI_CMD_ERROR_PATTERN.matcher(output).find();
	}
	
	public static String[] getCloudServices(IBootInstall bootInstall) throws Exception {
		Version cloudCliVersion = CloudCliUtils.getVersion(bootInstall);
		if (cloudCliVersion != null
				&& CloudCliUtils.CLOUD_CLI_JAVA_OPTS_SUPPORTING_VERSIONS.includes(cloudCliVersion)) {
			BootCliCommand cmd = new BootCliCommand(bootInstall.getHome());
			try {
				cmd.execute("cloud", "--list");
				if (!isCommandOutputErroneous(cmd.getOutput())) {
					String[] outputLines = cmd.getOutput().split("\n");
					return outputLines[outputLines.length - 1].split("\\s+");
				}
			} catch (RuntimeException e) {
				throw ExceptionUtil.coreException(e);
			}
		}
		return new String[0];
	}

}
