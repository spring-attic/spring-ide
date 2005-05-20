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

package org.springframework.ide.eclipse.web.flow.ui;

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
 */
public class WebFlowUIPlugin extends AbstractUIPlugin {

    //The shared instance.
    private static WebFlowUIPlugin plugin;

    //Resource bundle.
    private ResourceBundle resourceBundle;

    public static final String PLUGIN_ID = "org.springframework.ide.eclipse.web.flow.ui";

    private static final String RESOURCE_NAME = PLUGIN_ID + ".messages";

    /**
     * The constructor.
     */
    public WebFlowUIPlugin() {
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
     * This method is called upon plug-in activation
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
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
    public static WebFlowUIPlugin getDefault() {
        return plugin;
    }

    /**
     * Returns the string from the plugin's resource bundle, or 'key' if not
     * found.
     */
    public static String getResourceString(String key) {
        ResourceBundle bundle = WebFlowUIPlugin.getDefault()
                .getResourceBundle();
        try {
            return (bundle != null) ? bundle.getString(key) : key;
        }
        catch (MissingResourceException e) {
            return key;
        }
    }

    /**
     * Returns the plugin's resource bundle,
     */
    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    public static void log(IStatus status) {
        getDefault().getLog().log(status);
    }

    /**
     * Writes the message to the plug-in's log
     * 
     * @param message
     *            the text to write to the log
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
     * Returns a new <code>IStatus</code> for this plug-in
     */
    public static IStatus createErrorStatus(String message) {
        if (message == null) {
            message = "";
        }
        return new Status(Status.ERROR, PLUGIN_ID, 0, message, null);
    }

    /**
     * Returns a new <code>IStatus</code> for this plug-in
     */
    public static IStatus createErrorStatus(String message, Throwable exception) {
        if (message == null) {
            message = "";
        }
        return new Status(Status.ERROR, PLUGIN_ID, 0, message, exception);
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
}
