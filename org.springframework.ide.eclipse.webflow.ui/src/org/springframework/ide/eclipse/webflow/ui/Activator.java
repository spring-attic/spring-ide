/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.ui;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.*;
import org.osgi.framework.BundleContext;
import java.util.*;

/**
 * The main plugin class to be used in the desktop.
 *
 * @author Christian Dupuis
 * @since 2.0
 */
public class Activator extends AbstractUIPlugin {

    public static final String PLUGIN_ID =
    		"org.springframework.ide.eclipse.webflow.ui";

	public static final String PROJECT_EXPLORER_CONTENT_PROVIDER_ID = PLUGIN_ID
			+ ".navigator.projectExplorerContent";

	public static final String SPRING_EXPLORER_CONTENT_PROVIDER_ID = PLUGIN_ID
			+ ".navigator.springExplorerContent";

    private static final String RESOURCE_NAME = PLUGIN_ID + ".messages";

    /**
     * The shared instance.
     */
    private static Activator plugin;

    private ResourceBundle resourceBundle;

    /**
	 * The constructor.
	 */
    public Activator() {
        super();
        plugin = this;
        try {
            resourceBundle = ResourceBundle.getBundle(RESOURCE_NAME);
        }
        catch (MissingResourceException x) {
            resourceBundle = null;
        }
    }

    /**
     * This method is called upon plug-in activation.
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }

    /**
     * This method is called when the plug-in is stopped.
     */
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
    }

    /**
     * Returns the shared instance.
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Returns the string from the plugin's resource bundle, or 'key' if not
     * found.
     */
    public static String getResourceString(String key) {
        ResourceBundle bundle = Activator.getDefault()
                .getResourceBundle();
        try {
            return (bundle != null) ? bundle.getString(key) : key;
        }
        catch (MissingResourceException e) {
            return key;
        }
    }

    /**
     * Returns the plugin's resource bundle,.
     */
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

    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }

    /**
     * Writes the message to the plug-in's log.
     * 
     * @param message the text to write to the log
     */
    public static void log(String message, Throwable exception) {
        IStatus status = createErrorStatus(message, exception);
        getDefault().getLog().log(status);
    }

    public static void log(Throwable exception) {
        getDefault().getLog().log(
                createErrorStatus(getResourceString("Plugin.internal_error"),
                        exception));
    }

    /**
     * Returns a new {@link IStatus} for this plug-in.
     */
    public static IStatus createErrorStatus(String message) {
        if (message == null) {
            message = "";
        }
        return new Status(Status.ERROR, PLUGIN_ID, 0, message, null);
    }

    /**
     * Returns a new {@link IStatus} for this plug-in.
     */
    public static IStatus createErrorStatus(String message, Throwable exception) {
        if (message == null) {
            message = "";
        }
        return new Status(Status.ERROR, PLUGIN_ID, 0, message, exception);
    }
}
