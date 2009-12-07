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
package org.springframework.ide.eclipse.beans.core;

import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
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
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.beans.core.internal.model.metadata.BeanMetadataModel;
import org.springframework.ide.eclipse.beans.core.internal.model.namespaces.NamespaceManager;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.INamespaceDefinitionListener;
import org.springframework.ide.eclipse.beans.core.model.INamespaceDefinitionResolver;
import org.springframework.ide.eclipse.beans.core.model.metadata.IBeanMetadataModel;
import org.springframework.ide.eclipse.core.MessageUtils;
import org.springframework.osgi.util.OsgiBundleUtils;

/**
 * Central access point for the Spring Framework Core plug-in (id
 * <code>"org.springframework.ide.eclipse.beans.core"</code>).
 * 
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeansCorePlugin extends AbstractUIPlugin {

	/**
	 * Plugin identifier for Spring Beans Core (value <code>org.springframework.ide.eclipse.beans.core</code>).
	 */
	public static final String PLUGIN_ID = "org.springframework.ide.eclipse.beans.core";

	private static final String RESOURCE_NAME = PLUGIN_ID + ".messages";

	/** preference key to suppress missing namespace handler warnings */
	public static final String IGNORE_MISSING_NAMESPACEHANDLER_PROPERTY = "ignoreMissingNamespaceHandler";

	/** preference key for defining the parsing timeout */
	public static final String TIMEOUT_CONFIG_LOADING_PREFERENCE_ID = PLUGIN_ID + ".timeoutConfigLoading";

	/** preference key to enable namespace versions per namespace */
	public static final String NAMESPACE_VERSION_PROJECT_PREFERENCE_ID = "enable.namespace.versions";

	/** preference key to specify the default namespace version */
	public static final String NAMESPACE_DEFAULT_VERSION_PREFERENCE_ID = "default.version.";

	/** preference key to specify if versions should be taken from the classpath */
	public static final String NAMESPACE_DEFAULT_FROM_CLASSPATH_ID = "default.version.check.classpath";

	/** The shared instance */
	private static BeansCorePlugin plugin;

	/** The singleton beans model */
	private BeansModel model;

	private BeanMetadataModel metadataModel;

	/** Spring namespace/resolver manager */
	private NamespaceManager nsManager;

	private NamespaceBundleLister nsListener;

	/** Internal executor service */
	private ExecutorService executorService;

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
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		
		nsManager = new NamespaceManager(context);
		getPreferenceStore().setDefault(TIMEOUT_CONFIG_LOADING_PREFERENCE_ID, 60);
		getPreferenceStore().setDefault(NAMESPACE_DEFAULT_FROM_CLASSPATH_ID, true);

		Job modelJob = new Job("Initializing Spring Tooling") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				initNamespaceHandlers(context);

				model.start();
				metadataModel.start();

				return Status.OK_STATUS;
			}
		};
		// modelJob.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		// modelJob.setSystem(true);
		modelJob.setPriority(Job.INTERACTIVE);
		modelJob.schedule();
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
		return getDefault().model;
	}

	public static final INamespaceDefinitionResolver getNamespaceDefinitionResolver() {
		return getDefault().nsManager.getNamespacePlugins();
	}

	public static final IBeanMetadataModel getMetadataModel() {
		return getDefault().metadataModel;
	}

	public static final ExecutorService getExecutorService() {
		return getDefault().executorService;
	}
	
	public static final void registerNamespaceDefinitionListener(INamespaceDefinitionListener listener) {
		getDefault().nsManager.getNamespacePlugins().registerNamespaceDefinitionListener(listener);
	}

	public static final void unregisterNamespaceDefinitionListener(INamespaceDefinitionListener listener) {
		getDefault().nsManager.getNamespacePlugins().unregisterNamespaceDefinitionListener(listener);
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
		getDefault().getLog().log(createErrorStatus(getResourceString("Plugin.internal_error"), exception));
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

	protected void maybeAddNamespaceHandlerFor(final Bundle bundle, final boolean isLazy) {
		nsManager.maybeAddNamespaceHandlerFor(bundle, isLazy);
	}

	protected void maybeRemoveNameSpaceHandlerFor(final Bundle bundle) {
		nsManager.maybeRemoveNameSpaceHandlerFor(bundle);
	}

	protected void initNamespaceHandlers(BundleContext context) {

		// register listener first to make sure any bundles in INSTALLED state
		// are not lost
		nsListener = new NamespaceBundleLister();
		context.addBundleListener(nsListener);

		Bundle[] previousBundles = context.getBundles();

		for (int i = 0; i < previousBundles.length; i++) {
			Bundle bundle = previousBundles[i];
			if (OsgiBundleUtils.isBundleResolved(bundle)) {
				nsManager.maybeAddNamespaceHandlerFor(bundle, false);
			}
			else if (OsgiBundleUtils.isBundleLazyActivated(bundle)) {
				nsManager.maybeAddNamespaceHandlerFor(bundle, true);
			}
		}

		// discovery finished, publish the resolvers/parsers in the OSGi space
		nsManager.afterPropertiesSet();
	}

	/**
	 * Common base class for {@link ContextLoaderListener} listeners.
	 */
	private abstract class BaseListener implements BundleListener {

		/**
		 * common cache used for tracking down bundles started lazily so they don't get processed twice (once when
		 * started lazy, once when started fully)
		 */
		protected Map<Bundle, Object> lazyBundleCache = new WeakHashMap<Bundle, Object>();

		/** dummy value for the bundle cache */
		private final Object VALUE = new Object();

		// caches the bundle
		protected void push(Bundle bundle) {
			synchronized (lazyBundleCache) {
				lazyBundleCache.put(bundle, VALUE);
			}
		}

		// checks the presence of the bundle as well as removing it
		protected boolean pop(Bundle bundle) {
			synchronized (lazyBundleCache) {
				return (lazyBundleCache.remove(bundle) != null);
			}
		}

		/**
		 * A bundle has been started, stopped, resolved, or unresolved. This method is a synchronous callback, do not do
		 * any long-running work in this thread.
		 * 
		 * @see org.osgi.framework.SynchronousBundleListener#bundleChanged
		 */
		public void bundleChanged(BundleEvent event) {

			// check if the listener is still alive
			if (isClosed) {
				return;
			}
			try {
				handleEvent(event);
			}
			catch (Exception ex) {
				log(ex);
			}
		}

		protected abstract void handleEvent(BundleEvent event);
	}

	/**
	 * Bundle listener used for detecting namespace handler/resolvers. Exists as a separate listener so that it can be
	 * registered early to avoid race conditions with bundles in INSTALLING state but still to avoid premature context
	 * creation before the Spring {@link ContextLoaderListener} is not fully initialized.
	 */
	private class NamespaceBundleLister extends BaseListener {

		@Override
		protected void handleEvent(final BundleEvent event) {
			Bundle bundle = event.getBundle();

			switch (event.getType()) {
			case BundleEvent.LAZY_ACTIVATION: {
				push(bundle);
				maybeAddNamespaceHandlerFor(bundle, true);
				break;
			}
			case BundleEvent.RESOLVED: {
				if (!pop(bundle)) {
					maybeAddNamespaceHandlerFor(bundle, false);
				}
				break;
			}
			case BundleEvent.STOPPED: {
				pop(bundle);
				maybeRemoveNameSpaceHandlerFor(bundle);
				break;
			}
			default:
				break;
			}
		}
	}
}
