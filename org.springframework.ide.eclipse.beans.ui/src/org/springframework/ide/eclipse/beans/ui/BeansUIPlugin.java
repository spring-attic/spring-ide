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

package org.springframework.ide.eclipse.beans.ui;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;
import org.springframework.ide.eclipse.ui.ImageDescriptorRegistry;

/**
 * Central access point for the Spring Framework UI plug-in (id
 * <code>"org.springframework.ide.eclipse.beans.ui"</code>).
 * 
 * @author Torsten Juergeleit
 */
public class BeansUIPlugin extends AbstractUIPlugin {

	/**
	 * Plugin identifier for Spring Beans UI (value
	 * <code>org.springframework.ide.eclipse.beans.ui</code>).
	 */
	public static final String PLUGIN_ID =
			"org.springframework.ide.eclipse.beans.ui";

	public static final String RESOURCE_NAME = PLUGIN_ID + ".messages";

	/** The shared instance. */
	private static BeansUIPlugin plugin;

	private ResourceBundle resourceBundle;
	private ImageDescriptorRegistry imageDescriptorRegistry;
	private ILabelProvider labelProvider;

	/**
	 * Creates the Spring Beans UI plug-in.
	 * <p>
	 * The plug-in instance is created automatically by the Eclipse platform.
	 * Clients must not call.
	 */
	public BeansUIPlugin() {
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle(RESOURCE_NAME);
		} catch (MissingResourceException e) {
			log(e);
			resourceBundle = null;
		}
	}

	protected void initializeImageRegistry(ImageRegistry registry) {
		BeansUIImages.initializeImageRegistry(registry);
	}

	public void stop(BundleContext context) throws Exception {
		if (labelProvider != null) {
			labelProvider.dispose();
			labelProvider = null;
		}
		if (imageDescriptorRegistry != null) {
			imageDescriptorRegistry.dispose();
			imageDescriptorRegistry = null;
		}
		super.stop(context);
	}

	public static ImageDescriptorRegistry getImageDescriptorRegistry() {
		return getDefault().internalGetImageDescriptorRegistry();
	}

	private synchronized ImageDescriptorRegistry
			internalGetImageDescriptorRegistry() {
		if (imageDescriptorRegistry == null) {
			imageDescriptorRegistry = new ImageDescriptorRegistry();
		}
		return imageDescriptorRegistry;
	}

	/**
	 * Returns then singleton instance of
	 * <code>BeansModelLabelProvider(true)</code>.
	 * <p>
	 * <b>For this instance the dispose method must never becalled!! This is
	 * done by <code>Plugin.stop()</code> instead.</b>
	 */
	public static ILabelProvider getLabelProvider() {
		return getDefault().internalGetLabelProvider();
	}

	private synchronized ILabelProvider internalGetLabelProvider() {
		if (labelProvider == null) {
			labelProvider = new BeansModelLabelProvider(true);
		}
		return labelProvider;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static BeansUIPlugin getDefault() {
		return plugin;
	}

	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	public static Shell getActiveWorkbenchShell() {
		return getActiveWorkbenchWindow().getShell();
	}

	public static IWorkbenchPage getActiveWorkbenchPage() {
		return getActiveWorkbenchWindow().getActivePage();
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

	public static String getFormattedMessage(String key, Object arg) {
		return getFormattedMessage(key, new Object[] { arg });
	}

	public static String getFormattedMessage(String key, Object[] args) {
		return MessageFormat.format(getResourceString(key), args);
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
		getDefault().getLog().log(createErrorStatus(getResourceString(
				"Plugin.internal_error"), exception));
	}

	/**
	 * Returns a new <code>IStatus</code> for this plug-in
	 */
	public static IStatus createErrorStatus(String message,
			Throwable exception) {
		if (message == null) {
			message = "";
		}
		return new Status(Status.ERROR, PLUGIN_ID, 0, message, exception);
	}
}
