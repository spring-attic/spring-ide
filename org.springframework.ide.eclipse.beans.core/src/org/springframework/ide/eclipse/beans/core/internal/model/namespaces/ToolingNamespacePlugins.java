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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.wst.xml.core.internal.XMLCorePlugin;
import org.eclipse.wst.xml.core.internal.catalog.CatalogContributorRegistryReader;
import org.eclipse.wst.xml.core.internal.catalog.provisional.ICatalog;
import org.eclipse.wst.xml.core.internal.catalog.provisional.ICatalogElement;
import org.eclipse.wst.xml.core.internal.catalog.provisional.ICatalogEntry;
import org.eclipse.wst.xml.core.internal.catalog.provisional.INextCatalog;
import org.osgi.framework.Bundle;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.INamespaceDefinition;
import org.springframework.ide.eclipse.beans.core.model.INamespaceDefinitionResolver;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Extension to Spring DM's {@link NamespacePlugins} class that handles registering of XSDs in
 * Eclipse' XML Catalog and builds an internal representation of {@link INamespaceDefinition}s.
 * @author Christian Dupuis
 * @since 2.2.5
 */
@SuppressWarnings("restriction")
public class ToolingNamespacePlugins extends NamespacePlugins implements
		INamespaceDefinitionResolver {

	private static final String META_INF = "META-INF/";

	private static final String SPRING_SCHEMAS = "spring.schemas";

	private static final String SPRING_TOOLING = "spring.tooling";

	/** XML catalog entries keyed by the registering bundle */
	private final Map<Bundle, Set<ICatalogElement>> catalogEntriesByBundle = new HashMap<Bundle, Set<ICatalogElement>>();

	/** Namespace definitions keyed by the namespace uri */
	private final Map<String, NamespaceDefinition> namespaceDefinitionRegistry = new HashMap<String, NamespaceDefinition>();

	/** Namespace definition set keyed by the registering bundle */
	private final Map<Bundle, Set<NamespaceDefinition>> namespaceDefinitionsByBundle = new HashMap<Bundle, Set<NamespaceDefinition>>();

	/**
	 * {@inheritDoc}
	 */
	@Override
	void addPlugin(Bundle bundle, boolean lazyBundle, boolean applyCondition) {
		super.addPlugin(bundle, lazyBundle, applyCondition);
		addNamespaceDefinition(bundle);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	boolean removePlugin(Bundle bundle) {
		removeNamespaceDefinition(bundle);
		return super.removePlugin(bundle);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<INamespaceDefinition> getNamespaceDefinitions() {
		return new HashSet<INamespaceDefinition>(namespaceDefinitionRegistry.values());
	}

	/**
	 * {@inheritDoc}
	 */
	public INamespaceDefinition resolveNamespaceDefinition(String namespaceUri) {
		return namespaceDefinitionRegistry.get(namespaceUri);
	}

	/**
	 * Register the XML namespace catalog and build the {@link INamespaceDefinition}s.
	 */
	@SuppressWarnings("unchecked")
	private void addNamespaceDefinition(Bundle bundle) {
		ICatalog systemCatalog = getSystemCatalog();

		Set<ICatalogElement> catalogEntries = new HashSet<ICatalogElement>();
		this.catalogEntriesByBundle.put(bundle, catalogEntries);
		Set<NamespaceDefinition> namespaceDefinitions = new HashSet<NamespaceDefinition>();
		this.namespaceDefinitionsByBundle.put(bundle, namespaceDefinitions);

		Enumeration<URL> schemas = bundle.findEntries(META_INF, SPRING_SCHEMAS, false);
		while (schemas.hasMoreElements()) {
			URL schemaFile = schemas.nextElement();
			Properties props = loadProperties(schemaFile);

			for (Object xsd : props.keySet()) {

				String uri = "platform:/plugin/" + bundle.getSymbolicName() + "/"
						+ props.getProperty(xsd.toString());
				String key = xsd.toString();
				int type = ICatalogEntry.ENTRY_TYPE_SYSTEM;

				ICatalogElement catalogElement = systemCatalog.createCatalogElement(type);
				if (catalogElement instanceof ICatalogEntry) {
					ICatalogEntry entry = (ICatalogEntry) catalogElement;
					String resolvePath = CatalogContributorRegistryReader.resolvePath(
							CatalogContributorRegistryReader.getPlatformURL(bundle
									.getSymbolicName()), uri);
					entry.setKey(key);
					entry.setURI(resolvePath);
					systemCatalog.addCatalogElement(catalogElement);
					catalogEntries.add(catalogElement);
				}

				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setValidating(false);
				factory.setNamespaceAware(false);
				try {
					DocumentBuilder docBuilder = factory.newDocumentBuilder();
					Document doc = docBuilder.parse(bundle.getEntry(
							props.getProperty(xsd.toString())).openStream());
					String namespaceUri = doc.getDocumentElement().getAttribute("targetNamespace");

					Enumeration<URL> tooling = bundle.findEntries(META_INF, SPRING_TOOLING, false);
					if (tooling != null) {
						while (tooling.hasMoreElements()) {
							URL tool = tooling.nextElement();
							Properties toolingProps = loadProperties(tool);

							String icon = toolingProps.getProperty(namespaceUri + "@icon");
							String prefix = toolingProps.getProperty(namespaceUri + "@prefix");
							String name = toolingProps.getProperty(namespaceUri + "@name");

							if (name != null && prefix != null) {
								if (namespaceDefinitionRegistry.containsKey(namespaceUri)) {
									namespaceDefinitionRegistry.get(namespaceUri)
											.addSchemaLocation(key);
								}
								else {
									NamespaceDefinition namespaceDefinition = new NamespaceDefinition();
									namespaceDefinition.setName(name);
									namespaceDefinition.setPrefix(prefix);
									namespaceDefinition.setIconPath(icon);
									namespaceDefinition.addSchemaLocation(key);
									namespaceDefinition.setBundle(bundle);
									namespaceDefinition.setNamespaceUri(namespaceUri);
									namespaceDefinitionRegistry.put(namespaceUri,
											namespaceDefinition);
									namespaceDefinitions.add(namespaceDefinition);
								}
							}
						}
					}
				}
				catch (SAXException e) {
					BeansCorePlugin.log(e);
				}
				catch (IOException e) {
					BeansCorePlugin.log(e);
				}
				catch (ParserConfigurationException e) {
					BeansCorePlugin.log(e);
				}
			}
		}
	}

	/**
	 * Returns the Eclipse XML catalog.
	 */
	private ICatalog getSystemCatalog() {
		ICatalog systemCatalog = null;
		ICatalog defaultCatalog = XMLCorePlugin.getDefault().getDefaultXMLCatalog();

		INextCatalog[] nextCatalogs = defaultCatalog.getNextCatalogs();
		for (int i = 0; i < nextCatalogs.length; i++) {
			INextCatalog catalog = nextCatalogs[i];
			ICatalog referencedCatalog = catalog.getReferencedCatalog();
			if (referencedCatalog != null) {
				if (XMLCorePlugin.SYSTEM_CATALOG_ID.equals(referencedCatalog.getId())) {
					systemCatalog = referencedCatalog;
				}
			}
		}
		return systemCatalog;
	}

	/**
	 * Fail safe loading of a {@link Properties} from an {@link URL}. Returns an empty
	 * {@link Properties} if the an {@link IOException} occurs.
	 */
	private Properties loadProperties(URL url) {
		Properties toolingProps = new Properties();
		try {
			toolingProps.load(url.openStream());
		}
		catch (IOException e) {
			BeansCorePlugin.log(e);
		}
		return toolingProps;
	}
	
	/**
	 * Removes any registered {@link INamespaceDefinition}s and XML catalog entries. 
	 */
	private void removeNamespaceDefinition(Bundle bundle) {
		if (catalogEntriesByBundle.containsKey(bundle)) {
			ICatalog systemCatalog = getSystemCatalog();
			for (ICatalogElement element : catalogEntriesByBundle.get(bundle)) {
				systemCatalog.removeCatalogElement(element);
			}
			catalogEntriesByBundle.remove(bundle);
		}

		if (namespaceDefinitionsByBundle.containsKey(bundle)) {
			for (NamespaceDefinition definition : namespaceDefinitionsByBundle.get(bundle)) {
				namespaceDefinitionRegistry.remove(definition.getNamespaceUri());
			}
			namespaceDefinitionsByBundle.remove(bundle);
		}
	}

	/**
	 * Default implementation of {@link INamespaceDefinition}.
	 * @author Christian Dupuis
	 * @since 2.2.5
	 */
	class NamespaceDefinition implements INamespaceDefinition {

		private Bundle bundle;

		private String iconPath;

		private String name;

		private String namespaceUri;

		private String prefix;

		private Set<String> schemaLocations = new HashSet<String>();

		public void addSchemaLocation(String schemaLocation) {
			schemaLocations.add(schemaLocation);
		}

		public Bundle getBundle() {
			return bundle;
		}

		public String getDefaultSchemaLocation() {
			String defaultSchemaLocation = null;
			float version = 0F;
			for (String schemaLocation : schemaLocations) {
				float tempVersion = 0F;
				try {
					int ix = schemaLocation.lastIndexOf('-');
					if (ix > 0) {
						tempVersion = Float.parseFloat(schemaLocation.substring(ix + 1,
								schemaLocation.length() - 4));
					}
				}
				catch (Exception e) {
					// make sure it can't fail on us
				}
				if (tempVersion >= version) {
					version = tempVersion;
					defaultSchemaLocation = schemaLocation;
				}
			}
			return defaultSchemaLocation;
		}

		public String getIconPath() {
			return iconPath;
		}

		public String getName() {
			return name;
		}

		public String getNamespaceUri() {
			return namespaceUri;
		}

		public String getPrefix() {
			return prefix;
		}

		public Set<String> getSchemaLocations() {
			return schemaLocations;
		}

		public void setBundle(Bundle bundle) {
			this.bundle = bundle;
		}

		public void setIconPath(String iconPath) {
			this.iconPath = iconPath;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setNamespaceUri(String namespaceUri) {
			this.namespaceUri = namespaceUri;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}
	}

}
