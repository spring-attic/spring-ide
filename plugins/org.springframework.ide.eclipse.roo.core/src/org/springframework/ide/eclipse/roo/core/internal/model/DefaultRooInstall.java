/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.roo.core.internal.model;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.service.resolver.VersionRange;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.roo.core.RooCoreActivator;
import org.springframework.ide.eclipse.roo.core.model.IRooInstall;
import org.springframework.util.StringUtils;


/**
 * Instances of this class represent an installation of Spring Roo.
 * @author Christian Dupuis
 */
public class DefaultRooInstall implements IRooInstall {

	private final String home;

	private boolean isDefault = false;

	private final String name;

	public DefaultRooInstall(String home, String name, boolean isDefault) {
		this.home = (home != null && !home.endsWith(File.separator) ? home + File.separator : home);
		this.name = name;
		this.isDefault = isDefault;
	}

	public URL[] getClasspath() {
		if (!StringUtils.hasLength(home)) {
			return new URL[0];
		}

		Set<URL> urls = new LinkedHashSet<URL>();

		File rooHome = new File(home);
		if (rooHome.exists()) {
			addBootstrapJar(urls, rooHome, "bin");
			addBootstrapJar(urls, rooHome, "bundle");
		}
		return urls.toArray(new URL[urls.size()]);
	}

	public String getHome() {
		return home;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		if (home != null) {
			return versionInfo();
		}
		return "<UNKNOWN VERSION>";
	}

	public boolean isDefault() {
		return isDefault;
	}

	public IStatus validate() {
		// Check if Roo home exists
		File home = new File(getHome());
		if (!home.exists()) {
			return new Status(IStatus.ERROR, RooCoreActivator.PLUGIN_ID, String.format(
					"Roo installation '%s' points to non-existing path '%s'.", getName(), getHome()));
		}
		if (!home.isDirectory()) {
			return new Status(IStatus.ERROR, RooCoreActivator.PLUGIN_ID, String.format(
					"Roo installation '%s' does not point to a directory '%s'.", getName(), getHome()));
		}
		// Check if Roo home is writable
		if (!home.canWrite()) {
			return new Status(IStatus.ERROR, RooCoreActivator.PLUGIN_ID, String.format(
					"The Roo installation directory of Roo installation '%s' is not writable.", getName()));
		}
		// Check that Roo directory structure exists
		File bin = new File(home, "bin");
		File bundle = new File(home, "bundle");
		File conf = new File(home, "conf");
		File configProperties = new File(conf, "config.properties");
		if (!bin.exists()) {
			return new Status(IStatus.ERROR, RooCoreActivator.PLUGIN_ID, String.format(
					"Roo installation '%s' is missing the 'bin' folder.", getName()));
		}
		if (!bundle.exists()) {
			return new Status(IStatus.ERROR, RooCoreActivator.PLUGIN_ID, String.format(
					"Roo installation '%s' is missing the 'bundle' folder.", getName()));
		}
		if (!conf.exists()) {
			return new Status(IStatus.ERROR, RooCoreActivator.PLUGIN_ID, String.format(
					"Roo installation '%s' is missing the 'conf' folder.", getName()));
		}
		if (!configProperties.exists()) {
			return new Status(IStatus.ERROR, RooCoreActivator.PLUGIN_ID, String.format(
					"Roo installation '%s' is missing the 'conf/config.properties' file.", getName()));
		}
		if ("<UNKNOWN VERSION>".equals(getVersion())) {
			return new Status(IStatus.ERROR, RooCoreActivator.PLUGIN_ID, String.format(
					"Cannot determine Roo version for installation '%s'.", getName()));
		}
		String rawVersion = getVersion();
		int i = rawVersion.indexOf(' ');
		Version version = Version.parseVersion(rawVersion.substring(0, i));
		VersionRange range = new VersionRange(IRooInstall.SUPPORTED_VERSION);
		if (!range.isIncluded(version)) {
			return new Status(IStatus.ERROR, RooCoreActivator.PLUGIN_ID, String.format(
					"Roo installation '%s' of version '%s' is not supported.", getName(), getVersion()));
		}
		return Status.OK_STATUS;
	}

	private void addBootstrapJar(Set<URL> urls, File rooHome, String directory) {
		File dir = new File(rooHome.getAbsolutePath(), directory);
		if (dir.exists()) {
			for (File jar : dir.listFiles()) {
				if (jar.isFile() && jar.getName().endsWith(".jar") && !jar.getName().contains("jline")) {
					try {
						urls.add(jar.toURI().toURL());
					}
					catch (MalformedURLException e) {
					}
				}
			}
		}
	}

	private String versionInfo() {

		ClassLoader loader = new URLClassLoader(getClasspath());

		// Try to determine the bundle version
		String bundleVersion = null;
		String gitCommitHash = null;
		JarFile jarFile = null;
		try {
			URL classContainer = loader.loadClass("org.springframework.roo.shell.AbstractShell").getProtectionDomain()
					.getCodeSource().getLocation();
			if (classContainer.toString().endsWith(".jar")) {
				// Attempt to obtain the "Bundle-Version" version from the
				// manifest
				jarFile = new JarFile(new File(classContainer.toURI()), false);
				ZipEntry manifestEntry = jarFile.getEntry("META-INF/MANIFEST.MF");
				Manifest manifest = new Manifest(jarFile.getInputStream(manifestEntry));
				bundleVersion = manifest.getMainAttributes().getValue("Bundle-Version");
				gitCommitHash = manifest.getMainAttributes().getValue("Git-Commit-Hash");
			}
		}
		catch (Exception ignoreAndMoveOn) {
		}
		finally {
			if (jarFile != null) {
				try {
					jarFile.close();
				}
				catch (IOException e) {
				}
			}
		}

		StringBuilder sb = new StringBuilder();

		if (bundleVersion != null) {
			sb.append(bundleVersion);
		}

		if (gitCommitHash != null) {
			if (sb.length() > 0) {
				sb.append(" "); // to separate from version
			}
			sb.append("[rev ");
			sb.append(gitCommitHash.substring(0, 7));
			sb.append("]");
		}

		if (sb.length() == 0) {
			sb.append("<UNKNOWN VERSION>");
		}

		return sb.toString();
	}
}
