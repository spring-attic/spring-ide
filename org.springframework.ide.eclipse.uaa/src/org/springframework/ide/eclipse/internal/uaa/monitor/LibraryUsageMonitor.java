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
package org.springframework.ide.eclipse.internal.uaa.monitor;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJarEntryResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarEntryDirectory;
import org.osgi.framework.Constants;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.internal.uaa.IUsageMonitor;
import org.springframework.ide.eclipse.uaa.IUaa;
import org.springframework.uaa.client.DetectedProducts;
import org.springframework.uaa.client.DetectedProducts.ProductInfo;

/**
 * {@link IUsageMonitor} implementation that captures libraries used in Eclipse projects.
 * <p>
 * Note: only libraries that specify <code>org.springframework.*</code> as <code>Bundle-SymbolicName</code> are being
 * considered.
 * @author Christian Dupuis
 * @since 2.5.2
 */
@SuppressWarnings("restriction")
public class LibraryUsageMonitor implements IUsageMonitor {

	private IElementChangedListener changedListener = new IElementChangedListener() {

		public void elementChanged(ElementChangedEvent event) {
			for (IJavaElementDelta delta : event.getDelta().getAffectedChildren()) {
				if ((delta.getFlags() & IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED) != 0
						|| (delta.getFlags() & IJavaElementDelta.F_CLASSPATH_CHANGED) != 0
						|| (delta.getFlags() & IJavaElementDelta.F_OPENED) != 0) {
					final IJavaElement source = delta.getElement();
					if (source instanceof IJavaProject) {
						Job update = new Job(String.format("Recording usage data for '%s'", source.getElementName())) {

							@Override
							protected IStatus run(IProgressMonitor arg0) {
								projectClasspathChanged((IJavaProject) source);
								return Status.OK_STATUS;
							}

						};
						update.setSystem(true);
						update.schedule();
					}
				}
			}
		}
	};

	private IUaa manager;

	private Map<String, ProductMatch> matches = new ConcurrentHashMap<String, ProductMatch>();

	private Set<String> noMatches = new CopyOnWriteArraySet<String>();

	@SuppressWarnings("deprecation")
	public void readPackageFragmentRoot(IJavaProject source, IPackageFragmentRoot entry) throws JavaModelException {
		// Quick assertion that we only check JARs
		if (entry.getKind() != IPackageFragmentRoot.K_BINARY && !entry.isArchive()) {
			return;
		}

		String project = source.getProject().getName();

		// Check for previous matches or misses
		if (matches.containsKey(entry.getElementName())) {
			ProductMatch match = matches.get(entry.getElementName());
			ProductInfo info = match.getInfo();
			recordEvent(entry, info.getGroupId(), info.getArtifactId(), match.getVersion(), project);
			return;
		}
		else if (noMatches.contains(entry.getElementName())) {
			return;
		}

		String groupId = null;
		String artifactId = null;
		String version = null;

		JarEntryDirectory metaInfDirectory = getJarEntryDirectory(entry);

		// Step 1: Check META-INF/maven
		if (metaInfDirectory != null) {
			for (IJarEntryResource jarEntryResource : metaInfDirectory.getChildren()) {
				if ("maven".equals(jarEntryResource.getName())) {
					Product product = readMaven(jarEntryResource, entry);
					if (product != null) {
						groupId = product.getGroupId();
						artifactId = product.getArtifactId();
						version = product.getVersion();
						break;
					}
				}
			}
		}

		// Step 2: Check path if it follows the Maven convention of groupId/artifactId/version
		if (artifactId == null || groupId == null) {
			IPath jarPath = entry.getPath();

			IPath m2RepoPath = JavaCore.getClasspathVariable("M2_REPO");
			if (m2RepoPath == null) {
				m2RepoPath = ResourcesPlugin.getWorkspace().getPathVariableManager().getValue("M2_REPO");
			}
			if (m2RepoPath != null && m2RepoPath.isPrefixOf(jarPath)) {
				jarPath = jarPath.removeFirstSegments(m2RepoPath.segmentCount());
				int segments = jarPath.segmentCount();
				for (int i = 0; i < segments - 1; i++) {
					if (i == 0) {
						groupId = jarPath.segment(i);
					}
					else if (i > 0 && i < segments - 3) {
						groupId += "." + jarPath.segment(i);
					}
					else if (i == segments - 3) {
						artifactId = jarPath.segment(i);
					}
					else if (i == segments - 2) {
						version = jarPath.segment(i);
					}
				}
			}
		}

		// Step 3: Check for typeName match
		if (artifactId == null || groupId == null) {
			for (ProductInfo info : getProducts()) {
				String typeName = info.getTypeName();
				String packageName = "";
				if (typeName != null) {
					int i = typeName.lastIndexOf('.');
					if (i > 0) {
						packageName = typeName.substring(0, i);
						typeName = typeName.substring(i + 1);
					}
					IPackageFragment packageFragment = entry.getPackageFragment(packageName);
					if (packageFragment.exists()) {
						if (packageFragment.getClassFile(typeName + ".class").exists()) {
							artifactId = info.getArtifactId();
							groupId = info.getGroupId();
							break;
						}
					}
				}
			}
		}

		// Step 4: Obtain version from MANIFEST.MF
		if (metaInfDirectory != null && version == null) {
			for (IJarEntryResource jarEntryResource : metaInfDirectory.getChildren()) {
				if ("MANIFEST.MF".equals(jarEntryResource.getName())) {
					version = readManifest(jarEntryResource, entry);
				}
			}
		}

		// Step 5: Report found product
		recordEvent(entry, groupId, artifactId, version, project);
	}

	private JarEntryDirectory getJarEntryDirectory(IPackageFragmentRoot entry) {
		try {
			for (Object nonJavaResource : entry.getNonJavaResources()) {
				if (nonJavaResource instanceof JarEntryDirectory) {
					JarEntryDirectory directory = ((JarEntryDirectory) nonJavaResource);
					if (directory != null && "META-INF".equals(directory.getName())) {
						return directory;
					}
				}
			}
		}
		catch (JavaModelException e) {
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void startMonitoring(IUaa manager) {
		this.manager = manager;

		Job startup = new Job("Initializing classpath usage monitoring") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				// Before we start get all projects and record libraries
				for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
					if (JdtUtils.isJavaProject(project) && project.isOpen() && project.isAccessible()) {
						projectClasspathChanged(JdtUtils.getJavaProject(project));
					}
				}

				JavaCore.addElementChangedListener(changedListener);
				return Status.OK_STATUS;
			}

		};
		startup.setSystem(true);
		startup.schedule(5000);
	}

	/**
	 * {@inheritDoc}
	 */
	public void stopMonitoring() {
		if (changedListener != null) {
			JavaCore.removeElementChangedListener(changedListener);
		}
	}

	private void projectClasspathChanged(IJavaProject source) {
		try {
			IPackageFragmentRoot[] classpath = source.getPackageFragmentRoots();
			for (IPackageFragmentRoot entry : classpath) {
				readPackageFragmentRoot(source, entry);
			}
		}
		catch (Exception e) {
			// intentionally only onto the console
			e.printStackTrace();
		}
	}

	private String readManifest(IJarEntryResource jarEntryResource, IPackageFragmentRoot entry) {
		InputStream is = null;
		try {
			Manifest manifest = new Manifest();
			is = jarEntryResource.getContents();
			manifest.read(is);
			Attributes attr = manifest.getMainAttributes();
			if (attr != null) {
				String version = attr.getValue(Constants.BUNDLE_VERSION);
				if (version != null) {
					return version;
				}
			}
		}
		catch (Exception e) {
		}
		finally {
			if (is != null) {
				try {
					is.close();
				}
				catch (IOException e) {
				}
			}
		}
		return null;
	}

	private Product readMaven(IJarEntryResource jarEntryResource, IPackageFragmentRoot root) {
		if (jarEntryResource.getName().equals("pom.properties")) {
			InputStream is = null;
			try {
				Properties pom = new Properties();
				is = jarEntryResource.getContents();
				pom.load(is);
				String version = pom.getProperty("version");
				String groupId = pom.getProperty("groupId");
				String artifactId = pom.getProperty("artifactId");
				if (version != null && groupId != null && artifactId != null) {
					return new Product(artifactId, groupId, version);
				}
			}
			catch (Exception e) {
			}
			finally {
				if (is != null) {
					try {
						is.close();
					}
					catch (IOException e) {
					}
				}
			}
		}
		else {
			for (IJarEntryResource resource : jarEntryResource.getChildren()) {
				Product product = readMaven(resource, root);
				if (product != null) {
					return product;
				}
			}
		}
		return null;
	}

	private void recordEvent(IPackageFragmentRoot packageFragment, String groupId, String artifactId, String version,
			String name) {
		if (groupId != null && artifactId != null) {
			for (ProductInfo info : getProducts()) {
				if (info.getArtifactId().equals(artifactId) && info.getGroupId().equals(groupId)) {
					manager.registerProductUse(info.getProductName(), version, name);
					matches.put(packageFragment.getElementName(), new ProductMatch(info, version));
					return;
				}
			}
		}
		noMatches.add(packageFragment.getElementName());
	}

	private List<ProductInfo> getProducts() {
		DetectedProducts.setDocumentBuilderFactory(SpringCoreUtils.getDocumentBuilderFactory());
		return DetectedProducts.getProducts();
	}

	private static class ProductMatch {

		private final ProductInfo info;

		private final String version;

		public ProductMatch(ProductInfo info, String version) {
			this.info = info;
			this.version = version;
		}

		public ProductInfo getInfo() {
			return info;
		}

		public String getVersion() {
			return version;
		}
	}

	private static class Product {

		private final String artifactId;

		private final String groupId;

		private final String version;

		public Product(String artifactId, String groupId, String version) {
			this.artifactId = artifactId;
			this.groupId = groupId;
			this.version = version;
		}

		public String getArtifactId() {
			return artifactId;
		}

		public String getGroupId() {
			return groupId;
		}

		public String getVersion() {
			return version;
		}
	}

}
