/*******************************************************************************
 * Copyright (c) 2008, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.ui.graph;

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
 * @author Christian Dupuis
 */
public class BeansGraphImages {

	private static final String ICON_PATH_PREFIX = "icons/full/";

	private static final String NAME_PREFIX = BeansGraphPlugin.PLUGIN_ID + '.';

	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();

	/* Declare Common paths */
	private static URL ICON_BASE_URL = null;

	static {
		try {
			ICON_BASE_URL = new URL(BeansGraphPlugin.getDefault().getBundle().getEntry("/"), ICON_PATH_PREFIX);
		}
		catch (MalformedURLException e) {
			BeansGraphPlugin.log(e);
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
	public static final String IMG_OBJS_EXPORT_ENABLED = NAME_PREFIX + "export_wiz_obj.gif";

	public static final String IMG_OBJS_EXPORT_DISABLED = NAME_PREFIX + "export_wiz.gif";

	public static final String IMG_OBJS_BEANS_GRAPH = NAME_PREFIX + "beans_graph_obj.gif";

	// Use IPath and toOSString to build the names to ensure they have the
	// slashes correct
	// private final static String CTOOL = "ctool16/"; //basic colors - size
	// 16x16
	// private final static String LOCALTOOL = "clcl16/"; //basic colors - size
	// 16x16
	// private final static String DLCL = "dlcl16/"; //disabled - size 16x16
	// private final static String ELCL = "elcl16/"; //enabled - size 16x16
	private final static String OBJECT = "obj16/"; // basic colors - size

	// 16x16
	// private final static String WIZBAN = "wizban/"; //basic colors - size
	// 16x16
	// private final static String OVR = "ovr16/"; //basic colors - size 7x8
	//      private final static String VIEW= "cview16/"; // views //$NON-NLS-1$
	public static final ImageDescriptor DESC_OBJS_EXPORT_ENABLED = createManaged(OBJECT, IMG_OBJS_EXPORT_ENABLED);

	public static final ImageDescriptor DESC_OBJS_EXPORT_DISABLED = createManaged(OBJECT, IMG_OBJS_EXPORT_DISABLED);

	public static final ImageDescriptor DESC_OBJS_BEANS_GPRAH = createManaged(OBJECT, IMG_OBJS_BEANS_GRAPH);

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
	 * Sets the three image descriptors for enabled, disabled, and hovered to an action. The actions are retrieved from
	 * the *tool16 folders.
	 * 
	 * @param action
	 * @param iconName
	 */
	public static void setToolImageDescriptors(IAction action, String iconName) {
		setImageDescriptors(action, "tool16", iconName);
	}

	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to an action. The actions are retrieved from
	 * the *lcl16 folders.
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
	public static ImageRegistry getImageRegistry() {
		if (imageRegistry == null) {
			imageRegistry = new ImageRegistry();
			for (Iterator iter = imageDescriptors.keySet().iterator(); iter.hasNext();) {
				String key = (String) iter.next();
				imageRegistry.put(key, (ImageDescriptor) imageDescriptors.get(key));
			}
			imageDescriptors = null;
		}
		return imageRegistry;
	}

	// ---- Helper methods to access icons on the file system
	// -------------------

	private static void setImageDescriptors(IAction action, String type, String relPath) {
		try {
			ImageDescriptor id = ImageDescriptor.createFromURL(makeIconFileURL("d" + type, relPath));
			if (id != null) {
				action.setDisabledImageDescriptor(id);
			}
		}
		catch (MalformedURLException e) {
			BeansGraphPlugin.log(e);
		}
		/*
		 * try { ImageDescriptor id = ImageDescriptor.createFromURL(makeIconFileURL( "c" + type, relPath)); if (id !=
		 * null) { action.setHoverImageDescriptor(id); } } catch (MalformedURLException e) { BeansGraphPlugin.log(e); }
		 */
		action.setImageDescriptor(create("e" + type, relPath));
	}

	@SuppressWarnings("unchecked")
	private static ImageDescriptor createManaged(String prefix, String name) {
		try {
			ImageDescriptor result = ImageDescriptor.createFromURL(makeIconFileURL(prefix, name
					.substring(NAME_PREFIX_LENGTH)));
			if (imageDescriptors == null) {
				imageDescriptors = new HashMap();
			}
			imageDescriptors.put(name, result);
			if (imageRegistry != null) {
				BeansGraphPlugin.log("Image registry already defined", null);
			}
			return result;
		}
		catch (MalformedURLException e) {
			BeansGraphPlugin.log(e);
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
		}
		catch (MalformedURLException e) {
			BeansGraphPlugin.log(e);
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static URL makeIconFileURL(String prefix, String name) throws MalformedURLException {
		if (ICON_BASE_URL == null) {
			throw new MalformedURLException();
		}

		StringBuffer buffer = new StringBuffer(prefix);
		buffer.append('/');
		buffer.append(name);
		return new URL(ICON_BASE_URL, buffer.toString());
	}

}
