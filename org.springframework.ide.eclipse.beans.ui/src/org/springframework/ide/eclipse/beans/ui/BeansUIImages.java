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

package org.springframework.ide.eclipse.beans.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

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
 */
public class BeansUIImages {

	private static final String ICON_PATH_PREFIX = "icons/full/";
	private static final String NAME_PREFIX = BeansUIPlugin.PLUGIN_ID + '.';
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();

	/* Declare Common paths */
	private static URL ICON_BASE_URL = null;

	static {
		try {
			ICON_BASE_URL = new URL(
						  BeansUIPlugin.getDefault().getBundle().getEntry("/"),
						  ICON_PATH_PREFIX);
		} catch (MalformedURLException e) {
			BeansUIPlugin.log(e);
		}
	}
	
	/** A table of all the <code>ImageDescriptor</code>s. */
	private static Map imageDescriptors;

	/**  The image registry containing <code>Image</code>s. */
	private static ImageRegistry imageRegistry;

	/*
	 * Available cached Images in the Java plugin image registry.
	 */
	public static final String IMG_OBJS_PROJECT = NAME_PREFIX + "project_obj.gif";
	public static final String IMG_OBJS_CONFIG = NAME_PREFIX + "config_obj.gif";
	public static final String IMG_OBJS_CONFIG_SET = NAME_PREFIX + "configset_obj.gif";
	public static final String IMG_OBJS_ROOT_BEAN = NAME_PREFIX + "rootbean_obj.gif";
	public static final String IMG_OBJS_CHILD_BEAN = NAME_PREFIX + "childbean_obj.gif";
	public static final String IMG_OBJS_BEAN_REF = NAME_PREFIX + "beanref_obj.gif";
	public static final String IMG_OBJS_CONSTRUCTOR = NAME_PREFIX + "constructor_obj.gif";
	public static final String IMG_OBJS_PROPERTY = NAME_PREFIX + "property_obj.gif";
	public static final String IMG_OBJS_SPRING = NAME_PREFIX + "spring_obj.gif";
	public static final String IMG_OBJS_ERROR = NAME_PREFIX + "error_obj.gif";

	// Use IPath and toOSString to build the names to ensure they have the slashes correct
//	private final static String CTOOL = "ctool16/"; //basic colors - size 16x16
//	private final static String LOCALTOOL = "clcl16/"; //basic colors - size 16x16
//	private final static String DLCL = "dlcl16/"; //disabled - size 16x16
//	private final static String ELCL = "elcl16/"; //enabled - size 16x16
	private final static String OBJECT = "obj16/"; //basic colors - size 16x16
//	private final static String WIZBAN = "wizban/"; //basic colors - size 16x16
	private final static String OVR = "ovr16/"; //basic colors - size 7x8
//	private final static String VIEW= "cview16/"; // views

	public static final ImageDescriptor DESC_OBJS_PROJECT = createManaged(OBJECT, IMG_OBJS_PROJECT);
	public static final ImageDescriptor DESC_OBJS_CONFIG = createManaged(OBJECT, IMG_OBJS_CONFIG);
	public static final ImageDescriptor DESC_OBJS_CONFIG_SET = createManaged(OBJECT, IMG_OBJS_CONFIG_SET);
	public static final ImageDescriptor DESC_OBJS_ROOT_BEAN = createManaged(OBJECT, IMG_OBJS_ROOT_BEAN);
	public static final ImageDescriptor DESC_OBJS_CHILD_BEAN = createManaged(OBJECT, IMG_OBJS_CHILD_BEAN);
	public static final ImageDescriptor DESC_OBJS_BEAN_REF = createManaged(OBJECT, IMG_OBJS_BEAN_REF);
	public static final ImageDescriptor DESC_OBJS_CONSTRUCTOR = createManaged(OBJECT, IMG_OBJS_CONSTRUCTOR);
	public static final ImageDescriptor DESC_OBJS_PROPERTY = createManaged(OBJECT, IMG_OBJS_PROPERTY);
	public static final ImageDescriptor DESC_OBJS_SPRING = createManaged(OBJECT, IMG_OBJS_SPRING);
	public static final ImageDescriptor DESC_OBJS_ERROR = createManaged(OBJECT, IMG_OBJS_ERROR);

	public static final ImageDescriptor DESC_OVR_SPRING = create(OVR, "spring_ovr.gif");
	public static final ImageDescriptor DESC_OVR_ERROR = create(OVR, "error_ovr.gif");
	public static final ImageDescriptor DESC_OVR_WARNING = create(OVR, "warning_ovr.gif");
	public static final ImageDescriptor DESC_OVR_PROTOTYPE = create(OVR, "prototype_ovr.gif");
	public static final ImageDescriptor DESC_OVR_EXTERNAL = create(OVR, "external_ovr.gif");
	public static final ImageDescriptor DESC_OVR_ABSTRACT = create(OVR, "abstract_ovr.gif");
	public static final ImageDescriptor DESC_OVR_NO_CLASS = create(OVR, "no_class_ovr.gif");

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
	public static void setToolImageDescriptors(IAction action,
											   String iconName) {
		setImageDescriptors(action, "tool16", iconName);
	}

	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to an
	 * action. The actions are retrieved from the *lcl16 folders.
	 */
	public static void setLocalImageDescriptors(IAction action,
												String iconName) {
		setImageDescriptors(action, "lcl16", iconName);
	}
	
	/*
	 * Helper method to access the image registry from the JavaPlugin class.
	 */
	/* package */ static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
			for (Iterator iter = imageDescriptors.keySet().iterator();
															 iter.hasNext(); ) {
				String key = (String) iter.next();
				imageRegistry.put(key, (ImageDescriptor)
													 imageDescriptors.get(key));
			}
			imageDescriptors = null;
		}
		return imageRegistry;
	}

	//---- Helper methods to access icons on the file system -------------------

	private static void setImageDescriptors(IAction action, String type,
											String relPath) {
		try {
			ImageDescriptor id = ImageDescriptor.createFromURL(makeIconFileURL(
														  "d" + type, relPath));
			if (id != null) {
				action.setDisabledImageDescriptor(id);
			}
		} catch (MalformedURLException e) {
			BeansUIPlugin.log(e);
		}
/*
		try {
			ImageDescriptor id = ImageDescriptor.createFromURL(makeIconFileURL(
														  "c" + type, relPath));
			if (id != null) {
				action.setHoverImageDescriptor(id);
			}
		} catch (MalformedURLException e) {
			BeansUIPlugin.log(e);
		}
*/
		action.setImageDescriptor(create("e" + type, relPath));
	}
	
	private static ImageDescriptor createManaged(String prefix, String name) {
		try {
			ImageDescriptor result = ImageDescriptor.createFromURL(
				   makeIconFileURL(prefix, name.substring(NAME_PREFIX_LENGTH)));
			if (imageDescriptors == null) {
				imageDescriptors = new HashMap();
			}
			imageDescriptors.put(name, result);
			if (imageRegistry != null) {
				BeansUIPlugin.log("Image registry already defined", null);
			}
			return result;
		} catch (MalformedURLException e) {
			BeansUIPlugin.log(e);
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
		} catch (MalformedURLException e) {
			BeansUIPlugin.log(e);
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
