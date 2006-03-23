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
package org.springframework.ide.eclipse.core.ui;

import java.io.File;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.core.ui.images.Images;
import org.springframework.ide.eclipse.core.ui.images.PluginImages;
import org.springframework.ide.eclipse.core.ui.utils.PluginUtils;

/**
 * The main plugin class to be used in the desktop.
 * @author Pierre-Antoine Grégoire
 */
public class SpringCoreUIPlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = SpringCoreUIPlugin.class.getName();

	// The shared instance.
	private static SpringCoreUIPlugin plugin;

	// Resource bundle.
	private ResourceBundle resourceBundle;

	private Images images;

	/**
	 * The constructor.
	 */
	public SpringCoreUIPlugin() {
		super();
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("org.springframework.ide.eclipse.core.ui.SpringCoreUIPlugin");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		images = new Images();
		images.addImage(this, "icons/basic_error_64.gif", PluginImages.LOGO_BASIC_ERROR_64);
		images.addImage(this, "icons/basic_info_64.gif", PluginImages.LOGO_BASIC_INFO_64);
		images.addImage(this, "icons/basic_warning_64.gif", PluginImages.LOGO_BASIC_WARNING_64);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static SpringCoreUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = SpringCoreUIPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	public static File getFile(String relativePath) {
		return PluginUtils.getFileFromBundle(getDefault().getBundle(), relativePath);
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public static Shell getActiveShell() {
		if (plugin == null)
			return null;
		IWorkbench workBench = plugin.getWorkbench();
		if (workBench == null)
			return null;
		IWorkbenchWindow workBenchWindow = workBench.getActiveWorkbenchWindow();
		if (workBenchWindow == null)
			return null;
		return workBenchWindow.getShell();
	}

	public Images getImages() {
		return images;
	}
}
