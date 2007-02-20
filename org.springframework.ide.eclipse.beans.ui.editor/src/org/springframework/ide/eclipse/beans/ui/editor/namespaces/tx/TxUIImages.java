/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.ide.eclipse.beans.ui.editor.namespaces.tx;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.beans.ui.editor.Activator;

/**
 * The images provided by the Spring UI plugin. Initialize the image registry by
 * declaring all of the required graphics. This involves creating JFace image
 * descriptors describing how to create/find the image should it be needed. The
 * image is not actually allocated until requested.
 * 
 * Prefix conventions Wizard Banners WIZBAN_ Preference Banners PREF_BAN_
 * Property Page Banners PROPBAN_ Color toolbar CTOOL_ Enable toolbar ETOOL_
 * Disable toolbar DTOOL_ Local enabled toolbar ELCL_ Local Disable toolbar
 * DLCL_ Object large OBJL_ Object small OBJS_ View VIEW_ Product images PROD_
 * Misc images MISC_
 * 
 * Where are the images? The images (typically gifs) are found in the same
 * location as this plugin class. This may mean the same package directory as
 * the package holding this class. The images are declared using
 * <code>this.getClass()</code> to ensure they are looked up via this plugin
 * class.
 * 
 * @see org.eclipse.jface.resource.ImageRegistry
 */
public class TxUIImages {

	private static final String ICON_PATH_PREFIX = "";

	private static final String NAME_PREFIX = Activator.PLUGIN_ID + '.';

	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();
    
    private static final String STRUCTURE = "icons/full/obj16/";

	/* Declare Common paths */
	private static URL ICON_BASE_URL = null;

	static {
		try {
			ICON_BASE_URL = new URL(Activator.getDefault().getBundle()
					.getEntry("/"), ICON_PATH_PREFIX);
		} catch (MalformedURLException e) {
			Activator.log(e);
		}
	}

	/** A table of all the <code>ImageDescriptor</code>s. */
	private static Map<String, ImageDescriptor> imageDescriptors;

	/** The image registry containing <code>Image</code>s. */
	private static ImageRegistry imageRegistry;

	/*
	 * Available cached Images in the Java plugin image registry.
	 */
	public static final String IMG_OBJS_TX = NAME_PREFIX + "tx_obj.gif";

	public static final ImageDescriptor DESC_OBJS_TX = createManaged(
            STRUCTURE, IMG_OBJS_TX);

	/**
	 * Returns the <code>Image<code> identified by the given key,
	 * or <code>null</code> if it does not exist.
	 */
	public static Image getImage(String key) {
		return getImageRegistry().get(key);
	}

	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to an
	 * action. The actions are retrieved from the *tool16 folders.
	 */
	public static void setToolImageDescriptors(IAction action, String iconName) {
		setImageDescriptors(action, "tool16", iconName);
	}

	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to an
	 * action. The actions are retrieved from the *lcl16 folders.
	 */
	public static void setLocalImageDescriptors(IAction action, String iconName) {
		setImageDescriptors(action, "lcl16", iconName);
	}

	/*
	 * Helper method to access the image registry from the JavaPlugin class.
	 */
	/* package */static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
			for (Iterator<String> iter = imageDescriptors.keySet().iterator(); iter
					.hasNext();) {
				String key = (String) iter.next();
				imageRegistry.put(key, (ImageDescriptor) imageDescriptors
						.get(key));
			}
			imageDescriptors = null;
		}
		return imageRegistry;
	}

	// ---- Helper methods to access icons on the file system
	// -------------------

	private static void setImageDescriptors(IAction action, String type,
			String relPath) {
		try {
			ImageDescriptor id = ImageDescriptor.createFromURL(makeIconFileURL(
					"d" + type, relPath));
			if (id != null) {
				action.setDisabledImageDescriptor(id);
			}
		} catch (MalformedURLException e) {
			Activator.log(e);
		}
		/*
		 * try { ImageDescriptor id =
		 * ImageDescriptor.createFromURL(makeIconFileURL( "c" + type, relPath));
		 * if (id != null) { action.setHoverImageDescriptor(id); } } catch
		 * (MalformedURLException e) { BeansUIPlugin.log(e); }
		 */
		action.setImageDescriptor(create("e" + type, relPath));
	}

	private static ImageDescriptor createManaged(String prefix, String name) {
		try {
			ImageDescriptor result = ImageDescriptor
					.createFromURL(makeIconFileURL(prefix, name
							.substring(NAME_PREFIX_LENGTH)));
			if (imageDescriptors == null) {
				imageDescriptors = new HashMap<String, ImageDescriptor>();
			}
			imageDescriptors.put(name, result);
			if (imageRegistry != null) {
				Activator.log("Image registry already defined", null);
			}
			return result;
		} catch (MalformedURLException e) {
			Activator.log(e);
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
		} catch (MalformedURLException e) {
			Activator.log(e);
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static URL makeIconFileURL(String prefix, String name)
			throws MalformedURLException {
		if (ICON_BASE_URL == null) {
			throw new MalformedURLException();
		}

		StringBuffer buffer = new StringBuffer(prefix);
		buffer.append('/');
		buffer.append(name);
		return new URL(ICON_BASE_URL, buffer.toString());
	}
}
