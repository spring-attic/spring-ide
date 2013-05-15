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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springsource.ide.eclipse.commons.content.core.ContentLocation;
import org.springsource.ide.eclipse.commons.content.core.ContentPlugin;

/**
 * Points to simple project templates inside the Wizard bundle.
 */
public class BundleTemplateLocation implements ContentLocation {

	public static final String DESCRIPTOR_LOCATION = "/template/descriptor.xml";

	public BundleTemplateLocation() {

	}

	public InputStream streamFromContentLocation(String relativeURL) throws CoreException {

		try {
			String relativePath = getRelativeFilePath(relativeURL);
			if (relativePath != null) {
				IPath path = new Path(relativePath);
				return FileLocator.openStream(getBundle(), path, false);
			}

		}
		catch (IOException e) {
			String message = NLS.bind("Unable to find resource {0} in bundle {1}", relativeURL, getBundle()
					.getLocation());
			throw new CoreException(new Status(IStatus.ERROR, ContentPlugin.PLUGIN_ID, message, e));
		}

		return null;
	}

	/**
	 * Given a file URL, it will return the relative path if the location is a
	 * valid URI
	 * @param location
	 * @return
	 */
	protected String getRelativeFilePath(String location) throws CoreException {
		try {
			URI uri = new URI(location);
			if (uri.getScheme() == null || uri.getScheme().startsWith("file")) {
				return uri.getPath();
			}

		}
		catch (URISyntaxException e) {
			throw new CoreException(new Status(IStatus.ERROR, ContentPlugin.PLUGIN_ID,
					"Failed to load descriptors from location due to malformed URI: " + location, e));

		}

		return null;
	}

	public File getContentLocationFile() {

		URL url = FileLocator.find(getBundle(), new Path(DESCRIPTOR_LOCATION), null);

		if (url != null) {
			try {
				url = FileLocator.toFileURL(url);

				return new File(url.getPath());
			}
			catch (IOException e) {
				ContentPlugin
						.getDefault()
						.getLog()
						.log(new Status(
								IStatus.ERROR,
								ContentPlugin.PLUGIN_ID,
								"Failed to load descriptors from location due to I/O exception: " + DESCRIPTOR_LOCATION,
								e));
			}
		}
		return null;

	}

	protected Bundle getBundle() {
		return WizardPlugin.getDefault().getBundle();
	}

	public String getContentLocation() {
		return DESCRIPTOR_LOCATION;
	}

}
