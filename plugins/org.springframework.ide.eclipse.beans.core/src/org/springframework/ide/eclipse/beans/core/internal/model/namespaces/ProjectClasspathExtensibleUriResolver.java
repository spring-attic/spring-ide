/*******************************************************************************
 * Copyright (c) 2010 Spring IDE Developers
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
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolverExtension;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.util.CollectionUtils;

/**
 * {@link URIResolverExtension} resolves URIs on the project classpath using the protocol established by
 * <code>spring.schema</code> files.
 * @author Christian Dupuis
 * @since 2.3.1
 */
public class ProjectClasspathExtensibleUriResolver implements URIResolverExtension {

	private static final String DEFAULT_SCHEMA_MAPPINGS_LOCATION = "META-INF/spring.schemas";

	/**
	 * {@inheritDoc}
	 */
	public String resolve(IFile file, String baseLocation, String publicId, String systemId) {
		// systemId is already resolved; so don't touch
		if (systemId != null && systemId.startsWith("jar:")) {
			return null;
		}

		// file and systemId can't be null
		if (file == null || systemId == null) {
			return null;
		}

		// resolve the project first
		IBeansProject project = BeansCorePlugin.getModel().getProject(file.getProject());
		if (project == null) {
			return null;
		}

		// Resolve using the classpath
		if (NamespaceUtils.useNamespacesFromClasspath(file.getProject()) && checkFileExtension(file, project)) {
			return resolveOnClasspath(file, systemId);
		}
		return null;
	}

	/**
	 * Check that the file has a valid file extension.
	 */
	private boolean checkFileExtension(IFile file, IBeansProject project) {
		if (project.getConfigSuffixes() != null) {
			for (String extension : project.getConfigSuffixes()) {
				if (file.getName().endsWith(extension)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Verify if the <code>resources</code> array contains a file matching <code>fileName</code>.
	 */
	private boolean containsSchema(Object[] resources, String fileName) {
		for (Object resource : resources) {
			if (resource instanceof IResource) {
				if (((IResource) resource).getName().equals(fileName)) {
					return true;
				}
			}
			else if (resource instanceof IJarEntryResource) {
				if (((IJarEntryResource) resource).getName().equals(fileName)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Loads all schema mappings from all <code>spring.schemas</code> files on the project classpath.
	 */
	private Map<String, String> getSchemaMappings(IProject project) {
		Map<String, String> handlerMappings = new ConcurrentHashMap<String, String>();
		try {
			Properties mappings = PropertiesLoaderUtils.loadAllProperties(DEFAULT_SCHEMA_MAPPINGS_LOCATION,
					JdtUtils.getClassLoader(project, null));
			CollectionUtils.mergePropertiesIntoMap(mappings, handlerMappings);
		}
		catch (IOException ex) {
			// We can ignore this as we simply don't find the xsd file then.
		}
		return handlerMappings;
	}

	/**
	 * Resolves the given <code>systemId</code> on the classpath configured by the <code>file</code>'s project.
	 */
	private String resolveOnClasspath(IFile file, String systemId) {
		Map<String, String> schemaMappings = getSchemaMappings(file.getProject());

		// Only resolve if systemId is configured in any spring.schemas file
		if (schemaMappings.containsKey(systemId)) {

			String xsdPath = schemaMappings.get(systemId);
			String packageName = "";
			String fileName = xsdPath;

			int ix = xsdPath.lastIndexOf('/');
			if (ix > 0) {
				packageName = xsdPath.substring(0, ix).replace('/', '.');
				fileName = xsdPath.substring(ix + 1);
			}

			IJavaProject javaProject = JdtUtils.getJavaProject(file.getProject());
			if (javaProject != null) {

				try {
					for (IPackageFragmentRoot root : javaProject.getAllPackageFragmentRoots()) {
						boolean found = false;

						// Look in the root of the package fragment root
						if ("".equals(packageName) && root.exists()) {
							found = containsSchema(root.getNonJavaResources(), fileName);
						}

						// Check the package
						IPackageFragment packageFragment = root.getPackageFragment(packageName);
						if (!found && packageFragment != null && packageFragment.exists()) {
							found = containsSchema(packageFragment.getNonJavaResources(), fileName);
						}

						// Found the XSD in the package fragment root? -> construct usable URI
						if (found) {
							String path = "";

							// Workspace jar or resource
							if (root.getResource() != null) {
								URI jarUri = SpringCoreUtils.getResourceURI(root.getResource());
								path = jarUri.toString();
							}
							// Workspace external jar
							else {
								File jarFile = root.getPath().toFile();
								path = jarFile.toURI().toString();
							}

							// If the path points to a jar -> add jar: URI prefix and append ! as separator
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
				}
				catch (JavaModelException e) {
					// The implementation is called too often to log
				}
			}
		}

		return null;
	}

}
