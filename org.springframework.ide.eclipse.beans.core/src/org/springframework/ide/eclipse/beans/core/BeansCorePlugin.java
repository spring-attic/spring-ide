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
package org.springframework.ide.eclipse.beans.core;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.internal.model.metadata.BeanMetadataModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.metadata.IBeanMetadataModel;
import org.springframework.ide.eclipse.core.MessageUtils;

/**
 * Central access point for the Spring Framework Core plug-in (id
 * <code>"org.springframework.ide.eclipse.beans.core"</code>).
 * 
 * @author Torsten Juergeleit
 */
public class BeansCorePlugin extends AbstractUIPlugin {

	/**
	 * Plugin identifier for Spring Beans Core (value
	 * <code>org.springframework.ide.eclipse.beans.core</code>).
	 */
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.beans.core";

	private static final String RESOURCE_NAME = PLUGIN_ID + ".messages";

	/** preference key to suppress missing namespace handler warnings */
	public static final String IGNORE_MISSING_NAMESPACEHANDLER_PROPERTY = "ignoreMissingNamespaceHandler";

	public static final String TIMEOUT_CONFIG_LOADING_PREFERENCE_ID = PLUGIN_ID
			+ ".timeoutConfigLoading";

	/** The shared instance */
	private static BeansCorePlugin plugin;

	/** The singleton beans model */
	private static BeansModel model;

	private static BeanMetadataModel metadataModel;

	/** Internal executor service */
	private static ExecutorService executorService;

	/** Resource bundle */
	private ResourceBundle resourceBundle;

	/**
	 * Creates the Spring Beans Core plug-in.
	 * <p>
	 * The plug-in instance is created automatically by the Eclipse platform. Clients must not call.
	 */
	public BeansCorePlugin() {
		plugin = this;
		model = new BeansModel();
		metadataModel = new BeanMetadataModel();
		executorService = Executors.newCachedThreadPool();
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

		getPreferenceStore().setDefault(TIMEOUT_CONFIG_LOADING_PREFERENCE_ID, 60);

		// context.registerService(EventHandler.class.getName(),
		// new NamespaceEventHandler(),
		// getHandlerServiceProperties("org/springframework/osgi/extender/namespace/*"));

		// // Testing
		// ICatalog systemCatalog = null;
		// ICatalog defaultCatalog = XMLCorePlugin.getDefault().getDefaultXMLCatalog();
		// INextCatalog[] nextCatalogs = defaultCatalog.getNextCatalogs();
		// for (int i = 0; i < nextCatalogs.length; i++) {
		// INextCatalog catalog = nextCatalogs[i];
		// ICatalog referencedCatalog = catalog.getReferencedCatalog();
		// if (referencedCatalog != null) {
		// if (XMLCorePlugin.SYSTEM_CATALOG_ID.equals(referencedCatalog.getId())) {
		// systemCatalog = referencedCatalog;
		// }
		// }
		// }
		//
		// // <system
		// // systemId="http://www.springframework.org/schema/aop/spring-aop-2.0.xsd"
		// //
		// uri="platform:/plugin/org.springframework.bundle.spring/org/springframework/aop/config/spring-aop-2.0.xsd"
		// // />
		//
		// String key = "http://www.springframework.org/schema/batch/spring-batch-2.0.xsd";
		// int type = ICatalogEntry.ENTRY_TYPE_SYSTEM;
		//
		// ICatalogElement catalogElement = systemCatalog.createCatalogElement(type);
		// if (catalogElement instanceof ICatalogEntry) {
		// ICatalogEntry entry = (ICatalogEntry) catalogElement;
		// entry.setKey(key);
		// String resolvedPath =
		// "file:///Users/cdupuis/Development/Java/lib/spring-projects-svn/spring-batch/trunk/spring-batch-core/src/main/resources/org/springframework/batch/core/configuration/xml/spring-batch-2.0.xsd";
		// entry.setURI(resolvedPath);
		//			
		//			
		// DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		// factory.setValidating(false);
		// factory.setNamespaceAware(false);
		// DocumentBuilder docBuilder = factory.newDocumentBuilder();
		// Document doc = docBuilder.parse(new File(new URI(resolvedPath)));
		//			
		// String namespace = doc.getDocumentElement().getAttribute("targetNamespace");
		// System.out.println(namespace);
		// }

		// systemCatalog.addCatalogElement(catalogElement);
		
		Job modelJob = new Job("Initializing Beans Model") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				model.start();
				metadataModel.start();

				return Status.OK_STATUS;
			}
		};
//		modelJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		modelJob.setSystem(true);
		modelJob.setPriority(Job.INTERACTIVE);
		modelJob.schedule();
	}

	// protected Dictionary getHandlerServiceProperties(String... topics) {
	// Dictionary result = new Hashtable();
	// result.put(EventConstants.EVENT_TOPIC, topics);
	// return result;
	// }

	@Override
	public void stop(BundleContext context) throws Exception {
		model.stop();
		metadataModel.stop();
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static final BeansCorePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the singleton {@link IBeansModel}.
	 */
	public static final IBeansModel getModel() {
		return model;
	}

	public static final IBeanMetadataModel getMetadataModel() {
		return metadataModel;
	}

	public static final ExecutorService getExecutorService() {
		return executorService;
	}

	/**
	 * Returns the {@link IWorkspace} instance.
	 */
	public static final IWorkspace getWorkspace() {
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
		return MessageUtils.format(getResourceString(key), args);
	}

	public static String getPluginVersion() {
		Bundle bundle = getDefault().getBundle();
		return (String) bundle.getHeaders().get(Constants.BUNDLE_VERSION);
	}

	// class NamespaceEventHandler implements EventHandler {
	//
	// public void handleEvent(Event event) {
	// System.out.println(event + " " + event.getProperty(Constants.BUNDLE_SYMBOLICNAME));
	// }
	//
	// }
}
