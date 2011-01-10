/*******************************************************************************
 * Copyright (c) 2008, 2010 Spring IDE Developers
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

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.xml.DefaultNamespaceHandlerResolver;
import org.springframework.beans.factory.xml.NamespaceHandler;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.ToolAnnotationBasedNamespaceHandler;
import org.springframework.ide.eclipse.beans.core.internal.model.namespaces.DocumentAccessor.SchemaLocations;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.beans.core.namespaces.NamespaceUtils.NamespaceHandlerDescriptor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This {@link NamespaceHandlerResolver} provides a {@link NamespaceHandler} for a given namespace URI. Depending on
 * this namespace URI the returned namespace handler is one of the following (in the provided order):
 * <ol>
 * <li>a namespace handler provided by the Spring framework</li>
 * <li>a namespace handler contributed via the extension point
 * <code>org.springframework.ide.eclipse.beans.core.namespaces</code></li>
 * <li>a namespace handler resolved by a NemespaceHandlerResolver published as OSGi service</li> 
 * <li>a {@link ToolAnnotationBasedNamespaceHandler}</li>
 * </ol>
 * *
 * @author Christian Dupuis
 * @author Torsten Juergeleit
 * @since 2.0.3
 */
public class DelegatingNamespaceHandlerResolver extends DefaultNamespaceHandlerResolver {

	private static final SchemaLocations EMPTY_SCHEMA_LOCATIONS = new SchemaLocations();

	private NamespaceHandler toolAnnotationNamespaceHandler;

	private final Map<NamespaceHandlerDescriptor, NamespaceHandler> namespaceHandlers;

	private final Set<NamespaceHandlerResolver> namespaceHandlerResolvers;

	private final DocumentAccessor documentAccessor;

	private final Map<String, NamespaceHandler> resolvedNamespaceHandlers = new HashMap<String, NamespaceHandler>();

	public DelegatingNamespaceHandlerResolver(ClassLoader classLoader, IBeansConfig beansConfig) {
		this(classLoader, beansConfig, null);
	}

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
		// Check cache first
		if (resolvedNamespaceHandlers.containsKey(namespaceUri)) {
			return resolvedNamespaceHandlers.get(namespaceUri);
		}

		NamespaceHandler namespaceHandler = null;

		try {
			// First check for a namespace handler provided by Spring.
			namespaceHandler = super.resolve(namespaceUri);

			if (namespaceHandler != null) {
				return decorateNamespaceHandler(namespaceHandler);
			}

			SchemaLocations schemaLocations = EMPTY_SCHEMA_LOCATIONS;
			if (documentAccessor != null) {
				schemaLocations = documentAccessor.getCurrentSchemaLocations();
			}

			// Then check for a namespace handler contributed for the specific schemalocation
			String schemaLocation = schemaLocations.getSchemaLocation(namespaceUri);
			if (schemaLocation != null) {
				namespaceHandler = namespaceHandlers.get(NamespaceHandlerDescriptor.createNamespaceHandlerDescriptor(
						namespaceUri, schemaLocation));
				if (namespaceHandler != null) {
					return decorateNamespaceHandler(namespaceHandler);
				}
			}

			// Then check for a namespace handler provided by an extension.
			namespaceHandler = namespaceHandlers.get(NamespaceHandlerDescriptor.createNamespaceHandlerDescriptor(
					namespaceUri, null));
			if (namespaceHandler != null) {
				return decorateNamespaceHandler(namespaceHandler);
			}

			// Then check the contributed NamespaceHandlerResolver.
			for (NamespaceHandlerResolver resolver : namespaceHandlerResolvers) {
				try {
					namespaceHandler = resolver.resolve(namespaceUri);
					if (namespaceHandler != null) {
						return decorateNamespaceHandler(namespaceHandler);
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
		finally {

			// Add to cache for subsequent faster access
			if (namespaceHandler != null) {
				if (!(namespaceHandler instanceof ElementTrackingNamespaceHandler)) {
					namespaceHandler = decorateNamespaceHandler(namespaceHandler);
				}
				resolvedNamespaceHandlers.put(namespaceUri, namespaceHandler);
			}
		}
	}

	/**
	 * Decorate the given {@link NamespaceHandler} with an {@link ElementTrackingNamespaceHandler}.
	 */
	private NamespaceHandler decorateNamespaceHandler(NamespaceHandler namespaceHandler) {
		return new ElementTrackingNamespaceHandler(namespaceHandler);
	}

	/**
	 * {@link NamespaceHandler} that wraps another instance and keeps track of the current {@link Element} being parsed
	 * or decorated.
	 * @since 2.5.2
	 */
	class ElementTrackingNamespaceHandler implements NamespaceHandler {

		private final NamespaceHandler namespaceHandler;

		public ElementTrackingNamespaceHandler(NamespaceHandler namespaceHandler) {
			this.namespaceHandler = namespaceHandler;
		}

		/**
		 * {@inheritDoc}
		 */
		public void init() {
			namespaceHandler.init();
		}

		/**
		 * {@inheritDoc}
		 */
		public BeanDefinition parse(Element element, ParserContext parserContext) {
			try {
				documentAccessor.pushElement(element);
				return namespaceHandler.parse(element, parserContext);
			}
			finally {
				documentAccessor.popElement();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public BeanDefinitionHolder decorate(Node source, BeanDefinitionHolder definition, ParserContext parserContext) {
			try {
				documentAccessor.pushElement(source);
				return namespaceHandler.decorate(source, definition, parserContext);
			}
			finally {
				documentAccessor.popElement();
			}
		}
	}
	
}