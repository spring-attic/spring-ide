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
package org.springframework.ide.eclipse.beans.core.internal.model.namespaces;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.ToolAnnotationBasedNamespaceHandler;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.beans.core.namespaces.NamespaceUtils.NamespaceHandlerDescriptor;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;

/**
 * This {@link NamespaceHandlerResolver} provides a {@link NamespaceHandler} for a given namespace URI. Depending on
 * this namespace URI the returned namespace handler is one of the following (in the provided order):
 * <ol>
 * <li>a namespace handler provided by the Spring framework</li>
 * <li>a namespace handler contributed via the extension point
 * <code>org.springframework.ide.eclipse.beans.core.namespaces</code></li>
 * <li>a namespace handler resolved by a NemespaceHandlerResolver published as OSGi service</li> *
 * <li>a {@link ToolAnnotationBasedNamespaceHandler}</li>
 * </ol>
 * *
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 * @since 2.0.3
 */
public class DelegatingNamespaceHandlerResolver extends DefaultNamespaceHandlerResolver {

	private NamespaceHandler toolAnnotationNamespaceHandler;

	private final Map<NamespaceHandlerDescriptor, NamespaceHandler> namespaceHandlers;

	private final Set<NamespaceHandlerResolver> namespaceHandlerResolvers;

	private final DocumentAccessor documentAccessor;

	public DelegatingNamespaceHandlerResolver(ClassLoader classLoader, IBeansConfig beansConfig,
			DocumentAccessor documentHolder) {
		super(classLoader);
		this.documentAccessor = documentHolder;
		this.namespaceHandlers = NamespaceUtils.getNamespaceHandlers();
		this.namespaceHandlerResolvers = NamespaceUtils.getNamespaceHandlerResolvers();
		if (beansConfig != null) {
			this.toolAnnotationNamespaceHandler = new ToolAnnotationBasedNamespaceHandler(beansConfig);
		}
	}

	@Override
	public NamespaceHandler resolve(String namespaceUri) {

		SchemaLocations schemaLocations = new SchemaLocations();
		if (documentAccessor != null) {
			Document doc = documentAccessor.getCurrentDocument();
			if (doc != null && doc.getDocumentElement() != null) {
				schemaLocations.initSchemaLocations(doc.getDocumentElement().getAttributeNS(
						"http://www.w3.org/2001/XMLSchema-instance", "schemaLocation"));
			}
		}

		NamespaceHandler namespaceHandler = null;

		// First check for a namespace handler provided by Spring.
		namespaceHandler = super.resolve(namespaceUri);

		if (namespaceHandler != null) {
			return namespaceHandler;
		}

		// Then check for a namespace handler contributed for the specific schemalocation
		String schemaLocation = schemaLocations.getSchemaLocation(namespaceUri);
		if (schemaLocation != null) {
			namespaceHandler = namespaceHandlers.get(NamespaceHandlerDescriptor.createNamespaceHandlerDescriptor(
					namespaceUri, schemaLocation));
			if (namespaceHandler != null) {
				return namespaceHandler;
			}
		}

		// Then check for a namespace handler provided by an extension.
		namespaceHandler = namespaceHandlers.get(NamespaceHandlerDescriptor.createNamespaceHandlerDescriptor(
				namespaceUri, null));
		if (namespaceHandler != null) {
			return namespaceHandler;
		}

		// Then check the contributed NamespaceHandlerResolver.
		for (NamespaceHandlerResolver resolver : namespaceHandlerResolvers) {
			try {
				namespaceHandler = resolver.resolve(namespaceUri);
				if (namespaceHandler != null) {
					return namespaceHandler;
				}
			}
			catch (Exception e) {
				// Make sure a contributed NamespaceHandlerResolver can't prevent parsing.
				BeansCorePlugin.log(e);
			}
		}

		// Finally fall back to the tool annotation based namespace handler.
		return toolAnnotationNamespaceHandler;
	}

	/**
	 * Internal class that parses the value of the <code>schemaLocation</code> attribute and offers accessors to the
	 * mapping.
	 */
	private class SchemaLocations {

		private Map<String, String> mapping = new HashMap<String, String>();

		public void initSchemaLocations(String schemaLocations) {
			if (StringUtils.hasLength(schemaLocations)) {
				String[] tokens = StringUtils.tokenizeToStringArray(schemaLocations, " \r\n");
				if (tokens.length % 2 == 0) {
					for (int i = 0; i < tokens.length; i = i + 2) {
						mapping.put(tokens[i], tokens[i + 1]);
					}
				}
			}
		}

		public String getSchemaLocation(String namespaceUri) {
			return mapping.get(namespaceUri);
		}
	}

}