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
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springsource.ide.eclipse.commons.content.core.ContentItem;
import org.springsource.ide.eclipse.commons.content.core.ContentManager;
import org.springsource.ide.eclipse.commons.content.core.ContentPlugin;
import org.springsource.ide.eclipse.commons.content.core.TemplateDownloader;
import org.springsource.ide.eclipse.commons.core.HttpUtil;
import org.springsource.ide.eclipse.commons.core.ZipFileUtil;

/**
 * Loads template content for templates contained in a bundle, and referenced by
 * relative file URLs. If template content are referenced by Http URLs, the Http
 * downloader is used instead.
 * 
 */
public class BundleTemplateLoader extends TemplateDownloader {

	private final Bundle bundle;

	public BundleTemplateLoader(ContentItem rootItem, Bundle bundle) {
		super(rootItem);
		this.bundle = bundle;
	}

	@Override
	public IStatus downloadTemplate(IProgressMonitor monitor) {

		SubMonitor progress = SubMonitor.convert(monitor, 20);

		ContentManager manager = ContentPlugin.getDefault().getManager();

		IStatus status = Status.OK_STATUS;

		try {
			List<ContentItem> dependencies = manager.getDependencies(rootItem);
			for (ContentItem item : dependencies) {

				String url = item.getRemoteDescriptor().getUrl();
				InputStream bundleResourceInput = null;

				// This may be a relative file URL for a resource in the bundle
				try {
					URI uri = new URI(url);
					if (bundle != null && uri.getScheme().startsWith("file")) {
						IPath path = new Path(uri.getPath());
						bundleResourceInput = FileLocator
								.openStream(WizardPlugin.getDefault().getBundle(), path, false);
					}
				}
				catch (URISyntaxException e) {
					String message = NLS.bind(
							"Incorrect URI for resource {0} in bundle {1}. Unable to load template data.", url,
							bundle.getLocation());
					status = new Status(IStatus.ERROR, ContentPlugin.PLUGIN_ID, message, e);
					break;

				}
				catch (IOException e) {
					String message = NLS.bind(
							"I/O error. Failed to read resource {0} in bundle {1}. Unable to load template data.", url,
							bundle.getLocation());
					status = new Status(IStatus.ERROR, ContentPlugin.PLUGIN_ID, message, e);
					// Proceed with the next one
					break;
				}

				File baseDirectory = manager.getInstallDirectory();
				File archiveFile = new File(baseDirectory, item.getPathFromRemoteDescriptor()
						+ ContentManager.ARCHIVE_EXTENSION);
				File directory = new File(baseDirectory, item.getPathFromRemoteDescriptor());
				startCountdownTimer(monitor);

				if (bundleResourceInput == null) {
					status = HttpUtil.download(url, archiveFile, directory, progress);
				}
				else {
					status = load(bundleResourceInput, archiveFile, directory, monitor);
				}
				stopCountdownTimer();

			}

			// walk the file system to see if the file did get downloaded
			manager.refresh(progress, false);
		}
		catch (OperationCanceledException e) {
			if (getTimerStatus().isOK()) {
				throw e;
			}
			else {
				status = new Status(IStatus.ERROR, ContentPlugin.PLUGIN_ID, 0, NLS.bind(
						"Download ''{0}'' (''{1}'') timed out", rootItem.getName(), rootItem.getId()), e);
			}
		}
		catch (CoreException e) {
			status = new Status(IStatus.ERROR, ContentPlugin.PLUGIN_ID, 0, NLS.bind(
					"Failed to determine dependencies of ''{0}'' (''{1}'')", rootItem.getName(), rootItem.getId()), e);
		}
		finally {
			progress.done();
		}
		return status;
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
	 * @return OK status if copying zip file and unzipping local copy of zip
	 * file are successful. Error otherwise
	 */
	protected IStatus load(InputStream in, File localArchiveFile, File directory, IProgressMonitor monitor) {

		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}

		SubMonitor progress = SubMonitor.convert(monitor, 100);

		directory.mkdirs();
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
			return new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, "I/O error while copying template zip file "
					+ localArchiveFile.getAbsolutePath() + " to " + directory.getAbsolutePath() + " ---- "
					+ e.getMessage(), e);

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
				return new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, "I/O error while copying template zip file "
						+ localArchiveFile.getAbsolutePath() + " to " + directory.getAbsolutePath() + " ---- "
						+ e.getMessage(), e);
			}
		}

		// Now that the zip file has been copied , unzip its contents
		try {
			URL fileUrl = localArchiveFile.toURI().toURL();
			ZipFileUtil.unzip(fileUrl, directory, progress.newChild(30));
			if (directory.listFiles().length <= 0) {
				String message = NLS.bind("Zip file {0} appears to be empty", localArchiveFile);
				return new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, message);
			}
		}
		catch (MalformedURLException e) {
			return new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID,
					"Unable to unzip template content due to malformed URL: " + e.getMessage(), e);
		}
		catch (IOException e) {
			return new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, "I/O error while unzipping template zip file "
					+ localArchiveFile.getAbsolutePath() + " into " + directory.getAbsolutePath() + " --- "
					+ e.getMessage(), e);
		}
		finally {
			// Once unzipped, the copied archive file is no longer needed
			localArchiveFile.delete();
		}

		return Status.OK_STATUS;

	}
}
