/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.osgibridge;

import java.io.IOException;
import java.util.Stack;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Uses Spring OSGi infrastructure services to locate {@link InputSource} by
 * searching for {@link EntityResolver} that are installed with any bundles.
 * @author Christian Dupuis
 * @since 2.0.1
 */
public class OsgiBridgingEntityResolver extends AbstractBundleContextAware
		implements EntityResolver {

	/**
	 * Resolve the system Id and return the located {@link InputSource}
	 * implementation.
	 * @return the located {@link InputSource} (may be <code>null</code>)
	 */
	public InputSource resolveEntity(final String publicId,
			final String systemId) throws SAXException, IOException {
		final Stack<InputSource> inputSources = new Stack<InputSource>();
		try {
			OsgiUtils.executeCallback(new OsgiUtils.OsgiServiceCallback() {

				public void doWithService(Object service) throws Exception {
					if (service instanceof EntityResolver) {
						InputSource inputSource = ((EntityResolver) service)
								.resolveEntity(publicId, systemId);
						if (inputSource != null) {
							inputSources.push(inputSource);
						}
					}
				}
			}, getBundleContext(), EntityResolver.class);
		}
		catch (Exception e) {
			// don't really care here
		}
		if (!inputSources.isEmpty()) {
			return inputSources.pop();
		}
		return null;
	}
}
