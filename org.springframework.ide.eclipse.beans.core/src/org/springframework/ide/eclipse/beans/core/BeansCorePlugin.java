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

package org.springframework.ide.eclipse.beans.core;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.core.MessageUtils;

/**
 * Central access point for the Spring Framework Core plug-in
 * (id <code>"org.springframework.ide.eclipse.beans.core"</code>).
 *
 * @author Torsten Juergeleit
 */
public class BeansCorePlugin extends Plugin {

	/**
	 * Plugin identifier for Spring Beans Core (value
	 * <code>org.springframework.ide.eclipse.beans.core</code>).
	 */
	public static final String PLUGIN_ID =
								   "org.springframework.ide.eclipse.beans.core";
	private static final String RESOURCE_NAME = PLUGIN_ID + ".messages";

	/** The shared instance */
	private static BeansCorePlugin plugin;

	/** The singleton beans model */
	private static BeansModel model;

	/** Resource bundle */
	private ResourceBundle resourceBundle;

	/**
	 * Creates the Spring Beans Core plug-in.
	 * <p>
	 * The plug-in instance is created automatically by the Eclipse platform.
	 * Clients must not call.
	 */
	public BeansCorePlugin() {
		plugin = this;
		model = new BeansModel();
		try {
			resourceBundle = ResourceBundle.getBundle(RESOURCE_NAME);
		} catch (MissingResourceException e) {
			resourceBundle = null;
		}
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		model.startup();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		model.shutdown();
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static final BeansCorePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the singleton <code>IBeansModel</code>.
	 */
	public static final IBeansModel getModel() {
		return model;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static final IWorkspace getWorkspace() {
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
	public final ResourceBundle getResourceBundle() {
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
		return new Status(IStatus.ERROR, PLUGIN_ID, 0, message, exception);
	}

	public static String getFormattedMessage(String key, String arg) {
		return MessageUtils.format(getResourceString(key), arg);
	}

	public static String getFormattedMessage(String key, String[] args) {
		return MessageUtils.format(getResourceString(key), args);
	}
}
