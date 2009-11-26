/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.namespaces;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarEntryFile;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.core.SpringCorePreferences;
import org.springframework.ide.eclipse.core.java.JdtUtils;

/**
 * Default implementation of {@link INamespaceDefinition}.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings( { "deprecation", "restriction" })
public class DefaultNamespaceDefinition implements INamespaceDefinition {

	private Pattern versionPattern = Pattern.compile(".*-([0-9,.]*)\\.xsd");

	private final String prefix;

	private final String uri;

	private final String defaultLocation;

	private final Image image;

	private Set<String> locations = new HashSet<String>();

	private Properties uriMapping = new Properties();

	public DefaultNamespaceDefinition(String prefix, String uri, String defaultLocation, Image image) {
		this(prefix, uri, defaultLocation, new Properties(), image);
	}

	public DefaultNamespaceDefinition(String prefix, String uri, String defaultLocation,
			Properties namespaceDefinition, Image image) {
		if (prefix != null) {
			this.prefix = prefix;
		}
		else {
			int ix = uri.lastIndexOf('/');
			this.prefix = uri.substring(ix + 1);
		}
		this.uri = uri;
		this.defaultLocation = defaultLocation;
		this.uriMapping = namespaceDefinition;
		this.image = image;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNamespacePrefix() {
		return prefix;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getNamespaceURI() {
		return uri;
	}

	public String getDefaultSchemaLocation() {
		return getDefaultSchemaLocation(null);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDefaultSchemaLocation(IResource resource) {
		String location = null;
		if (hasProjectSpecificOptions(resource)) {
			SpringCorePreferences prefs = SpringCorePreferences.getProjectPreferences(resource.getProject(),
					BeansCorePlugin.PLUGIN_ID);
			if (prefs.getBoolean(BeansCorePlugin.NAMESPACE_DEFAULT_FROM_CLASSPATH_ID, true)) {
				location = getDefaultSchemaLocationFromClasspath(resource);
			}
			if ("".equals(location) || location == null) {
				location = prefs.getString(BeansCorePlugin.NAMESPACE_DEFAULT_VERSION_PREFERENCE_ID + getNamespaceURI(),
						"");
			}
		}
		else {
			Preferences prefs = BeansCorePlugin.getDefault().getPluginPreferences();
			if (prefs.getBoolean(BeansCorePlugin.NAMESPACE_DEFAULT_FROM_CLASSPATH_ID)) {
				location = getDefaultSchemaLocationFromClasspath(resource);
			}
			if ("".equals(location) || location == null) {
				location = prefs.getString(BeansCorePlugin.NAMESPACE_DEFAULT_VERSION_PREFERENCE_ID + getNamespaceURI());
			}
		}
		if ("".equals(location) || location == null) {
			return defaultLocation;
		}
		else {
			return location;
		}
	}

	private String getDefaultSchemaLocationFromClasspath(IResource resource) {
		IJavaProject jp = JdtUtils.getJavaProject(resource);
		Set<String> existingLocations = new HashSet<String>();
		try {
			for (Map.Entry<Object, Object> entry : uriMapping.entrySet()) {

				// Check that we are looking for relevant entries only; e.g. util, tool and beans are in the same
				// package
				if (((String) entry.getKey()).startsWith(uri)) {
					String fileLocation = (String) entry.getValue();

					// Get the package name from the location
					String packageName = "";
					int ix = fileLocation.lastIndexOf('/');
					if (ix > 0) {
						packageName = fileLocation.substring(0, ix).replace('/', '.');
					}

					// Search in all project packages
					for (IPackageFragmentRoot root : jp.getPackageFragmentRoots()) {
						IPackageFragment pf = root.getPackageFragment(packageName);
						if (pf.exists()) {
							for (Object obj : pf.getNonJavaResources()) {
								// Entry is coming from a JAR file on the classpath
								if (obj instanceof JarEntryFile
										&& ("/" + fileLocation).equals((((JarEntryFile) obj).getFullPath().toString()))) {
									existingLocations.add((String) entry.getKey());
								}
								// Entry is coming from the local project
								else if (obj instanceof IFile
										&& fileLocation.equals(pf.getElementName().replace('.', '/') + "/"
												+ ((IFile) obj).getName())) {
									existingLocations.add((String) entry.getKey());
								}
							}
						}
					}
				}
			}
		}
		catch (JavaModelException e) {
			// We have a safe fall-back
		}

		// Search for the highest version among the existing resources
		String highestLocation = null;
		Version version = Version.MINIMUM_VERSION;
		for (String location : existingLocations) {
			Version tempVersion = Version.MINIMUM_VERSION;
			Matcher matcher = versionPattern.matcher(location);
			if (matcher.matches()) {
				tempVersion = new Version(matcher.group(1));
			}
			if (tempVersion.compareTo(version) >= 0) {
				version = tempVersion;
				highestLocation = location;
			}
		}
		return highestLocation;
	}

	/**
	 * {@inheritDoc}
	 */
	public Image getNamespaceImage() {
		if (image != null) {
			return image;
		}
		return BeansUIImages.getImage(BeansUIImages.IMG_OBJS_XSD);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<String> getSchemaLocations() {
		return locations;
	}

	public void addSchemaLocation(String location) {
		locations.add(location);
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		return prefix.hashCode() ^ uri.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(Object obj) {
		if (obj instanceof DefaultNamespaceDefinition) {
			DefaultNamespaceDefinition o = (DefaultNamespaceDefinition) obj;
			return o.prefix.equals(prefix) && o.uri.equals(uri);
		}
		return false;
	}

	private boolean hasProjectSpecificOptions(IResource resource) {
		if (resource == null) {
			return false;
		}
		return SpringCorePreferences.getProjectPreferences(resource.getProject(), BeansCorePlugin.PLUGIN_ID)
				.getBoolean(BeansCorePlugin.NAMESPACE_VERSION_PROJECT_PREFERENCE_ID, false);
	}

	static class Version implements Comparable<Version> {

		private static final String MINIMUM_VERSION_STRING = "0";

		public static final Version MINIMUM_VERSION = new Version(MINIMUM_VERSION_STRING);

		private final org.osgi.framework.Version version;

		public Version(String v) {
			org.osgi.framework.Version tempVersion = null;
			try {
				tempVersion = org.osgi.framework.Version.parseVersion(v);
			}
			catch (Exception e) {
				// make sure that we don't crash on any new version numbers format that we don't support
				BeansCorePlugin.log("Cannot convert schema vesion", e);
				tempVersion = org.osgi.framework.Version.parseVersion(MINIMUM_VERSION_STRING);
			}
			this.version = tempVersion;
		}

		public int compareTo(Version v2) {
			return this.version.compareTo(v2.version);
		}
	}

}
