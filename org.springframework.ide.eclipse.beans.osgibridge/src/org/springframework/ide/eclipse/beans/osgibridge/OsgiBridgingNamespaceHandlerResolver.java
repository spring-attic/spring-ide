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
package org.springframework.ide.eclipse.beans.osgibridge;

import java.util.Stack;

import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.osgi.context.support.OsgiBundleNamespaceHandlerAndEntityResolver;

/**
 * Uses Spring OSGi infrastructure services to locate installed
 * {@link NamespaceHandlerResolver} with any bundles.
 * @author Christian Dupuis
 * @since 2.0.1
 */
public class OsgiBridgingNamespaceHandlerResolver extends AbstractBundleContextAware
		implements NamespaceHandlerResolver {

	/**
	 * Resolve the namespace URI and return the located {@link NamespaceHandler}
	 * implementation.
	 * @param namespaceUri the relevant namespace URI
	 * @return the located {@link NamespaceHandler} (may be <code>null</code>)
	 */
	public NamespaceHandler resolve(final String namespaceUri) {
		final Stack<NamespaceHandler> handlers = new Stack<NamespaceHandler>();
		try {
			OsgiUtils.executeCallback(
					new OsgiUtils.OsgiServiceCallback() {

						public void doWithService(Object service) {
							if (service instanceof NamespaceHandlerResolver) {
								NamespaceHandler handler = ((NamespaceHandlerResolver) service)
										.resolve(namespaceUri);
								if (handler != null) {
									handlers.push(handler);
								}
							}
						}
					}, getBundleContext(),
					OsgiBundleNamespaceHandlerAndEntityResolver.class);
		}
		catch (Exception e) {
			// don't really care here
		}
		if (!handlers.isEmpty()) {
			return handlers.pop();
		}
		return null;
	}
}
