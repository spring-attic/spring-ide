/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.core.internal.model.SpringModel;
import org.springframework.ide.eclipse.core.java.TypeStructureCache;
import org.springframework.ide.eclipse.core.model.ISpringModel;

/**
 * Central access point for the Spring IDE core plug-in (id
 * <code>"org.springframework.ide.eclipse.core"</code>).
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class SpringCore extends Plugin {

	/**
	 * Plugin identifier for Spring Core (value <code>org.springframework.ide.eclipse.core</code>).
	 */
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.core";

	/**
	 * The identifier for the Spring project builder (value
	 * <code>"org.springframework.ide.eclipse.core.springbuilder"</code>).
	 */
	public static final String BUILDER_ID = PLUGIN_ID + ".springbuilder";

	/**
	 * The identifier for the Spring nature (value
	 * <code>"org.springframework.ide.eclipse.core.springnature"</code>). The presence of this
	 * nature on a project indicates that it is Spring-capable.
	 * 
	 * @see org.eclipse.core.resources.IProject#hasNature(java.lang.String)
	 */
	public static final String NATURE_ID = PLUGIN_ID + ".springnature";

	/**
	 * The identifier for the Spring problem marker (value
	 * <code>"org.springframework.ide.eclipse.core.problemmarker"</code>).
	 */
	public static final String MARKER_ID = PLUGIN_ID + ".problemmarker";

	private static final String RESOURCE_NAME = PLUGIN_ID + ".messages";

	/** The identifier for enablement of project versus workspace settings */
	public static final String PROJECT_PROPERTY_ID = "enable.project.preferences";

	/** Temporally setting to enable or disable the calculation of java structure changes and state */
	public static final String USE_CHANGE_DETECTION_IN_JAVA_FILES = PLUGIN_ID
			+ ".useChangeDetectionForJavaFiles";

	/** The shared instance */
	private static SpringCore plugin;

	/** The singleton Spring model */
	private static SpringModel model;

	/** Resource bundle */
	private ResourceBundle resourceBundle;
	
	private static TypeStructureCache typeStructureCache;

	/**
	 * Creates the Spring core plug-in.
	 * <p>
	 * The plug-in instance is created automatically by the Eclipse platform. Clients must not call.
	 */
	public SpringCore() {
		plugin = this;
		model = new SpringModel();
		typeStructureCache = new TypeStructureCache();
		try {
			resourceBundle = ResourceBundle.getBundle(RESOURCE_NAME);
		}
		catch (MissingResourceException e) {
			resourceBundle = null;
		}
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		model.startup();
		typeStructureCache.startup();
		// install default for incremtal compilation
		plugin.getPluginPreferences().setDefault(USE_CHANGE_DETECTION_IN_JAVA_FILES, true);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		model.shutdown();
		typeStructureCache.shutdown();
		super.stop(context);
	}

	/**
	 * Returns the single instance of the Spring core plug-in runtime class.
	 */
	public static SpringCore getDefault() {
		return plugin;
	}

	/**
	 * Returns the singleton {@link ISpringModel}.
	 */
	public static final ISpringModel getModel() {
		return model;
	}
	
	public static final TypeStructureCache getTypeStructureCache() {
		return typeStructureCache;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		String bundleString;
		ResourceBundle bundle = getDefault().getResourceBundle();
		if (bundle != null) {
			try {
				bundleString = bundle.getString(key);
			}
			catch (MissingResourceException e) {
				log(e);
				bundleString = "!" + key + "!";
			}
		}
		else {
			bundleString = "!" + key + "!";
		}
		return bundleString;
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public static boolean isDebug(String option) {
		String value = Platform.getDebugOption(option);
		return (value != null && value.equalsIgnoreCase("true") ? true : false);
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Writes the message to the plug-in's log
	 * 
	 * @param message the text to write to the log
	 */
	public static void log(String message, Throwable exception) {
		IStatus status = createErrorStatus(message, exception);
		getDefault().getLog().log(status);
	}

	public static void log(Throwable exception) {
		getDefault().getLog().log(
				createErrorStatus(getResourceString("Plugin.internal_error"), exception));
	}

	/**
	 * Returns a new {@link IStatus} with status "ERROR" for this plug-in.
	 */
	public static IStatus createErrorStatus(String message, Throwable exception) {
		if (message == null) {
			message = "";
		}
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, exception);
	}

	public static String getFormattedMessage(String key, Object... args) {
		return MessageFormat.format(getResourceString(key), args);
	}
}
