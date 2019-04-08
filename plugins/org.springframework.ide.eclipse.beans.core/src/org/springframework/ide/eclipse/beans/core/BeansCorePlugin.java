/*******************************************************************************
 * Copyright (c) 2004, 2015 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core;

import java.util.Dictionary;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.Version;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.core.MessageUtils;
import org.springframework.ide.eclipse.xml.namespaces.NamespaceManagerProvider;
import org.springframework.ide.eclipse.xml.namespaces.SpringXmlNamespacesPlugin;

/**
 * Central access point for the Spring Framework Core plug-in (id
 * <code>"org.springframework.ide.eclipse.beans.core"</code>).
 * 
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @author Tomasz Zarna
 * @author Martin Lippert
 */
public class BeansCorePlugin extends AbstractUIPlugin {

	/**
	 * Plugin identifier for Spring Beans Core (value <code>org.springframework.ide.eclipse.beans.core</code>).
	 */
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.beans.core";

	private static final String RESOURCE_NAME = PLUGIN_ID + ".messages";

	/** preference key for defining the parsing timeout */
	public static final String TIMEOUT_CONFIG_LOADING_PREFERENCE_ID = PLUGIN_ID + ".timeoutConfigLoading";

	/** preference key to enable namespace versions per namespace */
	public static final String PROJECT_PROPERTY_ID = "enable.project.preferences";

	/** The shared instance */
	private static BeansCorePlugin plugin;

	/** The singleton beans model */
	private BeansModel model;

	/** Internal executor service */
	private ExecutorService executorService;
	private AtomicInteger threadCount = new AtomicInteger(0);
	private static final String THREAD_NAME_TEMPLATE = "Background Thread-%s (%s/%s.%s.%s)";

	/**
	 * Preference ID to globally disable any beans auto detection scanning.
	 */
	public static final String DISABLE_AUTO_DETECTION = BeansCorePlugin.class.getName()+".DISABLE_AUTO_DETECTION";

	/** Resource bundle */
	private ResourceBundle resourceBundle;

	/**
	 * flag indicating whether the context is down or not - useful during shutdown
	 */
	private volatile boolean isClosed = false;

	/**
	 * Monitor used for dealing with the bundle activator and synchronous bundle threads
	 */
	private transient final Object monitor = new Object();

	/**
	 * Creates the Spring Beans Core plug-in.
	 * <p>
	 * The plug-in instance is created automatically by the Eclipse platform. Clients must not call.
	 */
	public BeansCorePlugin() {
		plugin = this;
		model = new BeansModel();
		
		try {
			resourceBundle = ResourceBundle.getBundle(RESOURCE_NAME);
		}
		catch (MissingResourceException e) {
			resourceBundle = null;
		}
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		
//		Hashtable<String, String> properties = new Hashtable<String, String>();
//		properties.put(URLConstants.URL_HANDLER_PROTOCOL,
//				ProjectAwareUrlStreamHandlerService.PROJECT_AWARE_PROTOCOL);
//		projectAwareUrlService = context.registerService(
//				URLStreamHandlerService.class.getName(),
//				new ProjectAwareUrlStreamHandlerService(), properties);
		
		executorService = Executors.newCachedThreadPool(new ThreadFactory() {
			
			public Thread newThread(Runnable runnable) {
				Version version = Version.parseVersion(getPluginVersion());
				String productId = "Spring IDE";
				IProduct product = Platform.getProduct();
				if (product != null && "com.springsource.sts".equals(product.getId()))
						productId = "STS";
				Thread reportingThread = new Thread(runnable, String.format(THREAD_NAME_TEMPLATE, threadCount.incrementAndGet(), 
						productId, version.getMajor(), version.getMinor(), version.getMicro()));
				reportingThread.setDaemon(true);
				return reportingThread;
			}
		});

		
//		nsManager = new NamespaceManager(context);
		getPreferenceStore().setDefault(TIMEOUT_CONFIG_LOADING_PREFERENCE_ID, 60);
//		getPreferenceStore().setDefault(NAMESPACE_DEFAULT_FROM_CLASSPATH_ID, true);

		Job modelJob = new Job("Initializing Spring Tooling") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				model.start();
				return Status.OK_STATUS;
			}
		};
		modelJob.setRule(MultiRule.combine(ResourcesPlugin.getWorkspace().getRoot(), BeansCoreUtils.BEANS_MODEL_INIT_RULE));
		modelJob.setPriority(Job.DECORATE);
		NamespaceManagerProvider.get().nameSpaceHandlersReady().thenAccept(_void_ -> {
			modelJob.schedule();
		});
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		synchronized (monitor) {
			// if already closed, bail out
			if (isClosed) {
				return;
			}
			isClosed = true;
		}
		model.stop();
//		if (projectAwareUrlService != null) {
//			projectAwareUrlService.unregister();
//		}
		super.stop(context);
	}

	/**
	 * Returns the shared instance.
	 */
	public static BeansCorePlugin getDefault() {
		return plugin;
	}
	
	/**
	 * Returns the singleton {@link IBeansModel}.
	 */
	public static IBeansModel getModel() {
		return getDefault().model;
	}

	/**
	 * only for internal testing purposes
	 */
	public static void setModel(BeansModel model) {
		getDefault().model = model;
	}

	public static ExecutorService getExecutorService() {
		return getDefault().executorService;
	}

	/**
	 * Returns the {@link IWorkspace} instance.
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

	public static ClassLoader getClassLoader() {
		return BeansCorePlugin.class.getClassLoader();
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
	 * @param message the text to write to the log
	 */
	public static void log(String message, Throwable exception) {
		IStatus status = createErrorStatus(message, exception);
		getDefault().getLog().log(status);
	}

	public static void log(Throwable exception) {
		getDefault().getLog().log(createErrorStatus(getResourceString("Plugin.internal_error"), exception));
	}

	public static void logAsWarning(Throwable exception) {
		getDefault().getLog().log(createWarningStatus(getResourceString("Plugin.internal_warning"), exception));
	}

	public static IStatus createErrorStatus(String message, Throwable exception) {
		return createStatus(IStatus.ERROR, message, exception);
	}

	public static IStatus createWarningStatus(String message, Throwable exception) {
		return createStatus(IStatus.WARNING, message, exception);
	}
	
	public static IStatus createStatus(int severity, String message, Throwable exception) {
		return new Status(severity, PLUGIN_ID, 0, message == null ? "" : message, exception);
	}

	public static String getFormattedMessage(String key, Object... args) {
		return MessageUtils.format(getResourceString(key), args);
	}

	public static String getPluginVersion() {
		Bundle bundle = getDefault().getBundle();
		return bundle.getHeaders().get(Constants.BUNDLE_VERSION);
	}

	public boolean isAutoDetectionEnabled() {
		return !getPreferenceStore().getBoolean(DISABLE_AUTO_DETECTION);
	}
}
