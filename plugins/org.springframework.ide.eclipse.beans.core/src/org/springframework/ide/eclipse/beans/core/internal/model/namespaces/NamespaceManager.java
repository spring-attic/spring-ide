/*******************************************************************************
 * Copyright (c) 2009, 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.namespaces;

import java.io.IOException;
import java.util.Enumeration;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.ide.eclipse.beans.core.model.INamespaceDefinitionResolver;
import org.springframework.util.Assert;
import org.xml.sax.EntityResolver;

/**
 * Support class that deals with namespace parsers discovered inside Spring bundles.
 * 
 * @author Christian Dupuis
 * @author Costin Leau
 * @author Martin Lippert
 */
public class NamespaceManager {

	/** The set of all namespace plugins known to the extender */
	private ToolingAwareNamespacePlugins namespacePlugins;

	/**
	 * ServiceRegistration object returned by OSGi when registering the NamespacePlugins instance as
	 * a service
	 */
	private ServiceRegistration<?> nsResolverRegistration, enResolverRegistration = null;

	private ServiceRegistration<?> ndResolverRegistration;

	/** OSGi Environment */
	private final BundleContext context;


	private static final String META_INF = "META-INF/";

	private static final String SPRING_HANDLERS = "spring.handlers";

	private static final String SPRING_SCHEMAS = "spring.schemas";

	/**
	 * Constructs a new <code>NamespaceManager</code> instance.
	 * 
	 * @param context containing bundle context
	 */
	public NamespaceManager(BundleContext context) {
		this.context = context;
		// detect package admin
		this.namespacePlugins = new ToolingAwareNamespacePlugins();
	}

	/**
	 * Registers the namespace plugin handler if this bundle defines handler mapping or schema
	 * mapping resources.
	 * 
	 * <p/>
	 * This method considers only the bundle space and not the class space.
	 * 
	 * @param bundle target bundle
	 * @param isLazyBundle indicator if the bundle analyzed is lazily activated
	 */
	public void maybeAddNamespaceHandlerFor(Bundle bundle, boolean isLazyBundle) {
		// Ignore system bundle
		if (isSystemBundle(bundle)) {
			return;
		}

		// FIXME: RFC-124 big bundle temporary hack
		// since embedded libraries are not discovered by findEntries and inlining them doesn't work
		// (due to resource classes such as namespace handler definitions)
		// we use getResource

		boolean hasHandlers = false, hasSchemas = false;
		// extender/RFC 124 bundle
		if (context.getBundle().equals(bundle)) {

			try {
				Enumeration<?> handlers = bundle.getResources(META_INF + SPRING_HANDLERS);
				Enumeration<?> schemas = bundle.getResources(META_INF + SPRING_SCHEMAS);

				hasHandlers = handlers != null;
				hasSchemas = schemas != null;

			}
			catch (IOException ioe) {
			}
		}
		else {
			hasHandlers = bundle.findEntries(META_INF, SPRING_HANDLERS, false) != null;
			hasSchemas = bundle.findEntries(META_INF, SPRING_SCHEMAS, false) != null;
		}

		// if the bundle defines handlers
		if (hasHandlers) {

			if (isLazyBundle) {
				this.namespacePlugins.addPlugin(bundle, isLazyBundle, true);
			}
			else {
				// check type compatibility between the bundle's and spring-extender's spring
				// version
				if (hasCompatibleNamespaceType(bundle)) {
					this.namespacePlugins.addPlugin(bundle, isLazyBundle, false);
				}
			}
		}
		else {
			// bundle declares only schemas, add it though the handlers might not be compatible...
			if (hasSchemas)
				this.namespacePlugins.addPlugin(bundle, isLazyBundle, false);
		}
	}

	private boolean isSystemBundle(Bundle bundle) {
		Assert.notNull(bundle);
		return (bundle.getBundleId() == 0);
	}

	private boolean hasCompatibleNamespaceType(Bundle bundle) {
		return namespacePlugins.isTypeCompatible(bundle);
	}

	/**
	 * Removes the target bundle from the set of those known to provide handler or schema mappings.
	 * 
	 * @param bundle handler bundle
	 */
	public void maybeRemoveNameSpaceHandlerFor(Bundle bundle) {
		Assert.notNull(bundle);
		this.namespacePlugins.removePlugin(bundle);
	}

	/**
	 * Registers the NamespacePlugins instance as an Osgi Resolver service
	 */
	private void registerResolverServices() {
		nsResolverRegistration = context.registerService(
				new String[] { NamespaceHandlerResolver.class.getName() }, this.namespacePlugins,
				null);

		enResolverRegistration = context.registerService(new String[] { EntityResolver.class
				.getName() }, this.namespacePlugins, null);

		ndResolverRegistration = context.registerService(
				new String[] { INamespaceDefinitionResolver.class.getName() },
				this.namespacePlugins, null);
	}

	/**
	 * Unregisters the NamespaceHandler and EntityResolver service
	 */
	private void unregisterResolverService() {

		unregisterService(nsResolverRegistration);
		unregisterService(enResolverRegistration);
		unregisterService(ndResolverRegistration);
		
		this.nsResolverRegistration = null;
		this.enResolverRegistration = null;
		this.ndResolverRegistration = null;
	}

	public boolean unregisterService(ServiceRegistration<?> registration) {
		try {
			if (registration != null) {
				registration.unregister();
				return true;
			}
		}
		catch (IllegalStateException alreadyUnregisteredException) {
		}
		return false;
	}

    public ToolingAwareNamespacePlugins getNamespacePlugins() {
		return namespacePlugins;
	}

	//
	// Lifecycle methods
	//

	public void afterPropertiesSet() {
		registerResolverServices();
	}

	public void destroy() {
		unregisterResolverService();
		this.namespacePlugins.destroy();
		this.namespacePlugins = null;
	}
	
}