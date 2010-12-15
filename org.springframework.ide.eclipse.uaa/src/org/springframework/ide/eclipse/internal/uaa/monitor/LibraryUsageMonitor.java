/*******************************************************************************
 * Copyright (c) 2005, 2010 Spring IDE Developers
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
import org.springframework.ide.eclipse.internal.uaa.UaaManager;
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
	
	private volatile UaaManager manager;
	
	private Map<String, ProductMatch> matches = new ConcurrentHashMap<String, ProductMatch>();
	private Set<String> noMatches = new CopyOnWriteArraySet<String>();

	public void readPackageFragmentRoot(IJavaProject source, IPackageFragmentRoot entry) throws JavaModelException {
		String project = source.getProject().getName();
		
		if (matches.containsKey(entry.getElementName())) {
			ProductMatch match = matches.get(entry.getElementName());
			ProductInfo info = match.getInfo();
			recordEvent(entry, info.getGroupId(), info.getArtifactId(), match.getVersion(), project);
			return;
		}
		else if (noMatches.contains(entry.getElementName())) {
			return;
		}
		
		String version = null;
		for (Object nonJavaResource : entry.getNonJavaResources()) {
			if (nonJavaResource instanceof JarEntryDirectory) {
				JarEntryDirectory directory = ((JarEntryDirectory) nonJavaResource);
				if (directory != null && "META-INF".equals(directory.getName())) {
					for (IJarEntryResource jarEntryResource : directory.getChildren()) {
						if ("maven".equals(jarEntryResource.getName())) {
							if (readMaven(jarEntryResource, entry, project)) {
								return;
							}
						}
						else if ("MANIFEST.MF".equals(jarEntryResource.getName())) {
							version = readManifest(jarEntryResource, entry, project);
						}
					}
				}
			}
		}
		
		if (version != null) {
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
							recordEvent(entry, info.getGroupId(), info.getArtifactId(), version, source.getProject()
									.getName());
							return;
						}
					}
				}
			}
		}
		noMatches.add(entry.getElementName());
	}

	/**
	 * {@inheritDoc}
	 */
	public void startMonitoring(UaaManager manager) {
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
		JavaCore.removeElementChangedListener(changedListener);
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

	private String readManifest(IJarEntryResource jarEntryResource, IPackageFragmentRoot entry, String projectId) {
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
			// TODO CD report exception
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

	private boolean readMaven(IJarEntryResource jarEntryResource, IPackageFragmentRoot root, String name) {
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
					recordEvent(root, groupId, artifactId, version, name);
					return true;
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
				if (readMaven(resource, root, name)) {
					return true;
				}
			}
		}
		return false;
	}

	private void recordEvent(IPackageFragmentRoot packageFragment, String groupId, String artifactId, String version,
			String name) {
		for (ProductInfo info : getProducts()) {
			if (info.getArtifactId().equals(artifactId) && info.getGroupId().equals(groupId)) {
				manager.registerProductUse(info.getProductName(), version, name);
				matches.put(packageFragment.getElementName(), new ProductMatch(info, version));
				return;
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

}
