/*******************************************************************************
 *  Copyright (c) 2013 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template.infrastructure;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springsource.ide.eclipse.commons.content.core.ContentItem;
import org.springsource.ide.eclipse.commons.content.core.ContentManager;
import org.springsource.ide.eclipse.commons.core.ZipFileUtil;

/**
 * Loads content defined by descriptors for creating projects. These
 * descriptors, and the resources that they point to, are contained within an
 * Eclipse bundle, and referenced by relative file URLs.
 */
public class BundleContentLoader {

	private final Bundle bundle;

	private final ContentItem contentItem;

	private final SimpleProjectContentManager contentManager;

	public BundleContentLoader(ContentItem contentItem, Bundle bundle, SimpleProjectContentManager contentManager) {
		this.contentItem = contentItem;
		this.bundle = bundle;
		this.contentManager = contentManager;
	}

	/**
	 * Unzip (load) contents of a project creation descriptor into a
	 * installation directory pointed to by a content manager. If the contents
	 * are already unzipped , it will not unzip again. The contents of the
	 * descriptor are unzipped in a subdirectory with a name containing the
	 * descriptor name and version number. If a directory with that same name
	 * already exists, and contains at least one descriptor file, it will not
	 * unzip the contents. Therefore, to install newer versions of the same
	 * descriptor, make sure that the version number is changed in the
	 * descriptor definition.
	 * @param monitor
	 * @throws CoreException
	 */
	public void load(IProgressMonitor monitor) throws CoreException {

		SubMonitor progress = SubMonitor.convert(monitor, 20);

		try {

			File baseInstallDirectory = contentManager.getInstallDirectory();

			if (baseInstallDirectory == null || !baseInstallDirectory.exists()) {
				throw new CoreException(
						new Status(
								IStatus.ERROR,
								WizardPlugin.PLUGIN_ID,
								NLS.bind(
										"Installation directory for bundled contents in bundle {0} does not exist. Unable to extract contents to local directory.",
										WizardPlugin.getDefault().getBundle().getSymbolicName())));
			}
			File directory = new File(baseInstallDirectory, contentItem.getPath());

			// If it already exists, do not load it again
			if (contentManager.exists(directory)) {
				return;
			}

			String url = contentItem.getLocalDescriptor().getUrl();
			InputStream bundleResourceInput = null;

			// This may be a relative file URL for a resource in the bundle
			try {
				URI uri = new URI(url);
				if (bundle != null && uri.getScheme().startsWith("file")) {
					IPath path = new Path(uri.getPath());
					bundleResourceInput = FileLocator.openStream(WizardPlugin.getDefault().getBundle(), path, false);
				}
			}
			catch (URISyntaxException e) {
				String message = NLS.bind(
						"Incorrect URI for resource {0} in bundle {1}. Unable to load template data.", url,
						bundle.getLocation());
				throw new CoreException(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, message, e));

			}
			catch (IOException e) {
				String message = NLS.bind(
						"I/O error. Failed to read resource {0} in bundle {1}. Unable to load template data.", url,
						bundle.getLocation());
				throw new CoreException(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, message, e));

			}

			File archiveFile = new File(baseInstallDirectory, contentItem.getPath() + ContentManager.ARCHIVE_EXTENSION);

			if (bundleResourceInput != null) {
				// Loading also closes the input stream
				load(bundleResourceInput, archiveFile, directory, monitor);
			}
			else {
				throw new CoreException(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID,
						"Unable to create input stream to descriptor files in bundle: "
								+ WizardPlugin.getDefault().getBundle().getSymbolicName()));
			}
		}
		finally {
			progress.done();
		}

	}

	/**
	 * This operation does several things:
	 * 
	 * <p/>
	 * 1. Copies a zip file containing template files (e.g., template.xml,
	 * template.zip, and wizard.json) from the bundle into a local specified
	 * directory
	 * <p/>
	 * 2. Unzips the copied local archive file into that same directory
	 * <p/>
	 * 3. Deletes the copied local archive
	 * @param in inputstream to the archive file in the bundle
	 * @param localArchiveFile archive file where the contents of the the
	 * inputstream should be written to. In other words, this is the local copy
	 * of the zip file in the bundle
	 * @param directory directory where local archive file is located as well as
	 * where the contents of the local archive file are unzipped
	 * @param monitor
	 */
	protected void load(InputStream in, File localArchiveFile, File directory, IProgressMonitor monitor)
			throws CoreException {

		SubMonitor progress = SubMonitor.convert(monitor, 100);

		if (!directory.exists()) {
			directory.mkdirs();
		}

		OutputStream out = null;

		// Copy zip file from bundle to local zip file first. IMPORTANT: close
		// the streams first to flush the buffers before attempting to read the
		// local zip file, as for
		// zip files that are too small, all the contents may be in the output
		// buffer and not written to the local zip file
		try {
			out = new BufferedOutputStream(new FileOutputStream(localArchiveFile));

			byte[] buf = new byte[40 * 1024];
			int read;
			while ((read = in.read(buf)) >= 0) {
				if (read > 0) {
					out.write(buf, 0, read);
				}
			}

		}
		catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID,
					"I/O error while copying template zip file " + localArchiveFile.getAbsolutePath() + " to "
							+ directory.getAbsolutePath() + " ---- " + e.getMessage(), e));

		}
		finally {

			try {
				// Close the output stream first to flush the contents into the
				// local zip file.
				if (out != null) {
					out.close();
				}

				if (in != null) {
					in.close();
				}
			}
			catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID,
						"I/O error while copying template zip file " + localArchiveFile.getAbsolutePath() + " to "
								+ directory.getAbsolutePath() + " ---- " + e.getMessage(), e));
			}
		}

		// Now that the zip file has been copied , unzip its contents
		try {
			URL fileUrl = localArchiveFile.toURI().toURL();
			ZipFileUtil.unzip(fileUrl, directory, progress.newChild(30));
			if (directory.listFiles().length <= 0) {
				String message = NLS.bind("Zip file {0} appears to be empty", localArchiveFile);
				throw new CoreException(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, message));
			}
		}
		catch (MalformedURLException e) {
			throw new CoreException(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID,
					"Unable to unzip template content due to malformed URL: " + e.getMessage(), e));
		}
		catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID,
					"I/O error while unzipping template zip file " + localArchiveFile.getAbsolutePath() + " into "
							+ directory.getAbsolutePath() + " --- " + e.getMessage(), e));
		}
		finally {
			// Once unzipped, the copied archive file is no longer needed
			localArchiveFile.delete();
		}

	}
}
