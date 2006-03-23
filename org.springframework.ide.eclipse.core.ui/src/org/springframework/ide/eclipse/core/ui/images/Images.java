/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ide.eclipse.core.ui.images;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.springframework.ide.eclipse.core.ui.SpringCoreUIPlugin;
import org.springframework.ide.eclipse.core.ui.utils.PluginUtils;
import org.springframework.ide.eclipse.core.ui.wizards.StatusInfo;

/**
 * An utility class to manage the images of a plugin
 * 
 * @author Pierre-Antoine Grégoire
 */
public class Images {

	/** A table of all the <code>ImageDescriptor</code>s. */
	private static Map imageDescriptors;

	/** The image registry containing <code>Image</code>s. */
	private static ImageRegistry imageRegistry;

	/**
	 * Returns the <code>Image<code> identified by the given key,
	 * or <code>null</code> if it does not exist.
	 */
	public Image getImage(String key) {
		return getImageRegistry().get(key);
	}

	/**
	 * Returns the <code>Image<code> identified by the given key,
	 * or <code>null</code> if it does not exist.
	 */
	public ImageDescriptor getImageDescriptor(String key) {
		return (ImageDescriptor) imageDescriptors.get(key);
	}

	/*
	 * Helper method to access the image registry from the JavaPlugin class.
	 */
	private ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
			for (Iterator iter = imageDescriptors.keySet().iterator(); iter.hasNext();) {
				String key = (String) iter.next();
				imageRegistry.put(key, (ImageDescriptor) imageDescriptors.get(key));
			}
		}
		return imageRegistry;
	}

	public ImageDescriptor addImage(AbstractUIPlugin plugin, String path, String key) {
		try {
			URL baseURL = PluginUtils.getPluginInstallationURL(plugin);
			ImageDescriptor result = ImageDescriptor.createFromURL(makeIconFileURL(baseURL, path));
			if (imageDescriptors == null) {
				imageDescriptors = new HashMap();
			}
			imageDescriptors.put(key, result);
			if (imageRegistry != null) {
				SpringCoreUIPlugin.getDefault().getLog().log(new StatusInfo(IStatus.OK, "Image registry already defined."));
			}
			return result;
		} catch (MalformedURLException e) {
			SpringCoreUIPlugin.getDefault().getLog().log(new StatusInfo(IStatus.ERROR, e.getMessage()));
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private URL makeIconFileURL(URL baseURL, String name) throws MalformedURLException {
		return new URL(baseURL, name);
	}
}