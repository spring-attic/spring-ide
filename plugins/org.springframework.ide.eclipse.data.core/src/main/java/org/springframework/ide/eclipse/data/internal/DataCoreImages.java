/*******************************************************************************
 * Copyright (c) 2008, 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.data.internal;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * The images provided by the Spring Data Support plugin.
 * Initialize the image registry by declaring all of the required
 * graphics. This involves creating JFace image descriptors describing
 * how to create/find the image should it be needed.
 * The image is not actually allocated until requested.
 * Prefix conventions
 * Wizard Banners			WIZBAN_
 * Preference Banners		PREF_BAN_
 * Property Page Banners	PROPBAN_
 * Color toolbar			CTOOL_
 * Enable toolbar			ETOOL_
 * Disable toolbar			DTOOL_
 * Local enabled toolbar	ELCL_
 * Local Disable toolbar	DLCL_
 * Object large			OBJL_
 * Object small			OBJS_
 * View 					VIEW_
 * Product images			PROD_
 * Misc images				MISC_
 * Where are the images?
 * The images (typically gifs) are found in the same location as this
 * plugin class. This may mean the same package directory as the
 * package holding this class. The images are declared using
 * <code>this.getClass()</code> to ensure they are looked up via
 * this plugin class.
 * <p>
 * This class has been copied from org.springframework.ide.eclipse.beans.core plug-in.
 * </p>
 * @author Christian Dupuis
 * @author Tomasz Zarna
 * @see org.eclipse.jface.resource.ImageRegistry
 * @see org.springframework.ide.eclipse.beans.core.BeansCoreImages
 */
public class DataCoreImages {

	private static final String ICON_PATH_PREFIX = "icons/full/";
	private static final String NAME_PREFIX = DataCorePlugin.PLUGIN_ID + '.';
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();

	/* Declare Common paths */
	private static URL ICON_BASE_URL = null;

	static {
		try {
			ICON_BASE_URL = new URL(DataCorePlugin.getDefault().getBundle()
					.getEntry("/"), ICON_PATH_PREFIX);
		} catch (MalformedURLException e) {
			DataCorePlugin.log(e);
		}
	}
	
	/** A table of all the <code>ImageDescriptor</code>s. */
	private static Map<String, ImageDescriptor> imageDescriptors;

	/*
	 * Available cached Images in the Spring Beans UI plugin image registry.
	 */
	public static final String IMG_OBJS_KEY = NAME_PREFIX + "key_obj.gif";

	// Use IPath and toOSString to build the names to ensure they have the slashes correct
	private final static String OBJECT = "obj16/"; //basic colors - size 16x16
	//private final static String WIZBAN = "wizban/"; //basic colors - size 16x16
	//private final static String OVR = "ovr16/"; //basic colors - size 7x8

	public static final ImageDescriptor DESC_OBJS_KEY = createManaged(OBJECT, IMG_OBJS_KEY);

	/*
	 * Helper method to initialize the image registry from the BeansUIPlugin
	 * class.
	 */
	/* package */ static void initializeImageRegistry(ImageRegistry registry) {
		for (String key : imageDescriptors.keySet()) {
			registry.put(key, imageDescriptors.get(key));
		}
	}

	/**
	 * Returns the {@link Image} identified by the given key,
	 * or <code>null</code> if it does not exist.
	 */
	public static Image getImage(String key) {
		return DataCorePlugin.getDefault().getImageRegistry().get(key);
	}
	
	//---- Helper methods to access icons on the file system -------------------

	private static ImageDescriptor createManaged(String prefix, String name) {
		try {
			ImageDescriptor result = ImageDescriptor.createFromURL(
				   makeIconFileURL(prefix, name.substring(NAME_PREFIX_LENGTH)));
			if (imageDescriptors == null) {
				imageDescriptors = new HashMap<String, ImageDescriptor>();
			}
			imageDescriptors.put(name, result);
			return result;
		} catch (MalformedURLException e) {
			DataCorePlugin.log(e);
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static URL makeIconFileURL(String prefix, String name)
			throws MalformedURLException {
		if (ICON_BASE_URL == null) {
			throw new MalformedURLException();
		}

		StringBuffer buffer = new StringBuffer(prefix);
		buffer.append('/').append(name);
		return new URL(ICON_BASE_URL, buffer.toString());
	}
}
