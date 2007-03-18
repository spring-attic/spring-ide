/*
 * Copyright 2002-2007 the original author or authors.
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

package org.springframework.ide.eclipse.webflow.ui.editor.namespaces.webflow;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.springframework.ide.eclipse.webflow.ui.editor.Activator;

/**
 * The images provided by the Spring UI plugin. Initialize the image registry by
 * declaring all of the required graphics. This involves creating JFace image
 * descriptors describing how to create/find the image should it be needed. The
 * image is not actually allocated until requested. Prefix conventions Wizard
 * Banners WIZBAN_ Preference Banners PREF_BAN_ Property Page Banners PROPBAN_
 * Color toolbar CTOOL_ Enable toolbar ETOOL_ Disable toolbar DTOOL_ Local
 * enabled toolbar ELCL_ Local Disable toolbar DLCL_ Object large OBJL_ Object
 * small OBJS_ View VIEW_ Product images PROD_ Misc images MISC_ Where are the
 * images? The images (typically gifs) are found in the same location as this
 * plugin class. This may mean the same package directory as the package holding
 * this class. The images are declared using <code>this.getClass()</code> to
 * ensure they are looked up via this plugin class.
 * 
 * @see org.eclipse.jface.resource.ImageRegistry
 */
public class WebflowUIImages {

	/**
	 * 
	 */
	private static final String ICON_PATH_PREFIX = "";

	/**
	 * 
	 */
	private static final String NAME_PREFIX = Activator.PLUGIN_ID + '.';

	/**
	 * 
	 */
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();

	/**
	 * 
	 */
	private static final String STRUCTURE = "icons/full/obj16/";

	/* Declare Common paths */
	/**
	 * 
	 */
	private static URL ICON_BASE_URL = null;

	static {
		try {
			ICON_BASE_URL = new URL(Activator.getDefault().getBundle()
					.getEntry("/"), ICON_PATH_PREFIX);
		}
		catch (MalformedURLException e) {
		}
	}

	/**
	 * A table of all the <code>ImageDescriptor</code>s.
	 */
	private static Map<String, ImageDescriptor> imageDescriptors;

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
	public static final String IMG_OBJS_WEBFLOW = NAME_PREFIX
			+ "spring_webflow_obj.gif";

	/**
	 * 
	 */
	public static final String IMG_OBJS_ACTION = NAME_PREFIX + "action_obj.gif";

	/**
	 * 
	 */
	public static final String IMG_OBJS_ACTION_STATE = NAME_PREFIX
			+ "action_state_obj.gif";

	/**
	 * 
	 */
	public static final String IMG_OBJS_ACTIONS = NAME_PREFIX
			+ "actions_obj.gif";

	/**
	 * 
	 */
	public static final String IMG_OBJS_ARGUMENT = NAME_PREFIX
			+ "argument_obj.gif";

	/**
	 * 
	 */
	public static final String IMG_OBJS_ATTRIBUTE_MAPPER = NAME_PREFIX
			+ "attribute_mapper_obj.gif";

	/**
	 * 
	 */
	public static final String IMG_OBJS_BEAN_ACTION = NAME_PREFIX
			+ "bean_action_obj.gif";

	/**
	 * 
	 */
	public static final String IMG_OBJS_DECISION_STATE = NAME_PREFIX
			+ "decision_state_obj.gif";

	/**
	 * 
	 */
	public static final String IMG_OBJS_END_STATE = NAME_PREFIX
			+ "end_state_obj.gif";

	/**
	 * 
	 */
	public static final String IMG_OBJS_EVALUATION_ACTION = NAME_PREFIX
			+ "evaluation_action_obj.gif";

	/**
	 * 
	 */
	public static final String IMG_OBJS_EXCEPTION_HANDLER = NAME_PREFIX
			+ "exception_handler_obj.gif";

	/**
	 * 
	 */
	public static final String IMG_OBJS_EXPRESSION = NAME_PREFIX
			+ "expression_obj.gif";

	/**
	 * 
	 */
	public static final String IMG_OVR_ERROR = NAME_PREFIX
		+ "error_ovr.gif";

	/**
	 * 
	 */
	public static final String IMG_OBJS_GLOBAL_TRANSITION = NAME_PREFIX
			+ "global_transition_obj.gif";

	/**
	 * 
	 */
	public static final String IMG_OBJS_IF = NAME_PREFIX + "if_obj.gif";

	/**
	 * 
	 */
	public static final String IMG_OBJS_IMPORT = NAME_PREFIX + "import_obj.gif";

	/**
	 * 
	 */
	public static final String IMG_OBJS_METHOD = NAME_PREFIX + "method_obj.gif";

	/**
	 * 
	 */
	public static final String IMG_OBJS_INPUT = NAME_PREFIX + "input_obj.gif";

	/**
	 * 
	 */
	public static final String IMG_OVR_INPUT = NAME_PREFIX + "input_ovr.gif";

	/**
	 * 
	 */
	public static final String IMG_OBJS_OUTPUT = NAME_PREFIX + "output_obj.gif";

	/**
	 * 
	 */
	public static final String IMG_OVR_OUTPUT = NAME_PREFIX + "output_ovr.gif";

	/**
	 * 
	 */
	public static final String IMG_OBJS_PROPERTIES = NAME_PREFIX
			+ "properties_obj.gif";

	/**
	 * 
	 */
	public static final String IMG_OVR_RENDER = NAME_PREFIX
			+ "render_ovr.gif";

	/**
	 * 
	 */
	public static final String IMG_OBJS_START_ACTION = NAME_PREFIX
			+ "start_action_obj.gif";

	/**
	 * 
	 */
	public static final String IMG_OBJS_SET_ACTION = NAME_PREFIX
			+ "set_action_obj.gif";

	/**
	 * 
	 */
	public static final String IMG_OBJS_START_STATE = NAME_PREFIX
			+ "start_state_obj.gif";

	/**
	 * 
	 */
	public static final String IMG_OVR_START_STATE = NAME_PREFIX
			+ "start_state_ovr.gif";

	/**
	 * 
	 */
	public static final String IMG_OBJS_SUBFLOW_STATE = NAME_PREFIX
			+ "subflow_state_obj.gif";

	/**
	 * 
	 */
	public static final String IMG_OBJS_TRANSITION = NAME_PREFIX
			+ "transition_obj.gif";

	/**
	 * 
	 */
	public static final String IMG_OBJS_VIEW_STATE = NAME_PREFIX
			+ "view_state_obj.gif";

	/**
	 * 
	 */
	public static final String IMG_OBJS_VAR = NAME_PREFIX + "var_obj.gif";

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_WEBFLOW = createManaged(
			STRUCTURE, IMG_OBJS_WEBFLOW);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_ACTION = createManaged(
			STRUCTURE, IMG_OBJS_ACTION);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_ACTION_STATE = createManaged(
			STRUCTURE, IMG_OBJS_ACTION_STATE);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_ACTIONS = createManaged(
			STRUCTURE, IMG_OBJS_ACTIONS);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_ARGUMENT = createManaged(
			STRUCTURE, IMG_OBJS_ARGUMENT);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_ATTRIBUTE_MAPPER = createManaged(
			STRUCTURE, IMG_OBJS_ATTRIBUTE_MAPPER);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_BEAN_ACTION = createManaged(
			STRUCTURE, IMG_OBJS_BEAN_ACTION);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_DECISION_STATE = createManaged(
			STRUCTURE, IMG_OBJS_DECISION_STATE);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_EVALUATION_ACTION = createManaged(
			STRUCTURE, IMG_OBJS_EVALUATION_ACTION);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_END_STATE = createManaged(
			STRUCTURE, IMG_OBJS_END_STATE);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_EXCEPTION_HANDLER = createManaged(
			STRUCTURE, IMG_OBJS_EXCEPTION_HANDLER);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_EXPRESSION = createManaged(
			STRUCTURE, IMG_OBJS_EXPRESSION);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OVR_ERROR = createManaged(
			STRUCTURE, IMG_OVR_ERROR);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_GLOBAL_TRANSITION = createManaged(
			STRUCTURE, IMG_OBJS_GLOBAL_TRANSITION);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_IF = createManaged(STRUCTURE,
			IMG_OBJS_IF);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_IMPORT = createManaged(
			STRUCTURE, IMG_OBJS_IMPORT);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_INPUT = createManaged(
			STRUCTURE, IMG_OBJS_INPUT);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OVR_INPUT = createManaged(
			STRUCTURE, IMG_OVR_INPUT);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_METHOD = createManaged(
			STRUCTURE, IMG_OBJS_METHOD);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_OUTPUT = createManaged(
			STRUCTURE, IMG_OBJS_OUTPUT);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OVR_OUTPUT = createManaged(
			STRUCTURE, IMG_OVR_OUTPUT);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_PROPERTIES = createManaged(
			STRUCTURE, IMG_OBJS_PROPERTIES);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OVR_RENDER = createManaged(
			STRUCTURE, IMG_OVR_RENDER);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_SET_ACTION = createManaged(
			STRUCTURE, IMG_OBJS_SET_ACTION);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_START_ACTION = createManaged(
			STRUCTURE, IMG_OBJS_START_ACTION);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_START_STATE = createManaged(
			STRUCTURE, IMG_OBJS_START_STATE);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OVR_START_STATE = createManaged(
			STRUCTURE, IMG_OVR_START_STATE);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_SUBFLOW_STATE = createManaged(
			STRUCTURE, IMG_OBJS_SUBFLOW_STATE);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_TRANSITION = createManaged(
			STRUCTURE, IMG_OBJS_TRANSITION);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_VAR = createManaged(
			STRUCTURE, IMG_OBJS_VAR);

	/**
	 * 
	 */
	public static final ImageDescriptor DESC_OBJS_VIEW_STATE = createManaged(
			STRUCTURE, IMG_OBJS_VIEW_STATE);

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
	/* package *//**
	 * 
	 * 
	 * @return 
	 */
	static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
			for (Iterator<String> iter = imageDescriptors.keySet().iterator(); iter
					.hasNext();) {
				String key = iter.next();
				imageRegistry.put(key, imageDescriptors
						.get(key));
			}
			imageDescriptors = null;
		}
		return imageRegistry;
	}

	// ---- Helper methods to access icons on the file system
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
		}
		catch (MalformedURLException e) {
		}
		/*
		 * try { ImageDescriptor id =
		 * ImageDescriptor.createFromURL(makeIconFileURL( "c" + type, relPath));
		 * if (id != null) { action.setHoverImageDescriptor(id); } } catch
		 * (MalformedURLException e) { BeansUIPlugin.log(e); }
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
			}
			return result;
		}
		catch (MalformedURLException e) {
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
		}
		catch (MalformedURLException e) {
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
