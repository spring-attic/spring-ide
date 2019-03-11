/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui.graph;

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
 * The images provided by the Spring UI plugin. Initialize the image registry by
 * declaring all of the required graphics. This involves creating JFace image
 * descriptors describing how to create/find the image should it be needed. The
 * image is not actually allocated until requested.
 * Prefix conventions Wizard Banners WIZBAN_ Preference Banners PREF_BAN_
 * Property Page Banners PROPBAN_ Color toolbar CTOOL_ Enable toolbar ETOOL_
 * Disable toolbar DTOOL_ Local enabled toolbar ELCL_ Local Disable toolbar
 * DLCL_ Object large OBJL_ Object small OBJS_ View VIEW_ Product images PROD_
 * Misc images MISC_
 * Where are the images? The images (typically gifs) are found in the same
 * location as this plugin class. This may mean the same package directory as
 * the package holding this class. The images are declared using
 * <code>this.getClass()</code> to ensure they are looked up via this plugin
 * class.
 * @author Christian Dupuis
 * @see org.eclipse.jface.resource.ImageRegistry
 */
public class WebflowImages {

    /**
     * 
     */
    private static final String ICON_PATH_PREFIX = "icons/full/";

    /**
     * 
     */
    private static final String NAME_PREFIX = Activator.PLUGIN_ID + '.';

    /**
     * 
     */
    private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();

    /* Declare Common paths */
    /**
     * 
     */
    private static URL ICON_BASE_URL = null;

    static {
        try {
            ICON_BASE_URL = new URL(Activator.getDefault().getBundle()
                    .getEntry("/"), ICON_PATH_PREFIX);
        } catch (MalformedURLException e) {
            Activator.log(e);
        }
    }

    /**
     * A table of all the <code>ImageDescriptor</code>s.
     */
    private static Map imageDescriptors;

    /**
     * The image registry containing <code>Image</code>s.
     */
    private static ImageRegistry imageRegistry;

    /*
     * Available cached Images in the Java plugin image registry.
     */
    /**
     * 
     */
    public static final String IMG_OBJS_EXPORT_ENABLED = NAME_PREFIX
            + "export_wiz_obj.gif";

    /**
     * 
     */
    public static final String IMG_OBJS_EXPORT_DISABLED = NAME_PREFIX
            + "export_wiz.gif";

    /**
     * 
     */
    public static final String IMG_OBJS_CONNECTION = NAME_PREFIX
            + "connection16.gif";

    /**
     * 
     */
    public static final String IMG_OBJS_ELSE_CONNECTION = NAME_PREFIX
            + "connection_d16.gif";

    /**
     * 
     */
    public static final String IMG_OBJS_OUTLINE = NAME_PREFIX + "outline.gif";

    /**
     * 
     */
    public static final String IMG_OBJS_OVERVIEW = NAME_PREFIX + "overview.gif";

    /**
     * 
     */
    public static final String IMG_OBJS_JAVABEAN = NAME_PREFIX + "javabean.gif";

    // Use IPath and toOSString to build the names to ensure they have the
    // slashes correct
    //      private final static String CTOOL = "ctool16/"; //basic colors - size
    // 16x16
    //      private final static String LOCALTOOL = "clcl16/"; //basic colors - size
    // 16x16
    //      private final static String DLCL = "dlcl16/"; //disabled - size 16x16
    //      private final static String ELCL = "elcl16/"; //enabled - size 16x16
    /**
     * 
     */
    private final static String OBJECT = "obj16/"; //basic colors - size

    // 16x16
    //      private final static String WIZBAN = "wizban/"; //basic colors - size
    // 16x16
    //      private final static String OVR = "ovr16/"; //basic colors - size 7x8
    //      private final static String VIEW= "cview16/"; // views //$NON-NLS-1$

    /**
     * 
     */
    public static final ImageDescriptor DESC_OBJS_EXPORT_ENABLED = createManaged(
            OBJECT, IMG_OBJS_EXPORT_ENABLED);

    /**
     * 
     */
    public static final ImageDescriptor DESC_OBJS_EXPORT_DISABLED = createManaged(
            OBJECT, IMG_OBJS_EXPORT_DISABLED);

    /**
     * 
     */
    public static final ImageDescriptor DESC_OBJS_CONNECTION = createManaged(
            OBJECT, IMG_OBJS_CONNECTION);

    /**
     * 
     */
    public static final ImageDescriptor DESC_OBJS_ELSE_CONNECTION = createManaged(
            OBJECT, IMG_OBJS_ELSE_CONNECTION);

    /**
     * 
     */
    public static final ImageDescriptor DESC_OBJS_OUTLINE = createManaged(
            OBJECT, IMG_OBJS_OUTLINE);

    /**
     * 
     */
    public static final ImageDescriptor DESC_OBJS_OVERVIEW = createManaged(
            OBJECT, IMG_OBJS_OVERVIEW);

    /**
     * 
     */
    public static final ImageDescriptor DESC_OBJS_JAVABEAN = createManaged(
            OBJECT, IMG_OBJS_JAVABEAN);
    
    /**
     * Returns the <code>Image<code> identified by the given key,
     * or <code>null</code> if it does not exist.
     * 
     * @param key 
     * 
     * @return 
     */
    public static Image getImage(String key) {
        return getImageRegistry().get(key);
    }

    /**
     * Sets the three image descriptors for enabled, disabled, and hovered to an
     * action. The actions are retrieved from the *tool16 folders.
     * 
     * @param action 
     * @param iconName 
     */
    public static void setToolImageDescriptors(IAction action, String iconName) {
        setImageDescriptors(action, "tool16", iconName);
    }

    /**
     * Sets the three image descriptors for enabled, disabled, and hovered to an
     * action. The actions are retrieved from the *lcl16 folders.
     * 
     * @param action 
     * @param iconName 
     */
    public static void setLocalImageDescriptors(IAction action, String iconName) {
        setImageDescriptors(action, "lcl16", iconName);
    }

    /*
     * Helper method to access the image registry from the JavaPlugin class.
     */
    /**
     * 
     * 
     * @return 
     */
    public static ImageRegistry getImageRegistry() {
        if (imageRegistry == null) {
            imageRegistry = new ImageRegistry();
            for (Iterator iter = imageDescriptors.keySet().iterator(); iter
                    .hasNext();) {
                String key = (String) iter.next();
                imageRegistry.put(key, (ImageDescriptor) imageDescriptors
                        .get(key));
            }
            imageDescriptors = null;
        }
        return imageRegistry;
    }

    //---- Helper methods to access icons on the file system
    // -------------------

    /**
     * 
     * 
     * @param action 
     * @param type 
     * @param relPath 
     */
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
         * (MalformedURLException e) { BeansGraphPlugin.log(e); }
         */
        action.setImageDescriptor(create("e" + type, relPath));
    }

    /**
     * 
     * 
     * @param prefix 
     * @param name 
     * 
     * @return 
     */
    @SuppressWarnings("unchecked")
	private static ImageDescriptor createManaged(String prefix, String name) {
        try {
            ImageDescriptor result = ImageDescriptor
                    .createFromURL(makeIconFileURL(prefix, name
                            .substring(NAME_PREFIX_LENGTH)));
            if (imageDescriptors == null) {
                imageDescriptors = new HashMap();
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

    /**
     * 
     * 
     * @param prefix 
     * @param name 
     * 
     * @return 
     */
    private static ImageDescriptor create(String prefix, String name) {
        try {
            return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
        } catch (MalformedURLException e) {
            Activator.log(e);
            return ImageDescriptor.getMissingImageDescriptor();
        }
    }

    /**
     * 
     * 
     * @param prefix 
     * @param name 
     * 
     * @return 
     * 
     * @throws MalformedURLException 
     */
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
