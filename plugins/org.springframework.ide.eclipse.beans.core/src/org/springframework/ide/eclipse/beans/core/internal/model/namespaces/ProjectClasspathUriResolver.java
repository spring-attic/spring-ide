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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
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

		schemaMappings = getSchemaMappings();

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
		if (schemaMappings.containsKey(systemId)) {
			String xsdPath = schemaMappings.get(systemId);
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

	/**
	 * Verify if the <code>resources</code> array contains a file matching
	 * <code>fileName</code>.
	 */
	private boolean containsSchema(Object[] resources, String fileName) {
		for (Object resource : resources) {
			if (resource instanceof IResource) {
				if (((IResource) resource).getName().equals(fileName)) {
					return true;
				}
			} else if (resource instanceof IJarEntryResource) {
				if (((IJarEntryResource) resource).getName().equals(fileName)) {
					return true;
				}
			}
		}
		return false;
	}

	private String resolveXsdPathOnClasspath(String xsdPath) {
		// fallback, if schema location starts with / and therefore fails to be
		// found by classloader
		if (xsdPath.startsWith("/")) {
			xsdPath = xsdPath.substring(1);
		}

		String packageName = "";
		String fileName = xsdPath;

		int ix = xsdPath.lastIndexOf('/');
		if (ix > 0) {
			packageName = xsdPath.substring(0, ix).replace('/', '.');
			fileName = xsdPath.substring(ix + 1);
		}

		IJavaProject javaProject = JdtUtils.getJavaProject(project);
		if (javaProject != null) {
			try {
				for (IPackageFragmentRoot root : javaProject
						.getAllPackageFragmentRoots()) {
					boolean found = false;

					// Look in the root of the package fragment root
					if ("".equals(packageName) && root.exists()) {
						found = containsSchema(root.getNonJavaResources(),
								fileName);
					}

					// Check the package
					IPackageFragment packageFragment = root
							.getPackageFragment(packageName);
					if (!found && packageFragment != null
							&& packageFragment.exists()) {
						found = containsSchema(
								packageFragment.getNonJavaResources(), fileName);
					}

					// Found the XSD in the package fragment root? -> construct
					// usable URI
					if (found) {
						String path = "";

						// Workspace jar or resource
						if (root.getResource() != null) {
							URI jarUri = SpringCoreUtils.getResourceURI(root
									.getResource());
							path = jarUri.toString();
						}
						// Workspace external jar
						else {
							File jarFile = root.getPath().toFile();
							path = jarFile.toURI().toString();
						}

						// If the path points to a jar -> add jar: URI prefix
						// and append ! as separator
						if (path.endsWith(".jar")) {
							path = "jar:" + path + "!";
						}

						// Make sure that all paths start with '/'
						if (xsdPath.startsWith("/")) {
							return path + xsdPath;
						}
						return path + "/" + xsdPath;
					}
				}
			} catch (JavaModelException e) {
				// The implementation is called too often to log
			}
		}
		return null;
	}

}
