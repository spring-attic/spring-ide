/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.wizard.template;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.mylyn.commons.core.CoreUtil;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.template.infrastructure.ITemplateProjectData;
import org.springframework.ide.eclipse.wizard.template.infrastructure.Template;
import org.springframework.ide.eclipse.wizard.template.infrastructure.TemplateProjectData;
import org.springsource.ide.eclipse.commons.content.core.ContentItem;
import org.springsource.ide.eclipse.commons.content.core.ContentManager.DownloadJob;
import org.springsource.ide.eclipse.commons.content.core.ContentPlugin;
import org.springsource.ide.eclipse.commons.content.core.util.IContentConstants;
import org.springsource.ide.eclipse.commons.ui.UiStatusHandler;

/**
 * @author Terry Denney
 * @author Kaitlin Duck Sherwood
 */
public class TemplateUtils {

	public static File importDirectory(ContentItem item, final Shell shell, SubMonitor monitor) throws CoreException,
			InterruptedException {
		Assert.isNotNull(item);
		String id = item.getId();
		if (item.needsDownload()) {
			final ContentItem finalItem = item;
			final boolean[] response = new boolean[1];
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					response[0] = promptForDownload(shell, finalItem);
				}
			});
			if (response[0]) {
				DownloadJob job = ContentPlugin.getDefault().getManager().createDownloadJob(item);
				job.schedule();

				try {
					if (!job.getLatch().await(60, TimeUnit.SECONDS)) {
						job.cancel();
					}
				}
				catch (InterruptedException e) {
					job.cancel();
					throw e;
				}

				// re-get template project since it may changed
				item = ContentPlugin.getDefault().getManager().getItem(id);
				if (item == null) {
					return null;
				}
			}
			else if (!item.isLocal()) {
				return null;
			}
		}

		File baseDir = ContentPlugin.getDefault().getManager().getInstallDirectory();

		if (item.isRuntimeDefined()) {
			File directory = new File(baseDir, item.getPath());
			String projectName = item.getLocalDescriptor().getUrl();
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			File templateFolder = project.getLocation().toFile();
			copyFolder(templateFolder, directory);
		}

		if (!item.isLocal()) {
			String message = NLS.bind("Download of template ''{0}'' failed", id);
			throw new CoreException(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, message));
		}

		File projectDir = new File(baseDir, item.getPath());
		return projectDir;
	}

	private static void copyFolder(File source, File destination) {
		File[] sourceFiles = source.listFiles();
		for (File sourceFile : sourceFiles) {
			File destinationFile = new File(destination, sourceFile.getName());
			if (sourceFile.isDirectory()) {
				copyFolder(sourceFile, destinationFile);
			}
			else {
				FileOutputStream toStream = null;
				FileInputStream fromStream = null;
				try {
					fromStream = new FileInputStream(sourceFile);
					toStream = new FileOutputStream(destinationFile);
					byte[] buffer = new byte[4096];
					int bytesRead;

					while ((bytesRead = fromStream.read(buffer)) != -1) {
						toStream.write(buffer, 0, bytesRead); // write
					}
				}
				catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finally {
					if (fromStream != null) {
						try {
							fromStream.close();
						}
						catch (IOException e) {
						}
					}

					if (toStream != null) {
						try {
							toStream.close();
						}
						catch (IOException e) {
						}
					}
				}
			}
		}
	}

	public static ITemplateProjectData importTemplate(Template template, final Shell shell,
			final IProgressMonitor monitor) throws CoreException, InterruptedException {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		File templateDir = importDirectory(template.getItem(), shell, progress);

		if (templateDir == null) {
			return null;
		}

		if (!wasDownloadedViaDescriptor(templateDir)) {
			File[] childrenFiles = templateDir.listFiles();
			if (childrenFiles.length != 1) {
				String message = NLS
						.bind("There are more files the template download directory {0} than expected; using first directory",
								templateDir);
				IStatus status = new Status(IStatus.WARNING, WizardPlugin.PLUGIN_ID, message);
				UiStatusHandler.logAndDisplay(shell, status);
			}

			File subdir = null;
			for (File childFile : childrenFiles) {
				if (childFile.isDirectory()) {
					subdir = childFile;
					break;
				}
			}

			if (subdir != null) {
				File[] grandchildren = subdir.listFiles();
				for (File grandchild : grandchildren) {
					grandchild.renameTo(new File(templateDir, grandchild.getName()));
				}
				subdir.delete();
			}
			else {
				// There is another error thrown later on
				String message = NLS.bind(
						"There wasn't either {0} or a directory in the template download directory {1}",
						IContentConstants.TEMPLATE_DATA_FILE_NAME, templateDir);
				IStatus status = new Status(IStatus.WARNING, WizardPlugin.PLUGIN_ID, message);
				UiStatusHandler.logAndDisplay(shell, status);
			}

		}

		TemplateProjectData data = new TemplateProjectData(templateDir);
		data.read();
		progress.setWorkRemaining(10);

		return data;
	}

	// The layout of templates downloaded via descriptors.xml must
	// have template.xml in the root directory.
	private static boolean wasDownloadedViaDescriptor(File templateDir) {
		File templateXml = new File(templateDir, IContentConstants.TEMPLATE_DATA_FILE_NAME);
		return templateXml.exists();
	}

	protected static boolean promptForDownload(Shell shell, ContentItem item) {
		if (CoreUtil.TEST_MODE) {
			return true;
		}
		try {
			List<ContentItem> dependencies = ContentPlugin.getDefault().getManager().getDependencies(item);
			long size = 0;
			for (ContentItem dependency : dependencies) {
				size += dependency.getDownloadSize();
			}
			String formattedSize;
			if (size > 0) {
				formattedSize = NLS.bind("{0} bytes", size);
			}
			else {
				formattedSize = NLS.bind("unknown size", null);
			}

			String message;
			String requiredBundle = null;
			if (item.getLocalDescriptor() != null) {
				requiredBundle = item.getLocalDescriptor().getRequiresBundle();
			}
			else if (item.getRemoteDescriptor() != null) {
				requiredBundle = item.getRemoteDescriptor().getRequiresBundle();
			}

			if (requiredBundle != null && !hasBundle(requiredBundle)) {
				message = NLS
						.bind("Warning: this project requires the bundle \n\t{0}\nwhich is not installed.  You can download this template, but it will probably get build errors.\n\n",
								requiredBundle);
			}
			else {
				message = "";
			}

			if (!item.isLocal()) {
				message = message
						+ NLS.bind("{0} requires a download of {1}.\n\nProceed?", item.getName(), formattedSize);
				return MessageDialog.openQuestion(shell, "Import", message);
			}
			else if (item.isNewerVersionAvailable()) {
				message = NLS.bind("An update for {0} is available which requires a download of {1}.\n\n" + message
						+ "Update?", item.getName(), formattedSize);
				return MessageDialog.openQuestion(shell, "Import", message);
			}
		}
		catch (CoreException e) {
			UiStatusHandler.logAndDisplay(shell, e.getStatus());
			return false;
		}
		return true;
	}

	private static boolean hasBundle(String requiredBundle) {
		return (Platform.getBundle(requiredBundle) != null);
	}
}
