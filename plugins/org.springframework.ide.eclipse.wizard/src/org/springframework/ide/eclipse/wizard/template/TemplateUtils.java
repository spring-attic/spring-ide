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
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.mylyn.commons.core.CoreUtil;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.springframework.ide.eclipse.wizard.WizardPlugin;
import org.springframework.ide.eclipse.wizard.template.infrastructure.BundleTemplateLoader;
import org.springframework.ide.eclipse.wizard.template.infrastructure.ITemplateProjectData;
import org.springframework.ide.eclipse.wizard.template.infrastructure.RuntimeTemplateProjectData;
import org.springframework.ide.eclipse.wizard.template.infrastructure.Template;
import org.springframework.ide.eclipse.wizard.template.infrastructure.TemplateProjectData;
import org.springsource.ide.eclipse.commons.content.core.ContentItem;
import org.springsource.ide.eclipse.commons.content.core.ContentManager;
import org.springsource.ide.eclipse.commons.content.core.ContentPlugin;
import org.springsource.ide.eclipse.commons.content.core.TemplateDownloader;
import org.springsource.ide.eclipse.commons.content.core.util.IContentConstants;
import org.springsource.ide.eclipse.commons.ui.UiStatusHandler;

/**
 * @author Terry Denney
 * @author Kaitlin Duck Sherwood
 */
public class TemplateUtils {

	public static File importDirectory(ContentItem item, final Shell shell, boolean promptForDownload,
			SubMonitor monitor) throws CoreException, InterruptedException {
		Assert.isNotNull(item);
		String id = item.getId();
		ContentManager manager = ContentPlugin.getDefault().getManager();
		if (item.needsDownload()) {
			final ContentItem finalItem = item;
			final boolean[] response = { true };

			if (promptForDownload) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						response[0] = promptForDownload(shell, finalItem);
					}
				});
			}

			if (response[0]) {
				TemplateDownloader downloader = getTemplateDownloader(finalItem);
				IStatus status = downloader.downloadTemplate(monitor);
				if (!status.isOK()) {
					String message = NLS.bind("Download of template ''{0}'' failed: {1}", id, status.getMessage());
					throw new CoreException(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, message));
				}

				// re-get template project since it may changed
				item = manager.getItem(id);
				if (item == null) {
					return null;
				}
			}
			else if (!item.isLocal()) {
				return null;
			}
		}

		File baseDir = manager.getInstallDirectory();

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

	/**
	 * Determines if the given template has been downloaded.
	 * @param template
	 * @return true if downloaded, false otherwise
	 */
	public static boolean hasBeenDownloaded(Template template) {
		try {
			return template != null && template.getTemplateLocation() != null;
		}
		catch (CoreException e) {
			// Ignore as retrieving template location will occur at other times
			// where the error will matter.
		}
		return false;
	}

	private static TemplateDownloader getTemplateDownloader(ContentItem item) {
		ContentManager manager = ContentPlugin.getDefault().getManager();
		// Templates for simple projects are located in the bundle
		if (item.getId().equals(TemplateConstants.SIMPLE_JAVA_TEMPLATE_ID)
				|| item.getId().equals(TemplateConstants.SIMPLE_MAVEN_TEMPLATE_ID)) {
			return new BundleTemplateLoader(item, WizardPlugin.getDefault().getBundle());
		}
		else {
			return manager.createDownloader(item);
		}
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

	/**
	 * Given a template, download its contents and set the date in the given
	 * template. This method will always attempt to download the latest contents
	 * of the template, therefore this should only be called if the latest
	 * contents of the template need to be fetched.
	 */
	public static void downloadTemplateData(Template template, Shell shell) throws CoreException {

		if (template == null) {
			return;
		}

		final Template selectedTemplate = template;
		final Shell dialogueShell = shell;

		ContentItem selectedItem = selectedTemplate.getItem();
		if (!selectedItem.isLocal() && selectedItem.getRemoteDescriptor().getUrl() == null) {
			String message = NLS.bind("In the descriptor file for ''{0}'', the URL to the project ZIP is missing.",
					selectedItem.getName());
			MessageDialog.openError(dialogueShell, NLS.bind("URL missing", null), message);
			return;
		}

		// Otherwise download the data
		try {
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(dialogueShell);
			dialog.run(true, true, new IRunnableWithProgress() {

				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {

					try {
						monitor.beginTask("Download template " + selectedTemplate.getName(), 100);
						ITemplateProjectData data;
						if (selectedTemplate.getItem().isRuntimeDefined()) {
							data = new RuntimeTemplateProjectData(selectedTemplate.getItem().getRuntimeProject());
						}
						else {
							data = TemplateUtils.importTemplate(selectedTemplate, dialogueShell,
									new SubProgressMonitor(monitor, 1));
						}
						selectedTemplate.setTemplateData(data);
						if (data == null) {
							throw new InvocationTargetException(
									new CoreException(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, NLS.bind(
											"Template data missing. Please check the template "
													+ selectedTemplate.getName() + " to verify it has content.", null))));
						}
					}
					catch (CoreException e) {
						throw new InvocationTargetException(e);
					}
					catch (OperationCanceledException e) {
						throw new InterruptedException();
					}
					finally {
						monitor.done();
					}
				}
			});
		}
		catch (InterruptedException e) {
			// do nothing
		}
		catch (InvocationTargetException e) {
			Throwable target = e.getTargetException();
			if (target instanceof CoreException) {
				throw (CoreException) target;
			}
			else {
				throw new CoreException(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, e.getMessage(), e));
			}
		}

	}

	public static ITemplateProjectData importTemplate(Template template, final Shell shell,
			final IProgressMonitor monitor) throws CoreException, InterruptedException {
		SubMonitor progress = SubMonitor.convert(monitor, 100);

		// Do not prompt for download for Simple Templates, as they are bundled
		// in the plugin and
		// do not require download
		boolean shouldPromptForDownload = !(template instanceof SimpleProject);
		File templateDir = importDirectory(template.getItem(), shell, shouldPromptForDownload, progress);

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
