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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
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
import org.springsource.ide.eclipse.commons.content.core.ContentPlugin;
import org.springsource.ide.eclipse.commons.content.core.ContentManager.DownloadJob;
import org.springsource.ide.eclipse.commons.ui.UiStatusHandler;


/**
 * @author Terry Denney
 */
public class TemplateUtils {

	public static File importDirectory(ContentItem item, final Shell shell, SubMonitor monitor) throws CoreException {
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
				job.run(monitor.newChild(80));

				// re-get sample project since it may changed
				item = ContentPlugin.getDefault().getManager().getItem(id);
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

		if (item == null || !item.isLocal()) {
			throw new CoreException(new Status(IStatus.ERROR, WizardPlugin.PLUGIN_ID, NLS.bind(
					"Download of template ''{0}'' failed", id)));
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
			final IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		File templateDir = importDirectory(template.getItem(), shell, progress);

		if (templateDir == null) {
			return null;
		}

		TemplateProjectData data = new TemplateProjectData(templateDir);
		data.read();
		progress.setWorkRemaining(10);

		return data;
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
			String formattedSize = NLS.bind("{0} bytes", size);
			if (!item.isLocal()) {
				return MessageDialog.openQuestion(shell, "Import",
						NLS.bind("{0} requires a download of {1}. Proceed?", item.getName(), formattedSize));
			}
			else if (item.isNewerVersionAvailable()) {
				return MessageDialog.openQuestion(
						shell,
						"Import",
						NLS.bind("An update for {0} is available which requires a download ({1}). Update?",
								item.getName(), formattedSize));
			}
		}
		catch (CoreException e) {
			UiStatusHandler.logAndDisplay(shell, e.getStatus());
			return false;
		}
		return true;
	}

}
