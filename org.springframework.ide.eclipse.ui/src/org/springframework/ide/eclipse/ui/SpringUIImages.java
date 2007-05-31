/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

/**
 * The images provided by the Spring UI plugin.
 * Initialize the image registry by declaring all of the required
 * graphics. This involves creating JFace image descriptors describing
 * how to create/find the image should it be needed.
 * The image is not actually allocated until requested.
 *
 * Prefix conventions
 *		Wizard Banners			WIZBAN_
 *		Preference Banners		PREF_BAN_
 *		Property Page Banners	PROPBAN_
 *		Color toolbar			CTOOL_
 *		Enable toolbar			ETOOL_
 *		Disable toolbar			DTOOL_
 *		Local enabled toolbar	ELCL_
 *		Local Disable toolbar	DLCL_
 *		Object large			OBJL_
 *		Object small			OBJS_
 *		View 					VIEW_
 *		Product images			PROD_
 *		Misc images				MISC_
 *
 * Where are the images?
 *		The images (typically gifs) are found in the same location as this
 *		plugin class. This may mean the same package directory as the
 *		package holding this class. The images are declared using
 *		<code>this.getClass()</code> to ensure they are looked up via
 *		this plugin class.
 *
 * @see org.eclipse.jface.resource.ImageRegistry
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0
 */
public class SpringUIImages implements SpringUIImageFlags {

	private static final String ICON_PATH_PREFIX = "icons/full/";
	private static final String NAME_PREFIX = SpringUIPlugin.PLUGIN_ID + '.';
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();

	/* Declare Common paths */
	private static URL ICON_BASE_URL = null;

	static {
		try {
			ICON_BASE_URL = new URL(SpringUIPlugin.getDefault().getBundle()
					.getEntry("/"), ICON_PATH_PREFIX);
		} catch (MalformedURLException e) {
			SpringUIPlugin.log(e);
		}
	}
	
	/** A table of all the <code>ImageDescriptor</code>s. */
	private static Map<String, ImageDescriptor> imageDescriptors;

	/*
	 * Available cached Images in the Spring UI plugin image registry.
	 */
	public static final String IMG_OBJS_PROJECT = NAME_PREFIX + "project_obj.gif";
	public static final String IMG_OBJS_SPRING = NAME_PREFIX + "spring_obj.gif";
	public static final String IMG_OBJS_ERROR = NAME_PREFIX + "error_obj.gif";
	public static final String IMG_OBJS_RULE = NAME_PREFIX + "rule_obj.gif";
	
	public static final String IMG_WIZ_WORKINGSET = NAME_PREFIX + "workingset_wiz.png";

	// Use IPath and toOSString to build the names to ensure they have the slashes correct
	private final static String OBJECT = "obj16/"; //basic colors - size 16x16
	private final static String OVR = "ovr16/"; //basic colors - size 7x8
	private final static String WIZBAN = "wizban/"; //basic colors - size 16x16

	public static final ImageDescriptor DESC_OBJS_PROJECT = createManaged(OBJECT, IMG_OBJS_PROJECT);
	public static final ImageDescriptor DESC_OBJS_SPRING = createManaged(OBJECT, IMG_OBJS_SPRING);
	public static final ImageDescriptor DESC_OBJS_ERROR = createManaged(OBJECT, IMG_OBJS_ERROR);
	public static final ImageDescriptor DESC_OBJS_RULE = createManaged(OBJECT, IMG_OBJS_RULE);

	public static final ImageDescriptor DESC_OVR_SPRING = create(OVR, "spring_ovr.gif");
	public static final ImageDescriptor DESC_OVR_ERROR = create(OVR, "error_ovr.gif");
	public static final ImageDescriptor DESC_OVR_WARNING = create(OVR, "warning_ovr.gif");
	
	public static final ImageDescriptor DESC_WIZ_WORKINGSET = createManaged(WIZBAN, IMG_WIZ_WORKINGSET);

	/*
	 * Helper method to initialize the image registry from the Spring UI Plugin
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
		return SpringUIPlugin.getDefault().getImageRegistry().get(key);
	}


	public static Image getDecoratedImage(String baseImageKey, int flags) {
		return getDecoratedImage(getImage(baseImageKey), flags);
	}

	public static Image getDecoratedImage(Image baseImage, int flags) {
		ImageDescriptor descriptor = new SpringUICompositeImageDescriptor(
				baseImage, flags);
		return SpringUIPlugin.getImageDescriptorRegistry().get(descriptor);
	}

	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to
	 * an action. The actions are retrieved from the *tool16 folders.
	 */
	public static void setToolImageDescriptors(IAction action,
			String iconName) {
		setImageDescriptors(action, "tool16", iconName);
	}

	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to
	 * an action. The actions are retrieved from the *lcl16 folders.
	 */
	public static void setLocalImageDescriptors(IAction action,
			String iconName) {
		setImageDescriptors(action, "lcl16", iconName);
	}

	//---- Helper methods to access icons on the file system -------------------

	private static void setImageDescriptors(IAction action, String type,
			String relPath) {
		action.setImageDescriptor(create("e" + type, relPath));
		try {
			ImageDescriptor id = ImageDescriptor.createFromURL(makeIconFileURL(
					"d" + type, relPath));
			if (id != null) {
				action.setDisabledImageDescriptor(id);
			}
		} catch (MalformedURLException e) {
			SpringUIPlugin.log(e);
		}
	}

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
			SpringUIPlugin.log(e);
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix,
					name));
		} catch (MalformedURLException e) {
			SpringUIPlugin.log(e);
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

	private static class SpringUICompositeImageDescriptor extends
			AbstractCompositeImageDescriptor {

		public SpringUICompositeImageDescriptor(Image baseImage,
				int flags) {
			super(baseImage, flags);
		}

		@Override
		protected void drawOverlays() {
			int flags = getFlags();
			if ((flags & FLAG_WARNING) != 0) {
				ImageData data = DESC_OVR_WARNING.getImageData();
				drawImage(data, 0, getSize().y - data.height);
			}
			if ((flags & FLAG_ERROR) != 0) {
				ImageData data = DESC_OVR_ERROR.getImageData();
				drawImage(data, 0, getSize().y - data.height);
			}
		}
	}
}
