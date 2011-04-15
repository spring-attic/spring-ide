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
package org.springframework.ide.eclipse.internal.uaa.monitor;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.springframework.ide.eclipse.internal.uaa.IUsageMonitor;
import org.springframework.ide.eclipse.uaa.IUaa;
import org.springframework.ide.eclipse.uaa.UaaUtils;
import org.springframework.uaa.client.UaaDetectedProducts.ProductInfo;
import org.springframework.uaa.client.UaaServiceFactory;
import org.springframework.util.ClassUtils;

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

	private static final String IMPLEMENTATION_VERSION = "Implementation-Version";

	private static final String M2_REPO = "M2_REPO";

	private static final String MANIFEST_FILE_NAME = "MANIFEST.MF";

	private static final String META_INF_DIRECTORY_NAME = "META-INF";

	private static final String SPECIFICATION_VERSION = "Specification-Version";

	private static final Pattern VERSION_PATTERN = Pattern.compile(".*[-_]([0-9])(\\.[_\\-A-Za-z0-9.]*)?\\.jar");

	private IElementChangedListener changedListener = new IElementChangedListener() {

		public void elementChanged(ElementChangedEvent event) {
			for (IJavaElementDelta delta : event.getDelta().getAffectedChildren()) {
				if ((delta.getFlags() & IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED) != 0) {
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

	/**
	 * {@inheritDoc}
	 */
	public void startMonitoring(IUaa manager) {
		this.manager = manager;

		Job startup = new Job("Initializing classpath-based usage monitoring") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				// Before we start get all projects and record libraries
				for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
					if (UaaUtils.isJavaProject(project) && project.isOpen() && project.isAccessible()) {
						projectClasspathChanged(UaaUtils.getJavaProject(project));
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

	private JarEntryDirectory getJarEntryDirectory(IPackageFragmentRoot entry) {
		try {
			for (Object nonJavaResource : entry.getNonJavaResources()) {
				if (nonJavaResource instanceof JarEntryDirectory) {
					JarEntryDirectory directory = ((JarEntryDirectory) nonJavaResource);
					if (directory != null && META_INF_DIRECTORY_NAME.equals(directory.getName())) {
						return directory;
					}
				}
			}
		}
		catch (JavaModelException e) {
		}
		return null;
	}

	private List<ProductInfo> getProducts() {
		// Prevent concurrent modifications
		return new ArrayList<ProductInfo>(UaaServiceFactory.getUaaDetectedProducts().getDetectedProductInfos());
	}

	private void projectClasspathChanged(IJavaProject source) {
		IProject project = source.getProject();
		List<ProductMatch> productMatches = new ArrayList<ProductMatch>();
		try {
			IPackageFragmentRoot[] classpath = source.getPackageFragmentRoots();
			for (IPackageFragmentRoot entry : classpath) {
				// Obtain the ProductMatch from the classpath entry
				ProductMatch productMatch = readPackageFragmentRoot(source, entry);
				
				if (productMatch != null) {

					// We only want the highest version per product; therefore check if one has
					// already been registered and compare the versions
					int ix = productMatches.indexOf(productMatch);
					if (ix >= 0) {
						String newVersion = productMatch.getVersion();
						String oldVersion = productMatches.get(ix).getVersion();
						if (newVersion != null && newVersion.compareTo(oldVersion) > 0) {
							productMatches.remove(ix);
							productMatches.add(productMatch);
						}
					}
					else {
						productMatches.add(productMatch);
					}
				}
				
			}
		}
		catch (Exception e) {
			// Intentionally only onto the console
			e.printStackTrace();
		}
		finally {
			for (ProductMatch productMatch : productMatches) {
				recordEvent(productMatch, project.getName());
			}
		}
	}

	private String readManifest(IJarEntryResource jarEntryResource, IPackageFragmentRoot entry) {
		InputStream is = null;
		try {
			String fileName = "";
			if (entry.getPath() != null) {
				fileName = entry.getPath().lastSegment();
			}

			Manifest manifest = new Manifest();
			is = jarEntryResource.getContents();
			manifest.read(is);

			String bundleVersion = null;
			String implementationVersion = null;
			String specificationVersion = null;

			Attributes attributes = manifest.getMainAttributes();
			if (attributes != null) {
				bundleVersion = attributes.getValue(Constants.BUNDLE_VERSION);
				implementationVersion = attributes.getValue(IMPLEMENTATION_VERSION);
				specificationVersion = attributes.getValue(SPECIFICATION_VERSION);
			}

			Map<String, Attributes> map = manifest.getEntries();
			for (Iterator<String> it = map.keySet().iterator(); it.hasNext();) {
				String entryName = it.next();
				Attributes attrs = map.get(entryName);
				for (Iterator<?> it2 = attrs.keySet().iterator(); it2.hasNext();) {
					Attributes.Name attrName = (Attributes.Name) it2.next();
					if (attrName.toString().equals(Constants.BUNDLE_VERSION)) {
						bundleVersion = attrs.getValue(attrName);
					}
					else if (attrName.toString().equals(IMPLEMENTATION_VERSION)) {
						implementationVersion = attrs.getValue(attrName);
					}
					else if (attrName.toString().equals(SPECIFICATION_VERSION)) {
						implementationVersion = attrs.getValue(attrName);
					}
				}
			}

			if (bundleVersion != null) {
				return bundleVersion;
			}
			if (fileName.contains(implementationVersion)) {
				return implementationVersion;
			}
			if (fileName.contains(specificationVersion)) {
				return specificationVersion;
			}
			if (implementationVersion != null) {
				return implementationVersion;
			}
			if (specificationVersion != null) {
				return specificationVersion;
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
						// Ignore
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

	@SuppressWarnings("deprecation")
	private ProductMatch readPackageFragmentRoot(IJavaProject source, IPackageFragmentRoot entry)
			throws JavaModelException {
		// Quick assertion that we only check JARs that exist
		if (!entry.exists() || !entry.isArchive()) {
			return null;
		}

		// Check for previous matches or misses
		if (matches.containsKey(entry.getElementName())) {
			ProductMatch match = matches.get(entry.getElementName());
			return match;
		}
		else if (noMatches.contains(entry.getElementName())) {
			return null;
		}

		String groupId = null;
		String artifactId = null;
		String version = null;
		ProductInfo productInfo = null;

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

			IPath m2RepoPath = JavaCore.getClasspathVariable(M2_REPO);
			if (m2RepoPath == null) {
				m2RepoPath = ResourcesPlugin.getWorkspace().getPathVariableManager().getValue(M2_REPO);
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
						if (packageFragment.getClassFile(typeName + ClassUtils.CLASS_FILE_SUFFIX).exists()) {
							artifactId = info.getArtifactId();
							groupId = info.getGroupId();
							productInfo = info;
							break;
						}
					}
				}
			}
		}

		// Step 4: Obtain version from MANIFEST.MF
		if (groupId != null && artifactId != null && metaInfDirectory != null && version == null) {
			for (IJarEntryResource jarEntryResource : metaInfDirectory.getChildren()) {
				if (MANIFEST_FILE_NAME.equals(jarEntryResource.getName())) {
					version = readManifest(jarEntryResource, entry);
				}
			}
		}

		// Step 5: Obtain version from file name
		if (groupId != null && artifactId != null && version == null && entry.getPath() != null) {
			String fileName = entry.getPath().lastSegment();

			// Use regular expression to match any version number
			Matcher matcher = VERSION_PATTERN.matcher(fileName);
			if (matcher.matches()) {
				StringBuilder builder = new StringBuilder();
				for (int i = 1; i <= matcher.groupCount(); i++) {
					builder.append(matcher.group(i));
				}
				version = builder.toString();
			}
		}

		// Step 6: Construct the ProductMatch
		ProductMatch productMatch = null;
		if (productInfo == null) {
			for (ProductInfo info : getProducts()) {
				if (info.getArtifactId().equals(artifactId) && info.getGroupId().equals(groupId)) {
					productInfo = info;
					productMatch = new ProductMatch(info, version);
					break;
				}
			}
		}
		else {
			productMatch = new ProductMatch(productInfo, version);
		}

		// Step 7: Store ProductMatch for faster access in subsequent runs
		if (productMatch != null) {
			matches.put(entry.getElementName(), productMatch);
			return productMatch;
		}
		else {
			noMatches.add(entry.getElementName());
			return null;
		}
	}

	private void recordEvent(ProductMatch productMatch, String projectName) {
		if (productMatch != null && productMatch.getInfo() != null && productMatch.getVersion() != null) {
			ProductInfo info = productMatch.getInfo();
			if (info != null && UaaServiceFactory.getUaaDetectedProducts().shouldReportUsage(info.getGroupId(), info.getArtifactId())) {
				manager.registerProductUse(info.getProductName(), productMatch.getVersion(), projectName);
			}
		}
	}

	private class Product {

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

	private class ProductMatch {

		private final ProductInfo info;

		private final String version;

		public ProductMatch(ProductInfo info, String version) {
			this.info = info;
			this.version = version;
		}

		@Override
		public boolean equals(Object other) {
			// Don't add the version field to the equals method
			if (other instanceof ProductMatch) {
				return info.equals(((ProductMatch) other).info);
			}
			return false;
		}

		public ProductInfo getInfo() {
			return info;
		}

		public String getVersion() {
			return version;
		}

		@Override
		public int hashCode() {
			// Don't add the version field to the hashcode method
			return info.hashCode();
		}
	}
}
