/*******************************************************************************
 * Copyright (c) 2008, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.namespaces;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Set;

import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolver;
import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolverPlugin;
import org.eclipse.wst.xml.core.internal.XMLCorePlugin;
import org.eclipse.wst.xml.core.internal.catalog.provisional.ICatalog;
import org.springframework.beans.factory.xml.DelegatingEntityResolver;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.namespaces.NamespaceUtils;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Extension to Spring's {@link DelegatingEntityResolver} that tries to resolve entities from the
 * Eclipse XML Catalog as well as from other {@link EntityResolver}s that are available as OSGi
 * services.
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0.3
 */
@SuppressWarnings("restriction")
public class XmlCatalogDelegatingEntityResolver extends DelegatingEntityResolver {

	/** Internal list of already known {@link EntityResolver}s */
	private final Set<EntityResolver> entityResolvers;

	public XmlCatalogDelegatingEntityResolver(EntityResolver dtdResolver,
			EntityResolver schemaResolver) {
		super(dtdResolver, schemaResolver);
		this.entityResolvers = NamespaceUtils.getEntityResolvers();
	}

	/**
	 * Resolve an {@link InputSource} for the given publicId and systemId.
	 * <p>
	 * This implementation firstly delegates to the implementation in the
	 * {@link DelegatingEntityResolver}. If that doesn't resolve to an {@link InputSource} the
	 * Eclipse XML Catalog will be checked. As a last fall back the OSGi service registry is being
	 * queried for available {@link EntityResolver}s.
	 */
	@Override
	public InputSource resolveEntity(String publicId, String systemId) throws SAXException,
			IOException {
		InputSource inputSource = super.resolveEntity(publicId, systemId);
		if (inputSource != null) {
			return inputSource;
		}

		inputSource = resolveEntityViaXmlCatalog(publicId, systemId);
		if (inputSource != null) {
			return inputSource;
		}

		for (EntityResolver entityResolver : this.entityResolvers) {
			try {
				inputSource = entityResolver.resolveEntity(publicId, systemId);
				if (inputSource != null) {
					return inputSource;
				}
			}
			catch (Exception e) {
				// Make sure a contributed EntityResolver can't prevent parsing
				BeansCorePlugin.log(e);
			}
		}
		
		// Delegate to WTP to resolve over the XML Catalog and Cache
		URIResolver resolver = URIResolverPlugin.createResolver();
		String uri = resolver.resolvePhysicalLocation(null, publicId, systemId);
		if (uri != null) {
			URI realUri = URI.create(uri);
			if (realUri.getScheme() == "file") {
				inputSource = new InputSource(new FileInputStream(new File(URI.create(uri))));
			}
		}
		
		return inputSource;
	}

	/**
	 * Resolves an {@link InputSource} for a given publicId and systemId from the Eclipse XML
	 * Catalog.
	 * @see ICatalog
	 */
	private InputSource resolveEntityViaXmlCatalog(String publicId, String systemId) {
		ICatalog catalog = XMLCorePlugin.getDefault().getDefaultXMLCatalog();
		if (systemId != null) {
			try {
				String resolvedSystemId = catalog.resolveSystem(systemId);
				if (resolvedSystemId == null) {
					resolvedSystemId = catalog.resolveURI(systemId);
				}
				if (resolvedSystemId != null) {
					return new InputSource(resolvedSystemId);
				}
			}
			catch (MalformedURLException me) {
				// ignore
			}
			catch (IOException ie) {
				// ignore
			}
		}
		if (publicId != null) {
			if (!(systemId != null && systemId.endsWith(XSD_SUFFIX))) {
				try {
					String resolvedSystemId = catalog.resolvePublic(publicId, systemId);
					if (resolvedSystemId == null) {
						resolvedSystemId = catalog.resolveURI(publicId);
					}
					if (resolvedSystemId != null) {
						return new InputSource(resolvedSystemId);
					}
				}
				catch (MalformedURLException me) {
					// ignore
				}
				catch (IOException ie) {
					// ignore
				}
			}
		}
		return null;
	}
}