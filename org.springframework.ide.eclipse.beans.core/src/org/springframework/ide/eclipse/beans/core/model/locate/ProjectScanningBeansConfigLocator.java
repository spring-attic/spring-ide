/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.model.locate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.document.DOMModelImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.springframework.beans.factory.xml.NamespaceHandlerResolver;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.internal.model.DelegatingNamespaceHandlerResolver;
import org.springframework.ide.eclipse.beans.core.namespaces.NamespaceUtils;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.util.StringUtils;

/**
 * Basic {@link IBeansConfigLocator} that is capable for scanning an {@link IProject} or
 * {@link IJavaProject} for Spring XML configuration files.
 * <p>
 * Only those XML files that have any known namespace uri at the root element level are being
 * considered to be a suitable candidate.
 * @author Christian Dupuis
 * @since 2.0.5
 */
@SuppressWarnings("restriction")
public class ProjectScanningBeansConfigLocator extends
		AbstractJavaProjectPathMatchingBeansConfigLocator {

	/** Ant-style that matches on every XML file */
	private String ALLOWED_FILE_PATTERN = "**/*";

	/** Internal cache for {@link NamespaceHandlerResolver}s keyed by their {@link IProject} */
	private Map<IProject, NamespaceHandlerResolver> namespaceResoverCache = 
		new HashMap<IProject, NamespaceHandlerResolver>();
	
	/** Configured file patters derived from the configured file patterns */
	private List<String> configuredFilePatterns = null;
	
	/** Configured file extensions from the dialog */ 
	private List<String> configuredFileExtensions = null;
	
	/**
	 * Constructor taking a string of CSV file extensions
	 * @param configuredFileSuffixes
	 */
	public ProjectScanningBeansConfigLocator(String configuredFileSuffixes) {
		configuredFilePatterns = new ArrayList<String>();
		configuredFileExtensions = new ArrayList<String>();
		for (String filePattern : StringUtils.commaDelimitedListToStringArray(configuredFileSuffixes)) {
			filePattern = filePattern.trim();
			int ix = filePattern.lastIndexOf('.');
			if (ix != -1) {
				configuredFileExtensions.add(filePattern.substring(ix + 1));
			}
			else {
				configuredFileExtensions.add(filePattern);
			}
			configuredFilePatterns.add(ALLOWED_FILE_PATTERN + filePattern);
		}
	}
	
	/**
	 * As this locator is not intended to be used at runtime, we don't need to listen to any
	 * resource changes.
	 */
	public boolean requiresRefresh(IFile file) {
		return false;
	}

	/**
	 * Supports both an normal {@link IProject} and a {@link IJavaProject} but it needs to have the
	 * Spring nature.
	 */
	@Override
	public boolean supports(IProject project) {
		return SpringCoreUtils.isSpringProject(project);
	}

	/**
	 * Returns a {@link NamespaceHandlerResolver} for the given {@link IProject}. First looks in
	 * the {@link #namespaceResoverCache cache} before creating a new instance.
	 */
	private NamespaceHandlerResolver getNamespaceHandlerResolver(IProject project) {
		if (!namespaceResoverCache.containsKey(project)) {
			namespaceResoverCache.put(project, new DelegatingNamespaceHandlerResolver(JdtUtils
					.getClassLoader(project), null));
		}
		return namespaceResoverCache.get(project);
	}
	
	/**
	 * Filters out every {@link IFile} which is has unknown root elements in its XML content.
	 */
	@Override
	protected Set<IFile> filterMatchingFiles(Set<IFile> files) {
		Set<IFile> detectedFiles = new LinkedHashSet<IFile>();
		for (IFile file : files) {
			IStructuredModel model = null;
			try {
				try {
					model = StructuredModelManager.getModelManager().getExistingModelForRead(file);
				}
				catch (RuntimeException e) {
					// sometimes WTP throws a NPE in concurrency situations
				}
				if (model == null) {
					model = StructuredModelManager.getModelManager().getModelForRead(file);
				}
				if (model != null) {
					IDOMDocument document = ((DOMModelImpl) model).getDocument();
					if (document != null && document.getDocumentElement() != null) {
						String namespaceUri = document.getDocumentElement().getNamespaceURI();
						if (NamespaceUtils.DEFAULT_NAMESPACE_URI.equals(namespaceUri)
								|| getNamespaceHandlerResolver(file.getProject()).resolve(
										namespaceUri) != null) {
							detectedFiles.add(file);
						}
					}
				}
			}
			catch (IOException e) {
				BeansCorePlugin.log(e);
			}
			catch (CoreException e) {
				BeansCorePlugin.log(e);
			}
			finally {
				if (model != null) {
					model.releaseFromRead();
				}
			}
		}
		return detectedFiles;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<String> getAllowedFilePatterns() {
		return configuredFilePatterns;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected List<String> getAllowedFileExtensions() {
		return configuredFileExtensions;
	}
	
	/**
	 * Returns the root directories to scan.
	 */
	@Override
	protected Set<IPath> getRootDirectories(IProject project) {
		if (!JdtUtils.isJavaProject(project)) {
			Set<IPath> rootDirectories = new LinkedHashSet<IPath>();
			rootDirectories.add(project.getFullPath());
			return rootDirectories;
		}
		return super.getRootDirectories(project);
	}
	
}
