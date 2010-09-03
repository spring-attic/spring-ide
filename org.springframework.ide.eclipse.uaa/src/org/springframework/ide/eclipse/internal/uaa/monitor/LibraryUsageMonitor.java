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
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JarEntryDirectory;
import org.osgi.framework.Constants;
import org.springframework.ide.eclipse.core.java.JdtUtils;
import org.springframework.ide.eclipse.internal.uaa.IUsageMonitor;
import org.springframework.ide.eclipse.internal.uaa.UaaManager;

/**
 * {@link IUsageMonitor} implementation that captures libraries used in Eclipse projects.
 * <p>
 * Note: only libraries that specify <code>org.springframework.*</code> as <code>Bundle-SymbolicName</code> are being
 * considered.
 * @author Christian Dupuis
 * @since 2.5.0
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
					if (JdtUtils.isJavaProject(project)) {
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
				for (Object nonJavaResource : entry.getNonJavaResources()) {
					if (nonJavaResource instanceof JarEntryDirectory) {
						JarEntryDirectory directory = ((JarEntryDirectory) nonJavaResource);
						if (directory != null && "META-INF".equals(directory.getName())) {
							for (IJarEntryResource jarEntryResource : directory.getChildren()) {
								if ("MANIFEST.MF".equals(jarEntryResource.getName())) {
									readManifest(jarEntryResource);
								}
							}
						}
					}
				}
			}
		}
		catch (JavaModelException e) {
			// TODO CD report exception
		}
	}

	private void readManifest(IJarEntryResource jarEntryResource) {
		InputStream is = null;
		try {
			Manifest manifest = new Manifest();
			is = jarEntryResource.getContents();
			manifest.read(is);
			Attributes attr = manifest.getMainAttributes();
			if (attr != null) {
				String symbolicName = attr.getValue(Constants.BUNDLE_SYMBOLICNAME);
				String version = attr.getValue(Constants.BUNDLE_VERSION);
				recordEvent(symbolicName, version);
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
	}

	private void recordEvent(String symbolicName, String version) {
		// Due to privacy considerations we only record libraries from us
		if (manager != null && symbolicName != null && symbolicName.startsWith("org.springframework")) {
			manager.registerProductUse(symbolicName, version);
		}
	}

}
