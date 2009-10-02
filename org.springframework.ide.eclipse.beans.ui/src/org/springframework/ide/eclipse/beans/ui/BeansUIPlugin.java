/*******************************************************************************
 * Copyright (c) 2005, 2009 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
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
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelDecorator;
import org.springframework.ide.eclipse.beans.ui.model.BeansModelLabelProvider;
import org.springframework.ide.eclipse.ui.ImageDescriptorRegistry;

/**
 * Central access point for the Spring Framework UI plug-in (id <code>"org.springframework.ide.eclipse.beans.ui"</code>
 * ).
 * 
 * @author Torsten Juergeleit
 */
public class BeansUIPlugin extends AbstractUIPlugin {

	/**
	 * Plugin identifier for Spring Beans UI (value <code>org.springframework.ide.eclipse.beans.ui</code>).
	 */
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.beans.ui";

	public static final String PROJECT_EXPLORER_CONTENT_PROVIDER_ID = PLUGIN_ID + ".navigator.projectExplorerContent";

	public static final String SPRING_EXPLORER_CONTENT_PROVIDER_ID = PLUGIN_ID + ".navigator.springExplorerContent";

	public static final String DEFAULT_DOUBLE_CLICK_ACTION_PREFERENCE_ID = PLUGIN_ID + ".shouldOpenConfigFile";

	public static final String SHOULD_SHOW_INFRASTRUCTURE_BEANS_PREFERENCE_ID = PLUGIN_ID
			+ ".shouldShowInfrastructureBeans";

	public static final String SHOULD_SHOW_INNER_BEANS_PREFERENCE_ID = PLUGIN_ID + ".shouldShowInnerBeans";

	public static final String SHOULD_SHOW_EXTENDED_CONTENT_PREFERENCE_ID = PLUGIN_ID + ".shouldExtendedContent";

	public static final String RESOURCE_NAME = PLUGIN_ID + ".messages";

	/** The shared instance. */
	private static BeansUIPlugin plugin;

	private ResourceBundle resourceBundle;

	private ImageDescriptorRegistry imageDescriptorRegistry;

	private ILabelProvider labelProvider;

	/** {@link IResourceChangeListener} that gets notified for project nature added events */
	// private IResourceChangeListener changeListener;

	/**
	 * Creates the Spring Beans UI plug-in.
	 * <p>
	 * The plug-in instance is created automatically by the Eclipse platform. Clients must not call.
	 */
	public BeansUIPlugin() {
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle(RESOURCE_NAME);
		}
		catch (MissingResourceException e) {
			log(e);
			resourceBundle = null;
		}
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
		BeansUIImages.initializeImageRegistry(registry);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (labelProvider != null) {

			// Unfortunately with Eclipse 3.3 we can't call
			// WorkbenchLabelLabelProvider.dispose()
			// from here anymore because at this time the Workbench is
			// already shutdown -> WorkbenchPlugin.shutdown() was called

			// labelProvider.dispose();
			labelProvider = null;
		}
		if (imageDescriptorRegistry != null) {
			imageDescriptorRegistry.dispose();
			imageDescriptorRegistry = null;
		}
		// if (changeListener != null) {
		// ResourcesPlugin.getWorkspace().removeResourceChangeListener(changeListener);
		// }
		super.stop(context);
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		// changeListener = new SpringResourceChangeListener(new SpringNatureAddedEventHandler());
		// ResourcesPlugin.getWorkspace().addResourceChangeListener(changeListener,
		// BeansResourceChangeListener.LISTENER_FLAGS);
		getPreferenceStore().setDefault(DEFAULT_DOUBLE_CLICK_ACTION_PREFERENCE_ID, true);
		getPreferenceStore().setDefault(SHOULD_SHOW_INFRASTRUCTURE_BEANS_PREFERENCE_ID, false);
		getPreferenceStore().setDefault(SHOULD_SHOW_INNER_BEANS_PREFERENCE_ID, true);
		getPreferenceStore().setDefault(SHOULD_SHOW_EXTENDED_CONTENT_PREFERENCE_ID, false);
	}

	public static ImageDescriptorRegistry getImageDescriptorRegistry() {
		return getDefault().internalGetImageDescriptorRegistry();
	}

	private synchronized ImageDescriptorRegistry internalGetImageDescriptorRegistry() {
		if (imageDescriptorRegistry == null) {
			imageDescriptorRegistry = new ImageDescriptorRegistry();
		}
		return imageDescriptorRegistry;
	}

	/**
	 * Returns then singleton instance of {@link BeansModelLabelProvider}.
	 * <p>
	 * <b>For this instance the dispose method must never be called!! This is done by {@link Plugin.stop()} instead.</b>
	 */
	public static ILabelProvider getLabelProvider() {
		return getDefault().internalGetLabelProvider();
	}

	private synchronized ILabelProvider internalGetLabelProvider() {
		if (labelProvider == null) {
			labelProvider = new DecoratingLabelProvider(new BeansModelLabelProvider(true),
					new BeansModelLabelDecorator());
		}
		return labelProvider;
	}

	/**
	 * Returns an {@link ImageDescriptor} for the image file at the given plug-in relative path
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
		getDefault().getLog().log(createErrorStatus(getResourceString("Plugin.internal_error"), exception));
	}

	/**
	 * Returns a new {@link IStatus} for this plug-in
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
