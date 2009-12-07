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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.wst.xml.core.internal.XMLCorePlugin;
import org.eclipse.wst.xml.core.internal.catalog.CatalogContributorRegistryReader;
import org.eclipse.wst.xml.core.internal.catalog.provisional.ICatalog;
import org.eclipse.wst.xml.core.internal.catalog.provisional.ICatalogElement;
import org.eclipse.wst.xml.core.internal.catalog.provisional.ICatalogEntry;
import org.eclipse.wst.xml.core.internal.catalog.provisional.INextCatalog;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.INamespaceDefinition;
import org.springframework.ide.eclipse.beans.core.model.INamespaceDefinitionListener;
import org.springframework.ide.eclipse.beans.core.model.INamespaceDefinitionResolver;
import org.springframework.osgi.util.OsgiBundleUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * Extension to Spring DM's {@link NamespacePlugins} class that handles registering of XSDs in Eclipse' XML Catalog and
 * builds an internal representation of {@link INamespaceDefinition}s.
 * @author Christian Dupuis
 * @since 2.2.5
 */
@SuppressWarnings("restriction")
public class ToolingAwareNamespacePlugins extends NamespacePlugins implements INamespaceDefinitionResolver {

	private static final String XML_CORE_BUNDLE_SYMBOLICNAME = "org.eclipse.wst.xml.core";

	private static final String META_INF = "META-INF/";

	private static final String SPRING_SCHEMAS = "spring.schemas";

	private static final String SPRING_TOOLING = "spring.tooling";

	/** XML catalog entries keyed by the registering bundle */
	private volatile Map<Bundle, Set<ICatalogElement>> catalogEntriesByBundle = new HashMap<Bundle, Set<ICatalogElement>>();

	/** Namespace definitions keyed by the namespace uri */
	private volatile Map<String, NamespaceDefinition> namespaceDefinitionRegistry = new HashMap<String, NamespaceDefinition>();

	/** Namespace definition set keyed by the registering bundle */
	private volatile Map<Bundle, Set<NamespaceDefinition>> namespaceDefinitionsByBundle = new HashMap<Bundle, Set<NamespaceDefinition>>();

	/** Listeners to inform about namespace changes */
	private volatile Set<INamespaceDefinitionListener> namespaceDefinitionListeners = Collections
			.synchronizedSet(new HashSet<INamespaceDefinitionListener>());

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

		Set<ICatalogElement> catalogEntries = new HashSet<ICatalogElement>();
		this.catalogEntriesByBundle.put(bundle, catalogEntries);
		Set<NamespaceDefinition> namespaceDefinitions = new HashSet<NamespaceDefinition>();
		this.namespaceDefinitionsByBundle.put(bundle, namespaceDefinitions);

		Enumeration<URL> schemas = bundle.findEntries(META_INF, SPRING_SCHEMAS, false);
		if (schemas != null) {
			while (schemas.hasMoreElements()) {
				Properties props = loadProperties(schemas.nextElement());

				for (Object xsd : props.keySet()) {

					String key = xsd.toString();
					String uri = "platform:/plugin/" + bundle.getSymbolicName() + "/" + props.getProperty(key);
					String namespaceUri = getTargetNamespace(bundle.getEntry(props.getProperty(key)));
					String icon = null;
					String prefix = null;
					String name = null;

					// Add catalog entry to XML catalog
					addCatalogEntry(bundle, key, uri, catalogEntries, ICatalogEntry.ENTRY_TYPE_URI);

					Enumeration<URL> tooling = bundle.findEntries(META_INF, SPRING_TOOLING, false);
					if (tooling != null) {
						while (tooling.hasMoreElements()) {
							Properties toolingProps = loadProperties(tooling.nextElement());

							icon = toolingProps.getProperty(namespaceUri + "@icon");
							prefix = toolingProps.getProperty(namespaceUri + "@prefix");
							name = toolingProps.getProperty(namespaceUri + "@name");
						}
					}

					if (namespaceDefinitionRegistry.containsKey(namespaceUri)) {
						namespaceDefinitionRegistry.get(namespaceUri).addSchemaLocation(key);
						namespaceDefinitionRegistry.get(namespaceUri).addUri(props.getProperty(key));
					}
					else {
						NamespaceDefinition namespaceDefinition = new NamespaceDefinition(props);
						namespaceDefinition.setName(name);
						namespaceDefinition.setPrefix(prefix);
						namespaceDefinition.setIconPath(icon);
						namespaceDefinition.addSchemaLocation(key);
						namespaceDefinition.setBundle(bundle);
						namespaceDefinition.setNamespaceUri(namespaceUri);
						namespaceDefinition.addUri(props.getProperty(key));
						registerNamespaceDefinition(namespaceUri, namespaceDefinition);
						namespaceDefinitions.add(namespaceDefinition);
					}
				}
			}

			// Add catalog entry to namespace uri
			for (NamespaceDefinition definition : namespaceDefinitions) {
				addCatalogEntry(definition.getBundle(), definition.getNamespaceUri(), "platform:/plugin/"
						+ bundle.getSymbolicName() + "/" + definition.getDefaultUri(), catalogEntries,
						ICatalogEntry.ENTRY_TYPE_PUBLIC);
			}
		}
	}

	private void registerNamespaceDefinition(String uri, final NamespaceDefinition definition) {
		namespaceDefinitionRegistry.put(uri, definition);

		for (final INamespaceDefinitionListener listener : namespaceDefinitionListeners) {
			SafeRunner.run(new ISafeRunnable() {

				public void run() throws Exception {
					listener.onNamespaceDefinitionRegistered(definition);
				}

				public void handleException(Throwable exception) {
					BeansCorePlugin.log(exception);
				}
			});
		}
	}

	/**
	 * Create and add a XML catalog entry.
	 */
	private void addCatalogEntry(Bundle bundle, String key, String uri, Set<ICatalogElement> catalogEntries, int type) {
		Bundle xmlBundle = Platform.getBundle(XML_CORE_BUNDLE_SYMBOLICNAME);
		if (xmlBundle == null) {
			// The xml bundle is not even installed
			return;
		}
		if (!OsgiBundleUtils.isBundleActive(xmlBundle)) {
			try {
				xmlBundle.start();
			}
			catch (BundleException e) {
				// we can't start the xml core bundle and therefore we can't register the catalog element
				return;
			}
		}

		ICatalog systemCatalog = getSystemCatalog();
		if (systemCatalog == null) {
			return;
		}

		ICatalogElement catalogElement = systemCatalog.createCatalogElement(type);
		if (catalogElement instanceof ICatalogEntry) {
			ICatalogEntry entry = (ICatalogEntry) catalogElement;
			String resolvePath = CatalogContributorRegistryReader.resolvePath(CatalogContributorRegistryReader
					.getPlatformURL(bundle.getSymbolicName()), uri);
			entry.setKey(key);
			entry.setURI(resolvePath);
			systemCatalog.addCatalogElement(catalogElement);
			catalogEntries.add(catalogElement);
		}
	}

	/**
	 * Returns the target namespace URI of the XSD identified by the given <code>url</code>.
	 */
	private String getTargetNamespace(URL url) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(false);
		DocumentBuilder docBuilder;
		try {
			docBuilder = factory.newDocumentBuilder();
			Document doc = docBuilder.parse(url.openStream());
			return doc.getDocumentElement().getAttribute("targetNamespace");
		}
		catch (ParserConfigurationException e) {
			BeansCorePlugin.log(e);
		}
		catch (SAXException e) {
			BeansCorePlugin.log(e);
		}
		catch (IOException e) {
			BeansCorePlugin.log(e);
		}
		return null;
	}

	/**
	 * Returns the Eclipse XML catalog.
	 */
	private ICatalog getSystemCatalog() {
		XMLCorePlugin xmlCore = XMLCorePlugin.getDefault();
		if (xmlCore == null) {
			return null;
		}

		ICatalog systemCatalog = null;
		ICatalog defaultCatalog = xmlCore.getDefaultXMLCatalog();

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
	 * Fail safe loading of a {@link Properties} from an {@link URL}. Returns an empty {@link Properties} if the an
	 * {@link IOException} occurs.
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
		Bundle xmlBundle = Platform.getBundle(XML_CORE_BUNDLE_SYMBOLICNAME);

		if (catalogEntriesByBundle.containsKey(bundle) && OsgiBundleUtils.isBundleActive(xmlBundle)) {
			ICatalog systemCatalog = getSystemCatalog();
			for (ICatalogElement element : catalogEntriesByBundle.get(bundle)) {
				systemCatalog.removeCatalogElement(element);
			}
			catalogEntriesByBundle.remove(bundle);
		}

		if (namespaceDefinitionsByBundle.containsKey(bundle)) {
			for (NamespaceDefinition definition : namespaceDefinitionsByBundle.get(bundle)) {
				unregisterNamespaceDefinition(definition);
			}
			namespaceDefinitionsByBundle.remove(bundle);
		}
	}

	private void unregisterNamespaceDefinition(final NamespaceDefinition definition) {
		namespaceDefinitionRegistry.remove(definition.getNamespaceUri());

		for (final INamespaceDefinitionListener listener : namespaceDefinitionListeners) {
			SafeRunner.run(new ISafeRunnable() {

				public void run() throws Exception {
					listener.onNamespaceDefinitionUnregistered(definition);
				}

				public void handleException(Throwable exception) {
					BeansCorePlugin.log(exception);
				}
			});
		}
	}

	public void unregisterNamespaceDefinitionListener(INamespaceDefinitionListener listener) {
		namespaceDefinitionListeners.remove(listener);
	}

	public void registerNamespaceDefinitionListener(INamespaceDefinitionListener listener) {
		if (!namespaceDefinitionListeners.contains(listener)) {
			namespaceDefinitionListeners.add(listener);
		}
	}

	/**
	 * Default implementation of {@link INamespaceDefinition}.
	 */
	static class NamespaceDefinition implements INamespaceDefinition {

		private Pattern versionPattern = Pattern.compile(".*-([0-9,.]*)\\.xsd");

		private Bundle bundle;

		private String iconPath;

		private String name;

		private String namespaceUri;

		private String prefix;

		private Set<String> schemaLocations = new HashSet<String>();

		private Set<String> uris = new HashSet<String>();

		private Properties uriMapping = new Properties();

		public NamespaceDefinition(Properties uriMapping) {
			this.uriMapping = uriMapping;
		}

		public void addSchemaLocation(String schemaLocation) {
			schemaLocations.add(schemaLocation);
		}

		public void addUri(String uri) {
			uris.add(uri);
		}

		/**
		 * {@inheritDoc}
		 */
		public Bundle getBundle() {
			return bundle;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getDefaultSchemaLocation() {
			// Per convention the version-less XSD is the default
			String defaultSchemaLocation = null;
			for (String schemaLocation : schemaLocations) {
				if (!versionPattern.matcher(schemaLocation).matches()) {
					defaultSchemaLocation = schemaLocation;
				}
			}
			if (defaultSchemaLocation == null && schemaLocations.size() > 0) {
				List<String> locations = new ArrayList<String>(schemaLocations);
				Collections.sort(locations);
				defaultSchemaLocation = locations.get(0);
			}
			return defaultSchemaLocation;
		}

		/**
		 * {@inheritDoc}
		 */
		protected String getDefaultUri() {
			String defaultUri = null;
			Version version = Version.MINIMUM_VERSION;
			for (String uri : uris) {
				Version tempVersion = Version.MINIMUM_VERSION;
				Matcher matcher = versionPattern.matcher(uri);
				if (matcher.matches()) {
					tempVersion = new Version(matcher.group(1));
				}
				if (tempVersion.compareTo(version) >= 0) {
					version = tempVersion;
					defaultUri = uri;
				}
			}
			return defaultUri;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getIconPath() {
			return iconPath;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getName() {
			return name;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getNamespaceUri() {
			return namespaceUri;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getPrefix() {
			if (prefix != null) {
				return prefix;
			}
			int ix = namespaceUri.lastIndexOf('/');
			if (ix > 0) {
				return namespaceUri.substring(ix + 1);
			}
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public Set<String> getSchemaLocations() {
			return schemaLocations;
		}

		/**
		 * {@inheritDoc}
		 */
		public void setBundle(Bundle bundle) {
			this.bundle = bundle;
		}

		public void setIconPath(String iconPath) {
			this.iconPath = iconPath;
		}

		/**
		 * {@inheritDoc}
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * {@inheritDoc}
		 */
		public void setNamespaceUri(String namespaceUri) {
			this.namespaceUri = namespaceUri;
		}

		/**
		 * {@inheritDoc}
		 */
		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		/**
		 * {@inheritDoc}
		 */
		public Properties getUriMapping() {
			return this.uriMapping;
		}
	}

	static class Version implements Comparable<Version> {

		private static final String MINIMUM_VERSION_STRING = "0";

		public static final Version MINIMUM_VERSION = new Version(MINIMUM_VERSION_STRING);

		private final org.osgi.framework.Version version;

		public Version(String v) {
			org.osgi.framework.Version tempVersion = null;
			try {
				tempVersion = org.osgi.framework.Version.parseVersion(v);
			}
			catch (Exception e) {
				// make sure that we don't crash on any new version numbers format that we don't support
				BeansCorePlugin.log("Cannot convert schema vesion", e);
				tempVersion = org.osgi.framework.Version.parseVersion(MINIMUM_VERSION_STRING);
			}
			this.version = tempVersion;
		}

		public int compareTo(Version v2) {
			return this.version.compareTo(v2.version);
		}
	}
}
