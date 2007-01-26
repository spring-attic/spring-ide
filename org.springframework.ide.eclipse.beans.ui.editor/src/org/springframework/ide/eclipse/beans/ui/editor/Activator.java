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

package org.springframework.ide.eclipse.beans.ui.editor;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wst.sse.ui.internal.provisional.registry.AdapterFactoryRegistry;
import org.eclipse.wst.sse.ui.internal.provisional.registry.AdapterFactoryRegistryImpl;
import org.osgi.framework.BundleContext;
import org.springframework.ide.eclipse.beans.ui.editor.templates.BeansTemplateContextTypeIds;

/**
 * The main plugin class
 */
@SuppressWarnings("restriction")
public class Activator extends AbstractUIPlugin {

	/**
	 * Plugin identifier for Spring Beans UI (value
	 * <code>org.springframework.ide.eclipse.beans.ui</code>).
	 */
	public static final String PLUGIN_ID =
							 "org.springframework.ide.eclipse.beans.ui.editor";
	private static final String RESOURCE_NAME = PLUGIN_ID + ".messages";

	/** Key to store our templates. */
	private static final String TEMPLATES_KEY = PLUGIN_ID + ".templates";

	/** The shared instance. */
	private static Activator plugin;

	private ResourceBundle resourceBundle;
	private ContextTypeRegistry contextTypeRegistry;
	private TemplateStore templateStore;
	
	private BundleContext context;

	/**
	 * Creates the Spring Beans Editor plug-in.
	 * <p>
	 * The plug-in instance is created automatically by the Eclipse platform.
	 * Clients must not call.
	 */
	public Activator() {
		plugin = this;

		try {
			resourceBundle = ResourceBundle.getBundle(RESOURCE_NAME);
		} catch (MissingResourceException e) {
			log(e);
			resourceBundle = null;
		}
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		this.context = context;
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
		resourceBundle = null;
		this.context = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static Activator getDefault() {
		return plugin;
	}

	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}
	
	public BundleContext getBundleContext() {
		return context;
	}

	public AdapterFactoryRegistry getAdapterFactoryRegistry() {
		return AdapterFactoryRegistryImpl.getInstance();

	}

	/**
	 * Returns the template context type registry for the Spring beans editor.
	 * 
	 * @return the template context type registry for the Spring beans editor
	 */
	public ContextTypeRegistry getTemplateContextRegistry() {
		if (contextTypeRegistry == null) {
			ContributionContextTypeRegistry registry =
										 new ContributionContextTypeRegistry();
			registry.addContextType(BeansTemplateContextTypeIds.ALL);
			registry.addContextType(BeansTemplateContextTypeIds.PROPERTY);
			registry.addContextType(BeansTemplateContextTypeIds.BEAN);
			contextTypeRegistry = registry;
		}
		return contextTypeRegistry;
	}

	/**
	 * Returns the template store for the Spring beans editor.
	 * 
	 * @return the template store for the Spring beans editor
	 */
	public TemplateStore getTemplateStore() {
		if (templateStore == null) {
			templateStore = new ContributionTemplateStore(
							getTemplateContextRegistry(), getPreferenceStore(),
							TEMPLATES_KEY);
			try {
				templateStore.load();
			} catch (IOException e) {
				log(e);
			}
		}
		return templateStore;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path.
	 * @param path  the path of the image file
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
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

	public static String getFormattedMessage(String key, String arg) {
		return getFormattedMessage(key, new String[] { arg });
	}

	public static String getFormattedMessage(String key, String[] args) {
		return MessageFormat.format(getResourceString(key), (Object[]) args);
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
	 * Returns a new <code>IStatus</code> for this plug-in
	 */
	public static IStatus createErrorStatus(String message,
											Throwable exception) {
		if (message == null) {
			message= ""; 
		}		
		return new Status(Status.ERROR, PLUGIN_ID, 0, message, exception);
	}
}
