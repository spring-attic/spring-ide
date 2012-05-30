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
package org.springframework.ide.eclipse.metadata;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * The images provided by the Spring Beans UI plugin.
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
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @see org.eclipse.jface.resource.ImageRegistry
 */
public class MetadataUIImages {

	private static final String ICON_PATH_PREFIX = "icons/full/"; //$NON-NLS-1$
	private static final String NAME_PREFIX = MetadataPlugin.PLUGIN_ID + '.';
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();

	/* Declare Common paths */
	private static URL ICON_BASE_URL = null;

	static {
		try {
			ICON_BASE_URL = new URL(MetadataPlugin.getDefault().getBundle()
					.getEntry("/"), ICON_PATH_PREFIX); //$NON-NLS-1$
		} catch (MalformedURLException e) {
		}
	}
	
	/** A table of all the <code>ImageDescriptor</code>s. */
	private static Map<String, ImageDescriptor> imageDescriptors;

	/*
	 * Available cached Images in the Spring Beans UI plugin image registry.
	 */
    public static final String IMG_OBJS_ASPECT = NAME_PREFIX + "aspect_obj.gif"; //$NON-NLS-1$
    public static final String IMG_OBJS_AROUND_ADVICE = NAME_PREFIX + "around_advice_obj.gif"; //$NON-NLS-1$
    public static final String IMG_OBJS_BEFORE_ADVICE = NAME_PREFIX + "before_advice_obj.gif"; //$NON-NLS-1$
    public static final String IMG_OBJS_AFTER_ADVICE = NAME_PREFIX + "after_advice_obj.gif"; //$NON-NLS-1$
    public static final String IMG_OBJS_DECLARE_PARENTS = NAME_PREFIX + "dec_parents_obj.gif"; //$NON-NLS-1$
    public static final String IMG_OBJS_DECLARE_ERROR = NAME_PREFIX + "dec_error_obj.gif"; //$NON-NLS-1$
    public static final String IMG_OBJS_DECLARE_WARNING = NAME_PREFIX + "dec_warning_obj.gif"; //$NON-NLS-1$
    public static final String IMG_OBJS_DECLARE_ANNOTATION = NAME_PREFIX + "dec_annotation_obj.gif"; //$NON-NLS-1$
    public static final String IMG_OBJS_POINTCUT = NAME_PREFIX + "pointcut_pub_obj.gif"; //$NON-NLS-1$
    public static final String IMG_OBJS_TX = NAME_PREFIX + "tx_obj.gif"; //$NON-NLS-1$
    public static final String IMG_OBJS_ANNOTATION = NAME_PREFIX + "annotation_obj.gif"; //$NON-NLS-1$
    public static final String IMG_OBJS_REQUEST_MAPPING = NAME_PREFIX + "request_mapping_obj.gif"; //$NON-NLS-1$
    public static final String IMG_OBJS_ANNOTATION_BEAN = NAME_PREFIX + "configuration_bean_obj.gif"; //$NON-NLS-1$
    public static final String IMG_OBJS_BREAKPOINT = NAME_PREFIX + "brkp_obj.gif"; //$NON-NLS-1$
    public static final String IMG_OBJS_JAVA_FILE = NAME_PREFIX + "jcu_obj.gif"; //$NON-NLS-1$
    public static final String IMG_OBJS_BROWSER = NAME_PREFIX + "internal_browser.gif"; //$NON-NLS-1$
    public static final String IMG_OBJS_ORIENTATION_HORIZONTAL = NAME_PREFIX + "th_horizontal.gif"; //$NON-NLS-1$
    public static final String IMG_OBJS_ORIENTATION_VERTICAL = NAME_PREFIX + "th_vertical.gif"; //$NON-NLS-1$
    
	// Use IPath and toOSString to build the names to ensure they have the slashes correct
	private final static String OBJECT = "obj16/"; //basic colors - size 16x16 //$NON-NLS-1$

	public static final ImageDescriptor DESC_OBJS_ASPECT = createManaged(OBJECT, IMG_OBJS_ASPECT);
	public static final ImageDescriptor DESC_OBJS_AROUND_ADVICE = createManaged(OBJECT, IMG_OBJS_AROUND_ADVICE);
	public static final ImageDescriptor DESC_OBJS_BEFORE_ADVICE = createManaged(OBJECT, IMG_OBJS_BEFORE_ADVICE);
	public static final ImageDescriptor DESC_OBJS_AFTER_ADVICE = createManaged(OBJECT, IMG_OBJS_AFTER_ADVICE);
	public static final ImageDescriptor DESC_OBJS_DECLARE_PARENTS = createManaged(OBJECT, IMG_OBJS_DECLARE_PARENTS);
	public static final ImageDescriptor DESC_OBJS_DECLARE_WARNING = createManaged(OBJECT, IMG_OBJS_DECLARE_WARNING);
	public static final ImageDescriptor DESC_OBJS_DECLARE_ERROR = createManaged(OBJECT, IMG_OBJS_DECLARE_ERROR);
	public static final ImageDescriptor DESC_OBJS_DECLARE_ANNOTATION = createManaged(OBJECT, IMG_OBJS_DECLARE_ANNOTATION);
	public static final ImageDescriptor DESC_OBJS_POINTCUT = createManaged(OBJECT, IMG_OBJS_POINTCUT);
	public static final ImageDescriptor DESC_OBJS_TX = createManaged(OBJECT, IMG_OBJS_TX);
	public static final ImageDescriptor DESC_OBJS_ANNOTATION = createManaged(OBJECT, IMG_OBJS_ANNOTATION);
	public static final ImageDescriptor DESC_OBJS_REQUEST_MAPPING = createManaged(OBJECT, IMG_OBJS_REQUEST_MAPPING);
	public static final ImageDescriptor DESC_OBJS_ANNOTATION_BEAN = createManaged(OBJECT, IMG_OBJS_ANNOTATION_BEAN);
	public static final ImageDescriptor DESC_OBJS_BREAKPOINT = createManaged(OBJECT, IMG_OBJS_BREAKPOINT);
	public static final ImageDescriptor DESC_OBJS_JAVA_FILE = createManaged(OBJECT, IMG_OBJS_JAVA_FILE);
	public static final ImageDescriptor DESC_OBJS_BROWSER = createManaged(OBJECT, IMG_OBJS_BROWSER);
	public static final ImageDescriptor DESC_OBJS_ORIENTATION_HORIZONTAL = createManaged(OBJECT, IMG_OBJS_ORIENTATION_HORIZONTAL);
	public static final ImageDescriptor DESC_OBJS_ORIENTATION_VERTICAL = createManaged(OBJECT, IMG_OBJS_ORIENTATION_VERTICAL);

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
		return MetadataPlugin.getDefault().getImageRegistry().get(key);
	}
	
	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to
	 * an action. The actions are retrieved from the *tool16 folders.
	 */
	public static void setToolImageDescriptors(IAction action,
			String iconName) {
		setImageDescriptors(action, "tool16", iconName); //$NON-NLS-1$
	}

	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to
	 * an action. The actions are retrieved from the *lcl16 folders.
	 */
	public static void setLocalImageDescriptors(IAction action,
			String iconName) {
		setImageDescriptors(action, "lcl16", iconName); //$NON-NLS-1$
	}

	//---- Helper methods to access icons on the file system -------------------

	private static void setImageDescriptors(IAction action, String type,
			String relPath) {
		action.setImageDescriptor(create("e" + type, relPath)); //$NON-NLS-1$
		try {
			ImageDescriptor id = ImageDescriptor.createFromURL(makeIconFileURL(
					"d" + type, relPath)); //$NON-NLS-1$
			if (id != null) {
				action.setDisabledImageDescriptor(id);
			}
		} catch (MalformedURLException e) {
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
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix,
					name));
		} catch (MalformedURLException e) {
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
