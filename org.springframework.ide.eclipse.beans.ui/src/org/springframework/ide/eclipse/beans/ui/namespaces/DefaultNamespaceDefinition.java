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
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.ui.BeansUIImages;
import org.springframework.ide.eclipse.core.SpringCorePreferences;

/**
 * Default implementation of {@link INamespaceDefinition}.
 * @author Christian Dupuis
 * @since 2.0
 */
@SuppressWarnings("deprecation")
public class DefaultNamespaceDefinition implements INamespaceDefinition {

	private final String prefix;

	private final String uri;

	private final String defaultLocation;

	private final Image image;

	private Set<String> locations = new HashSet<String>();

	public DefaultNamespaceDefinition(final String prefix, final String uri, final String defaultLocation,
			final Image image) {
		this.prefix = prefix;
		this.uri = uri;
		this.defaultLocation = defaultLocation;
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
		String version = null;
		if (hasProjectSpecificOptions(resource)) {
			SpringCorePreferences prefs = SpringCorePreferences.getProjectPreferences(resource.getProject(),
					BeansCorePlugin.PLUGIN_ID);
			version = prefs.getString(BeansCorePlugin.NAMESPACE_DEFAULT_VERSION_PREFERENCE_ID + getNamespaceURI(), "");
		}
		else {
			Preferences prefs = BeansCorePlugin.getDefault().getPluginPreferences();
			version = prefs.getString(BeansCorePlugin.NAMESPACE_DEFAULT_VERSION_PREFERENCE_ID + getNamespaceURI());
		}
		if ("".equals(version) || version == null) {
			return defaultLocation;
		}
		else {
			return version;
		}
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
	
}
