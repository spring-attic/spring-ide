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
package org.springframework.roo.shell.eclipse;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

/**
 * Command implementation that refreshes a project resource tree.
 * @author Christian Dupuis
 * @since 2.1.0
 */
public class ProjectRefresher {

	// private static final String TEST_FOLDER_PATH_SEGMENT = File.separator +
	// "test" + File.separator;

	private final IProject project;

	private String projectDirectoryPath;

	public ProjectRefresher(IProject project) {
		this.project = project;
		if (project != null) {
			URI uri = SpringCoreUtils.getResourceURI(project);
			if (uri != null) {
				try {
					this.projectDirectoryPath = new File(uri).getCanonicalPath();
				}
				catch (IOException e) {
				}
			}
		}
	}

	public void refresh(final File file, final Boolean isNewFile) {
		Job job = new Job("refresh project") {

			@Override
			protected IStatus run(IProgressMonitor monitor) {

				if (projectDirectoryPath != null && file != null) {
					try {
						String path = file.getCanonicalPath();

						if (projectDirectoryPath.equals(path) || file.getName().endsWith(".log")
								|| file.getName().endsWith(".roo")) {
							return Status.OK_STATUS;
						}
						project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
					}
					catch (Exception e) {
						// We ignore this here
					}
				}
				return Status.OK_STATUS;
			}

		};
		job.setSystem(true);
		job.setRule(ResourcesPlugin.getWorkspace().getRuleFactory().buildRule());
		job.setPriority(Job.INTERACTIVE);
		job.schedule();

	}

	// private boolean shouldOpenFile(File file) {
	// if (file != null && file.exists() && file.isFile() &&
	// file.getName().endsWith(".java")) {
	// return !file.getAbsolutePath().contains(TEST_FOLDER_PATH_SEGMENT);
	// }
	// return false;
	// }

}
