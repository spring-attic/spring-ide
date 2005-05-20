/*
 * Copyright 2002-2005 the original author or authors.
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

package org.springframework.ide.eclipse.web.flow.ui.editor;

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
public class WebFlowImages {

    private static final String ICON_PATH_PREFIX = "icons/full/";

    private static final String NAME_PREFIX = WebFlowPlugin.PLUGIN_ID + '.';

    private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();

    /* Declare Common paths */
    private static URL ICON_BASE_URL = null;

    static {
        try {
            ICON_BASE_URL = new URL(WebFlowPlugin.getDefault().getBundle()
                    .getEntry("/"), ICON_PATH_PREFIX);
        }
        catch (MalformedURLException e) {
            WebFlowPlugin.log(e);
        }
    }

    /** A table of all the <code>ImageDescriptor</code>s. */
    private static Map imageDescriptors;

    /** The image registry containing <code>Image</code>s. */
    private static ImageRegistry imageRegistry;

    /*
     * Available cached Images in the Java plugin image registry.
     */
    public static final String IMG_OBJS_ACTION_STATE = NAME_PREFIX
            + "action_state_obj.gif";

    public static final String IMG_OBJS_ACTION = NAME_PREFIX + "action_obj.gif";

    public static final String IMG_OBJS_END_STATE = NAME_PREFIX
            + "end_state_obj.gif";

    public static final String IMG_OBJS_SUBFLOW_STATE = NAME_PREFIX
            + "subflow_state_obj.gif";

    public static final String IMG_OBJS_START_STATE = NAME_PREFIX
            + "start_state_obj.gif";

    public static final String IMG_OBJS_ATTRIBUTE_MAPPER = NAME_PREFIX
            + "attribute_mapper_obj.gif";

    public static final String IMG_OBJS_VIEW_STATE = NAME_PREFIX
            + "view_state_obj.gif";

    public static final String IMG_OBJS_EXPORT_ENABLED = NAME_PREFIX
            + "export_wiz_obj.gif";

    public static final String IMG_OBJS_EXPORT_DISABLED = NAME_PREFIX
            + "export_wiz.gif";

    public static final String IMG_OBJS_DECISION_STATE = NAME_PREFIX
            + "decision_state_obj.gif";

    public static final String IMG_OBJS_IF = NAME_PREFIX + "if_obj.gif";

    public static final String IMG_OBJS_SPRING = NAME_PREFIX + "spring_obj.gif";

    public static final String IMG_OBJS_ERROR = NAME_PREFIX + "error_obj.gif";

    public static final String IMG_OBJS_PROPERTIES = NAME_PREFIX
            + "properties_obj.gif";

    public static final String IMG_OBJS_CONNECTION = NAME_PREFIX
            + "connection16.gif";

    public static final String IMG_OBJS_ELSE_CONNECTION = NAME_PREFIX
            + "connection_d16.gif";

    public static final String IMG_OBJS_OUTLINE = NAME_PREFIX
            + "outline.gif";
    
    public static final String IMG_OBJS_OVERVIEW = NAME_PREFIX
            + "overview.gif";
    
    public static final String IMG_OBJS_JAVABEAN = NAME_PREFIX
            + "javabean.gif";
    
    // Use IPath and toOSString to build the names to ensure they have the
    // slashes correct
    //      private final static String CTOOL = "ctool16/"; //basic colors - size
    // 16x16
    //      private final static String LOCALTOOL = "clcl16/"; //basic colors - size
    // 16x16
    //      private final static String DLCL = "dlcl16/"; //disabled - size 16x16
    //      private final static String ELCL = "elcl16/"; //enabled - size 16x16
    private final static String OBJECT = "obj16/"; //basic colors - size

    // 16x16
    //      private final static String WIZBAN = "wizban/"; //basic colors - size
    // 16x16
    //      private final static String OVR = "ovr16/"; //basic colors - size 7x8
    //      private final static String VIEW= "cview16/"; // views //$NON-NLS-1$

    public static final ImageDescriptor DESC_OBJS_ACTION_STATE = createManaged(
            OBJECT, IMG_OBJS_ACTION_STATE);

    public static final ImageDescriptor DESC_OBJS_ACTION = createManaged(
            OBJECT, IMG_OBJS_ACTION);

    public static final ImageDescriptor DESC_OBJS_END_STATE = createManaged(
            OBJECT, IMG_OBJS_END_STATE);

    public static final ImageDescriptor DESC_OBJS_SUBFLOW_STATE = createManaged(
            OBJECT, IMG_OBJS_SUBFLOW_STATE);

    public static final ImageDescriptor DESC_OBJS_START_STATE = createManaged(
            OBJECT, IMG_OBJS_START_STATE);

    public static final ImageDescriptor DESC_OBJS_ATTRIBUTE_MAPPER = createManaged(
            OBJECT, IMG_OBJS_ATTRIBUTE_MAPPER);

    public static final ImageDescriptor DESC_OBJS_VIEW_STATE = createManaged(
            OBJECT, IMG_OBJS_VIEW_STATE);

    public static final ImageDescriptor DESC_OBJS_SPRING = createManaged(
            OBJECT, IMG_OBJS_SPRING);

    public static final ImageDescriptor DESC_OBJS_EXPORT_ENABLED = createManaged(
            OBJECT, IMG_OBJS_EXPORT_ENABLED);

    public static final ImageDescriptor DESC_OBJS_EXPORT_DISABLED = createManaged(
            OBJECT, IMG_OBJS_EXPORT_DISABLED);

    public static final ImageDescriptor DESC_OBJS_PROPERTIES = createManaged(
            OBJECT, IMG_OBJS_PROPERTIES);

    public static final ImageDescriptor DESC_OBJS_DECISION_STATE = createManaged(
            OBJECT, IMG_OBJS_DECISION_STATE);

    public static final ImageDescriptor DESC_OBJS_IF = createManaged(OBJECT,
            IMG_OBJS_IF);
    
    public static final ImageDescriptor DESC_OBJS_CONNECTION = createManaged(OBJECT,
            IMG_OBJS_CONNECTION);
    
    public static final ImageDescriptor DESC_OBJS_ELSE_CONNECTION = createManaged(OBJECT,
            IMG_OBJS_ELSE_CONNECTION);

    public static final ImageDescriptor DESC_OBJS_OUTLINE = createManaged(OBJECT,
            IMG_OBJS_OUTLINE);
    
    public static final ImageDescriptor DESC_OBJS_OVERVIEW = createManaged(OBJECT,
            IMG_OBJS_OVERVIEW);
    
    public static final ImageDescriptor DESC_OBJS_JAVABEAN = createManaged(OBJECT,
            IMG_OBJS_JAVABEAN);
    
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

    private static void setImageDescriptors(IAction action, String type,
            String relPath) {
        try {
            ImageDescriptor id = ImageDescriptor.createFromURL(makeIconFileURL(
                    "d" + type, relPath));
            if (id != null) {
                action.setDisabledImageDescriptor(id);
            }
        }
        catch (MalformedURLException e) {
            WebFlowPlugin.log(e);
        }
        /*
         * try { ImageDescriptor id =
         * ImageDescriptor.createFromURL(makeIconFileURL( "c" + type, relPath));
         * if (id != null) { action.setHoverImageDescriptor(id); } } catch
         * (MalformedURLException e) { BeansGraphPlugin.log(e); }
         */
        action.setImageDescriptor(create("e" + type, relPath));
    }

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
                WebFlowPlugin.log("Image registry already defined", null);
            }
            return result;
        }
        catch (MalformedURLException e) {
            WebFlowPlugin.log(e);
            return ImageDescriptor.getMissingImageDescriptor();
        }
    }

    private static ImageDescriptor create(String prefix, String name) {
        try {
            return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
        }
        catch (MalformedURLException e) {
            WebFlowPlugin.log(e);
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
