/*******************************************************************************
 * Copyright (c) 2010, 2011 Spring IDE Developers
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
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.wst.common.uriresolver.internal.provisional.URIResolverExtension;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.namespaces.DocumentAccessor.SchemaLocations;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.w3c.dom.Document;

/**
 * {@link URIResolverExtension} resolves URIs on the project classpath using the protocol established by
 * <code>spring.schemas</code> files.
 * @author Christian Dupuis
 * @author Martin Lippert
 * @since 2.3.1
 */
@SuppressWarnings("restriction")
public class ProjectClasspathExtensibleUriResolver implements URIResolverExtension, IElementChangedListener {

	private static Map<IProject, ProjectClasspathUriResolver> projectResolvers = new ConcurrentHashMap<IProject, ProjectClasspathUriResolver>();
	private ThreadLocal<IPath> previousFile = new ThreadLocal<IPath>();
	
	public ProjectClasspathExtensibleUriResolver() {
		JavaCore.addElementChangedListener(this, ElementChangedEvent.POST_CHANGE);
	}

	/**
	 * {@inheritDoc}
	 */
	public String resolve(IFile file, String baseLocation, String publicId, String systemId) {
		// systemId is already resolved; so don't touch
		if (systemId != null && systemId.startsWith("jar:")) {
			return null;
		}
		
		if (systemId == null && file != null) {
			systemId = findSystemIdFromFile(file, publicId);
		}
		
		if (systemId == null && publicId == null) {
			return null;
		}

		if (file == null) {
			IPath prevFile = previousFile.get();
			if (prevFile != null) {
				file = ResourcesPlugin.getWorkspace().getRoot().getFile(prevFile);
			}
			else {
				return null;
			}
		}
		else {
			previousFile.set(file.getFullPath());
		}
		
		ProjectClasspathUriResolver resolver = getProjectResolver(file);
		if (resolver != null) {
			return resolver.resolveOnClasspath(publicId, systemId);
		}

		return null;
	}
	
	private ProjectClasspathUriResolver getProjectResolver(IFile file) {
		IProject project = file.getProject();
		
		// no project resolver if not a spring project
		IBeansProject beansProject = BeansCorePlugin.getModel().getProject(project);
		if (beansProject == null) {
			return null;
		}

		if (!NamespaceUtils.useNamespacesFromClasspath(project)) {
			return null;
		}
		
		if (!checkFileExtension(file, beansProject)) {
			return null;
		}
		
		synchronized(projectResolvers) {
			if (projectResolvers.containsKey(project)) {
				return projectResolvers.get(project);
			}
			
			ProjectClasspathUriResolver resolver = new ProjectClasspathUriResolver(project);
			projectResolvers.put(project, resolver);
			return resolver;
		}
	}
	
	public void elementChanged(ElementChangedEvent event) {
		for (IJavaElementDelta delta : event.getDelta().getAffectedChildren()) {
			if ((delta.getFlags() & IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED) != 0
					|| (delta.getFlags() & IJavaElementDelta.F_CLASSPATH_CHANGED) != 0) {
				resetForChangedElement(delta.getElement());
			}
			else if ((delta.getFlags() & IJavaElementDelta.F_CLOSED) != 0) {
				resetForChangedElement(delta.getElement());
			}
			else if ((delta.getKind() & IJavaElementDelta.REMOVED) != 0) {
				resetForChangedElement(delta.getElement());
			}
		}
	}

	private void resetForChangedElement(IJavaElement element) {
		for(IProject project : projectResolvers.keySet()) {
			IJavaProject javaProject = JdtUtils.getJavaProject(project);
			if (javaProject != null) {
				if (javaProject.equals(element) || javaProject.isOnClasspath(element)) {
					projectResolvers.remove(project);
				}
			}
		}
	}

	/**
	 * try to extract the system-id of the given namespace from the xml file
	 * @since 2.6.0
	 */
	private String findSystemIdFromFile(IFile file, String publicIc) {
		InputStream contents = null;
		try {
			contents = file.getContents();
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setValidating(false);
			builderFactory.setNamespaceAware(true);
			
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document doc = builder.parse(contents);
			
			DocumentAccessor accessor = new DocumentAccessor();
			accessor.pushDocument(doc);
			SchemaLocations locations = accessor.getCurrentSchemaLocations();
			
			String location = locations.getSchemaLocation(publicIc);
			return location;
		} catch (Exception e) {
			// do nothing, systemId cannot be identified
		} finally {
			if (contents != null) {
				try {
					contents.close();
				} catch (IOException e) {
					// do nothing, systemId cannot be identified
				}
			}
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
		
		if (file.getName().endsWith(".xsd")) {
		    return true;
		}
		
		return false;
	}

}
