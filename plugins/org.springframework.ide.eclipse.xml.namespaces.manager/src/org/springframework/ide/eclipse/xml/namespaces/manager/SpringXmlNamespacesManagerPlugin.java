/*******************************************************************************
 * Copyright (c) 2019 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.xml.namespaces.manager;

import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.ide.eclipse.xml.namespaces.model.INamespaceDefinitionListener;
import org.springframework.ide.eclipse.xml.namespaces.model.INamespaceDefinitionResolver;

public class SpringXmlNamespacesManagerPlugin extends Plugin {
	
	private static final String PLUGIN_ID = "org.springframework.ide.eclipse.xml.namespaces.manager";

	private static SpringXmlNamespacesManagerPlugin plugin;
	
	/**
	 * Monitor used for dealing with the bundle activator and synchronous bundle threads
	 */
	private transient final Object monitor = new Object();

	/**
	 * flag indicating whether the context is down or not - useful during shutdown
	 */
	private volatile boolean isClosed = false;

	/** Spring namespace/resolver manager */
	private NamespaceManager nsManager;
	
	public final static CompletableFuture<Void> nameSpaceHandlersReady = new CompletableFuture<>();

	public SpringXmlNamespacesManagerPlugin() {
		plugin = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		nsManager = new NamespaceManager(context);
		Job modelJob = new Job("Initializing Spring Xml Namespace Tooling") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				initNamespaceHandlers(context);
				nameSpaceHandlersReady.complete(null);
				return Status.OK_STATUS;
			}
		};
		modelJob.setPriority(Job.DECORATE);
		modelJob.setRule(ResourcesPlugin.getWorkspace().getRoot());
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
	}
	
	public static NamespaceHandlerResolver getNamespaceHandlerResolver() {
		return getDefault().nsManager.getNamespacePlugins();
	}
	
	/**
	 * Returns the shared instance.
	 */
	public static SpringXmlNamespacesManagerPlugin getDefault() {
		return plugin;
	}
	
	public static INamespaceDefinitionResolver getNamespaceDefinitionResolver() {
		return getDefault().nsManager.getNamespacePlugins();
	}

	public static void registerNamespaceDefinitionListener(INamespaceDefinitionListener listener) {
		getDefault().nsManager.getNamespacePlugins().registerNamespaceDefinitionListener(listener);
		getDefault().namespaceDefinitionListeners.add(listener);
	}

	public static void unregisterNamespaceDefinitionListener(INamespaceDefinitionListener listener) {
		getDefault().nsManager.getNamespacePlugins().unregisterNamespaceDefinitionListener(listener);
		getDefault().namespaceDefinitionListeners.remove(listener);
	}
	
	/** Listeners to inform about namespace changes */
	private volatile Set<INamespaceDefinitionListener> namespaceDefinitionListeners = Collections
			.synchronizedSet(new HashSet<INamespaceDefinitionListener>());

	public static void notifyNamespaceDefinitionListeners(IProject project) {
		for (INamespaceDefinitionListener listener : getDefault().namespaceDefinitionListeners) {
			listener.onNamespaceDefinitionRegistered(new INamespaceDefinitionListener.NamespaceDefinitionChangeEvent(
					null, project));
		}
	}

	protected void maybeAddNamespaceHandlerFor(Bundle bundle, boolean isLazy) {
		nsManager.maybeAddNamespaceHandlerFor(bundle, isLazy);
	}

	protected void maybeRemoveNameSpaceHandlerFor(Bundle bundle) {
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
			if (isBundleResolved(bundle)) {
				nsManager.maybeAddNamespaceHandlerFor(bundle, false);
			}
			else if (isBundleLazyActivated(bundle)) {
				nsManager.maybeAddNamespaceHandlerFor(bundle, true);
			}
		}

		// discovery finished, publish the resolvers/parsers in the OSGi space
		nsManager.afterPropertiesSet();
	}

	public boolean isBundleResolved(Bundle bundle) {
		return (bundle.getState() >= Bundle.RESOLVED);
	}

	public boolean isBundleLazyActivated(Bundle bundle) {
		if (bundle.getState() == Bundle.STARTING) {
			Dictionary<String, String> headers = bundle.getHeaders();
			if (headers != null && headers.get(Constants.BUNDLE_ACTIVATIONPOLICY) != null) {
				String value = headers.get(Constants.BUNDLE_ACTIVATIONPOLICY).trim();
				return (value.startsWith(Constants.ACTIVATION_LAZY));
			}
		}
		return false;
	}

	private NamespaceBundleLister nsListener;

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

	public static void log(Throwable exception) {
		getDefault().getLog().log(createErrorStatus("Plugin.internal_error", exception));
	}

	public static IStatus createErrorStatus(String message, Throwable exception) {
		return createStatus(IStatus.ERROR, message, exception);
	}

	public static IStatus createStatus(int severity, String message, Throwable exception) {
		return new Status(severity, PLUGIN_ID, 0, message == null ? "" : message, exception);
	}


}
