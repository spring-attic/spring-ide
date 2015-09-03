/*******************************************************************************
 * Copyright (c) 2015 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.dash.cloudfoundry.packaging;

import java.io.File;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.springframework.ide.eclipse.boot.dash.BootDashActivator;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ApplicationManifestHandler;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.CloudZipApplicationArchive;
import org.springframework.ide.eclipse.boot.dash.cloudfoundry.ICloudApplicationArchiver;

/**
 * Archiver strategy that consults manifest.yml file for an entry pointing to an existing archive.
 * The existing archive is returned by the archiver rather than building an archive.
 */
public class CloudApplicationArchiverStrategyFromManifest implements CloudApplicationArchiverStrategy {

	private IProject project;
	private String applicationName;
	private ApplicationManifestHandler parser;

	public CloudApplicationArchiverStrategyFromManifest(IProject project, String applicationName, ApplicationManifestHandler parser) {
		this.project = project;
		this.applicationName = applicationName;
		this.parser = parser;
	}

	@Override
	public ICloudApplicationArchiver getArchiver(IProgressMonitor mon) {
		final String archivePath = getArchivePath(mon);
		if (archivePath!=null) {
			return new ICloudApplicationArchiver() {
				public CloudZipApplicationArchive getApplicationArchive(IProgressMonitor monitor) throws Exception {
					return getArchive(archivePath);
				}
			};
		}
		return null;
	}

	private String getArchivePath(IProgressMonitor mon) {
		if (parser.hasManifest()) {
			if (parser.hasManifest()) {
				return parser.getApplicationProperty(applicationName, ApplicationManifestHandler.PATH_PROP, mon);
			}
		}
		return null;
	}

	private CloudZipApplicationArchive getArchive(String archivePath) throws Exception {
		Assert.isNotNull(archivePath);
		File packagedFile = null;
		// Only support paths that point to archive files
		IPath path = new Path(archivePath);
		if (path.getFileExtension() != null) {

			if (!path.isAbsolute()) {
				// Check if it is project relative first
				IFile projectRelativeFile = getProject().getFile(path);
				if (projectRelativeFile != null && projectRelativeFile.exists()) {
					packagedFile = projectRelativeFile.getLocation().toFile();
				} else {
					// Case where file exists in file system but is not
					// present in workspace (i.e. IProject may be out of
					// synch with file system)
					IPath projectPath = getProject().getLocation();
					if (projectPath != null) {
						archivePath = projectPath.append(archivePath).toString();
						File absoluteFile = new File(archivePath);
						if (absoluteFile.exists() && absoluteFile.canRead()) {
							packagedFile = absoluteFile;
						}
					}
				}
			} else {
				// See if it is an absolute path
				File absoluteFile = new File(archivePath);
				if (absoluteFile.exists() && absoluteFile.canRead()) {
					packagedFile = absoluteFile;
				}
			}
		}
		// If a path is specified but no file found stop further deployment
		if (packagedFile == null) {
			throw BootDashActivator.asCoreException(
					"No file found at: " + path + ". Unable to package the application for deployment");
		} else {
			return new CloudZipApplicationArchive(new ZipFile(packagedFile));
		}
	}

	private IProject getProject() {
		return project;
	}

}