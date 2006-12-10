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

/**
 * Central access point for the Spring IDE core plug-in
 * (id <code>"org.springframework.ide.eclipse.core"</code>).
 *
 * @author Torsten Juergeleit
 */
public class SpringCore extends Plugin {

	/**
	 * Plugin identifier for Spring Core (value
	 * <code>org.springframework.ide.eclipse.core</code>).
	 */
	public static final String PLUGIN_ID =
										"org.springframework.ide.eclipse.core";
	/**
	 * The identifier for the Spring project builder
	 * (value <code>"org.springframework.ide.eclipse.core.springbuilder"</code>).
	 */
	public static final String BUILDER_ID = PLUGIN_ID + ".springbuilder";

	/**
	 * The identifier for the Spring nature
	 * (value <code>"org.springframework.ide.eclipse.core.springnature"</code>).
	 * The presence of this nature on a project indicates that it is 
	 * Spring-capable.
	 *
	 * @see org.eclipse.core.resources.IProject#hasNature(java.lang.String)
	 */
	public static final String NATURE_ID = PLUGIN_ID + ".springnature";

	/**
	 * The identifier for the Spring problem marker
	 * (value <code>"org.springframework.ide.eclipse.core.problemmarker"</code>).
	 */
	public static final String MARKER_ID = PLUGIN_ID + ".problemmarker";

	private static final String RESOURCE_NAME = PLUGIN_ID + ".messages";

	/** The shared instance */
	private static SpringCore plugin; 

	/** Resource bundle */
	private ResourceBundle resourceBundle;

	/**
	 * Creates the Spring core plug-in.
	 * <p>
	 * The plug-in instance is created automatically by the Eclipse platform.
	 * Clients must not call.
	 */
	public SpringCore() {
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle(RESOURCE_NAME);
		} catch (MissingResourceException e) {
			resourceBundle = null;
		}
	}

	/**
	 * Returns the single instance of the Spring core plug-in runtime class.
	 * 
	 * @return the single instance of the Spring core plug-in runtime class
	 */
	public static SpringCore getDefault() {
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
	    String bundleString;
		ResourceBundle bundle = getDefault().getResourceBundle();
		if (bundle != null) {
			try {
				bundleString = bundle.getString(key);
			} catch (MissingResourceException e) {
			    log(e);
				bundleString = "!" + key + "!";
			}
		} else {
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
		getDefault().getLog().log(createErrorStatus(
						getResourceString("Plugin.internal_error"), exception));
	}

	/**
	 * Returns a new <code>IStatus</code> with status "ERROR" for this plug-in.
	 */
	public static IStatus createErrorStatus(String message,
											Throwable exception) {
		if (message == null) {
			message = ""; 
		}		
		return new Status(Status.ERROR, PLUGIN_ID, 0, message, exception);
	}

	public static String getFormattedMessage(String key, String arg) {
		return getFormattedMessage(key, new String[] { arg });
	}

	public static String getFormattedMessage(String key, Object[] args) {
		return MessageFormat.format(getResourceString(key), args);
	}
}
