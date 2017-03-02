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
import java.util.ArrayList;
import java.util.List;

import org.springframework.ide.eclipse.boot.core.cli.install.IBootInstall;

/**
 * Utility methods for Spring Boot CLI installation
 * 
 * @author Alex Boyko
 *
 */
public class BootCliUtils {
	
	/**
	 * Retrieves currently selected Spring Boot CLI installation
	 * @return
	 * @throws Exception
	 */
	public static IBootInstall getSpringBootInstall() throws Exception {
		return BootInstallManager.getInstance().getDefaultInstall();
	}
	
	/**
	 * Extract the version of the JAR from its file name
	 * @param fileName JAR file name
	 * @return version of the JAR
	 */
	public static String getSpringBootCliJarVersion(String fileName) {
		String version = null;
		if (fileName.startsWith("spring-") && fileName.endsWith(".jar")) {
			int end = fileName.length()-4; //4 chars in ".jar"
			int start = fileName.lastIndexOf("-");
			if (start>=0) {
				version = fileName.substring(start+1, end);
			}
		}
		return version; 
	}

	/**
	 * Finds Spring Boot CLI extension JAR lib files 
	 * @param install Spring Boot CLI installation
	 * @param prefix prefix of the lib file
	 * @return matching JAR lib files
	 */
	public static File[] findExtensionJars(IBootInstall install, String prefix) {
		List<File> jars = new ArrayList<>();
		try {
			for (File lib : install.getExtensionsJars()) {
				if (lib.getName().startsWith(prefix)) {
					jars.add(lib);
				}
			}
		} catch (Exception e) {
			// ignore
		}
		return jars.toArray(new File[jars.size()]);
	}
}
