/*******************************************************************************
 * Copyright (c) 2011 Spring IDE Developers
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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.core.resources.IProject;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.util.CollectionUtils;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * resolves URIs on the project classpath using the protocol established by
 * <code>spring.schemas</code> files.
 * 
 * @author Martin Lippert
 * @since 2.7.0
 */
public class ProjectClasspathUriResolver {

	private final IProject project;
	private boolean includingSourceFolders;

	private Map<String, String> typePublic;
	private Map<String, String> typeUri;
	private Map<String, String> schemaMappings;

	public ProjectClasspathUriResolver(IProject project) {
		this.project = project;
		this.includingSourceFolders = NamespaceUtils
				.useNamespacesAlsoFromSourceFolders(project);

		if (!includingSourceFolders) {
			init();
		}
	}

	/**
	 * Resolves the given <code>systemId</code> on the classpath configured by
	 * the <code>file</code>'s project.
	 */
	public String resolveOnClasspath(String publicId, String systemId) {
		if (includingSourceFolders) {
			return resolveOnClasspathAndSourceFolders(publicId, systemId);
		}

		return resolveOnClasspathOnly(publicId, systemId);
	}

	private String resolveOnClasspathAndSourceFolders(String publicId,
			String systemId) {
		Map<String, String> mappings = getSchemaMappings();
		if (mappings != null && systemId != null && mappings.containsKey(systemId)) {
			String xsdPath = mappings.get(systemId);
			return resolveXsdPathOnClasspath(xsdPath);
		}
		return null;
	}

	private String resolveOnClasspathOnly(String publicId, String systemId) {
		String resolved = null;
		if (systemId != null) {
			resolved = typeUri.get(systemId);
		}
		if (resolved == null && publicId != null) {
			if (!(systemId != null && systemId.endsWith(".xsd"))) {
				resolved = typePublic.get(publicId);
			}
		}
		return resolved;
	}

	private void init() {
		this.typePublic = new ConcurrentHashMap<String, String>();
		this.typeUri = new ConcurrentHashMap<String, String>();

		Map<String, NamespaceDefinition> namespaceDefinitionRegistry = new HashMap<String, NamespaceDefinition>();

		schemaMappings = getSchemaMappings();
		if (schemaMappings != null) {
			for (String key : schemaMappings.keySet()) {
				String path = schemaMappings.get(key);

				// add the resolved path to the list of uris
				String resolvedPath = resolveXsdPathOnClasspath(path);
				if (resolvedPath != null) {
					typeUri.put(key, resolvedPath);
				}

				// collect base information to later extract the default uri
				String namespaceUri = getTargetNamespace(resolvedPath);

				if (namespaceDefinitionRegistry.containsKey(namespaceUri)) {
					namespaceDefinitionRegistry.get(namespaceUri)
							.addSchemaLocation(key);
					namespaceDefinitionRegistry.get(namespaceUri).addUri(path);
				} else {
					NamespaceDefinition namespaceDefinition = new NamespaceDefinition(
							null);
					namespaceDefinition.addSchemaLocation(key);
					namespaceDefinition.setNamespaceUri(namespaceUri);
					namespaceDefinition.addUri(path);
					namespaceDefinitionRegistry.put(namespaceUri,
							namespaceDefinition);
				}
			}

			// Add catalog entry to namespace uri
			for (NamespaceDefinition definition : namespaceDefinitionRegistry
					.values()) {
				String namespaceKey = definition.getNamespaceUri();
				String defaultUri = definition.getDefaultUri();

				String resolvedPath = resolveXsdPathOnClasspath(defaultUri);
				if (resolvedPath != null) {
					typePublic.put(namespaceKey, resolvedPath);
				}
			}

		}
	}

	/**
	 * Returns the target namespace URI of the XSD identified by the given
	 * <code>resolvedPath</code>.
	 */
	private String getTargetNamespace(String resolvedPath) {
		if (resolvedPath == null) {
			return null;
		}

		try {
			URI uri = new URI(resolvedPath);

			DocumentBuilder docBuilder = SpringCoreUtils.getDocumentBuilder();
			Document doc = docBuilder.parse(uri.toURL().openStream());
			return doc.getDocumentElement().getAttribute("targetNamespace");
		} catch (SAXException e) {
			BeansCorePlugin.log(e);
		} catch (IOException e) {
			BeansCorePlugin.log(e);
		} catch (URISyntaxException e) {
			BeansCorePlugin.log(e);
		}
		return null;
	}

	/**
	 * Loads all schema mappings from all <code>spring.schemas</code> files on
	 * the project classpath.
	 */
	private Map<String, String> getSchemaMappings() {
		Map<String, String> handlerMappings = new ConcurrentHashMap<String, String>();
		try {
			Properties mappings = PropertiesLoaderUtils
					.loadAllProperties(
							ProjectClasspathNamespaceDefinitionResolver.DEFAULT_SCHEMA_MAPPINGS_LOCATION,
							JdtUtils.getClassLoader(project, null));
			CollectionUtils.mergePropertiesIntoMap(mappings, handlerMappings);
		} catch (IOException ex) {
			// We can ignore this as we simply don't find the xsd file then.
		}
		return handlerMappings;
	}

	private String resolveXsdPathOnClasspath(String xsdPath) {
		ClassLoader cls = JdtUtils.getClassLoader(project, null);
		
		URL url = cls.getResource(xsdPath);

		// fallback, if schema location starts with / and therefore fails to be found by classloader
		if (url == null && xsdPath.startsWith("/")) {
			xsdPath = xsdPath.substring(1);
			url = cls.getResource(xsdPath);
		}
		
		return url.toString();
	}
	
}
