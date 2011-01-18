/*******************************************************************************
 * Copyright (c) 2009, 2010 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.namespaces;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import org.osgi.framework.Bundle;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.DelegatingEntityResolver;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.osgi.util.BundleDelegatingClassLoader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Spring schema handler/resolver for OSGi environments.
 * Besides delegation this class also does type filtering to avoid wiring the wrong bundle if multiple versions of the
 * same library (which support the same schema) are available.
 * Additionally, lazy handlers are supported so that they are checked (and thus loaded) only if no previous handler has
 * been able to satisfy the request.
 * @author Christian Dupuis
 * @author Hal Hildebrand
 * @author Costin Leau
 */
public class NamespacePlugins implements NamespaceHandlerResolver, EntityResolver, DisposableBean {

	private final LazyBundleRegistry.Activator<Plugin> activation = new LazyBundleRegistry.Activator<Plugin>() {

		public Plugin activate(Bundle bundle) {
			return new Plugin(bundle);
		}
	};

	final LazyBundleRegistry.Condition condition = new LazyBundleRegistry.Condition() {

		private final String NS_HANDLER_RESOLVER_CLASS_NAME = NamespaceHandlerResolver.class.getName();

		public boolean pass(Bundle bundle) {
			try {
				Class<?> type = bundle.loadClass(NS_HANDLER_RESOLVER_CLASS_NAME);
				return NamespaceHandlerResolver.class.equals(type);
			}
			catch (Throwable th) {
				// if the interface is not wired, ignore the bundle
				return false;
			}
		}

	};

	private final LazyBundleRegistry<Plugin> pluginRegistry = new LazyBundleRegistry<Plugin>(condition, activation);

	public void destroy() {
		pluginRegistry.clear();
	}

	public NamespaceHandler resolve(final String namespaceUri) {
		if (System.getSecurityManager() != null) {
			return AccessController.doPrivileged(new PrivilegedAction<NamespaceHandler>() {

				public NamespaceHandler run() {
					return doResolve(namespaceUri);
				}
			});

		}
		return doResolve(namespaceUri);
	}

	public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
		if (System.getSecurityManager() != null) {
			try {
				return AccessController.doPrivileged(new PrivilegedExceptionAction<InputSource>() {

					public InputSource run() throws Exception {
						return doResolveEntity(publicId, systemId);
					}
				});
			}
			catch (PrivilegedActionException pae) {
				Exception cause = pae.getException();
				handleInputSourceException(cause);
			}
		}
		else {
			try {
				return doResolveEntity(publicId, systemId);
			}
			catch (Exception ex) {
				handleInputSourceException(ex);
			}
		}

		return null;
	}

	private NamespaceHandler doResolve(final String namespaceUri) {
		try {
			return pluginRegistry.apply(new LazyBundleRegistry.Operation<Plugin, NamespaceHandler>() {

				public NamespaceHandler operate(Plugin plugin) {
					try {
						NamespaceHandler handler = plugin.resolve(namespaceUri);
						if (handler != null) {
							return handler;
						}
					}
					catch (IllegalArgumentException ex) {
					}
					return null;
				}
			});
		}
		catch (Exception ex) {
			// the inner method doesn't declare any exceptions so the cast should be safe
			throw (RuntimeException) ex;
		}
	}

	private InputSource doResolveEntity(final String publicId, final String systemId) throws Exception {
		if (systemId != null) {

			return pluginRegistry.apply(new LazyBundleRegistry.Operation<Plugin, InputSource>() {

				public InputSource operate(Plugin plugin) throws SAXException, IOException {
					try {
						InputSource inputSource = plugin.resolveEntity(publicId, systemId);

						if (inputSource != null) {
							return inputSource;
						}

					}
					catch (FileNotFoundException ex) {
					}
					return null;
				}
			});
		}
		return null;
	}

	private void handleInputSourceException(Exception exception) throws SAXException, IOException {
		if (exception instanceof RuntimeException) {
			throw (RuntimeException) exception;
		}
		if (exception instanceof IOException) {
			throw (IOException) exception;
		}
		throw (SAXException) exception;
	}

	/**
	 * Adds a bundle as a handler to plugin registry.
	 * @param bundle
	 * @param lazyBundle
	 */
	void addPlugin(Bundle bundle, boolean lazyBundle, boolean applyCondition) {
		pluginRegistry.add(bundle, lazyBundle, applyCondition);
	}

	/**
	 * Checks the type compatibility check between the namespace parser wired to Spring DM and the discovered bundle
	 * class space.
	 * @param bundle handler bundle
	 * @return true if there is type compatibility, false otherwise
	 */
	boolean isTypeCompatible(Bundle bundle) {
		return condition.pass(bundle);
	}

	/**
	 * Returns true if a handler mapping was removed for the given bundle.
	 * 
	 * @param bundle bundle to look at
	 * @return true if the bundle was used in the plugin map
	 */
	boolean removePlugin(Bundle bundle) {
		return pluginRegistry.remove(bundle);
	}

	/**
	 * Wrapper class which implements both {@link EntityResolver} and {@link NamespaceHandlerResolver} interfaces.
	 * 
	 * Simply delegates to the actual implementation discovered in a specific bundle.
	 */
	private static class Plugin implements NamespaceHandlerResolver, EntityResolver {

		private final Bundle bundle;

		private final EntityResolver entity;

		private final NamespaceHandlerResolver namespace;

		private Plugin(Bundle bundle) {
			this.bundle = bundle;

			ClassLoader loader = BundleDelegatingClassLoader.createBundleClassLoaderFor(bundle);

			entity = new DelegatingEntityResolver(loader);
			namespace = new DefaultNamespaceHandlerResolver(loader);
		}

		public Bundle getBundle() {
			return bundle;
		}

		public NamespaceHandler resolve(String namespaceUri) {
			return namespace.resolve(namespaceUri);
		}

		public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
			return entity.resolveEntity(publicId, systemId);
		}
	}

}