/*******************************************************************************
 * Copyright (c) 2008, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.internal.model.update;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowModel;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;
import org.springframework.ide.eclipse.webflow.core.model.update.IWebflowModelUpdate;

/**
 * Utility class that applies {@link IWebflowModelUpdate} implementations to the {@link IWebflowModel}.
 * <p>
 * Note: Spring IDE does not expose an extension point for contributing model updates.
 * @author Christian Dupuis
 * @since 2.5.2
 */
public abstract class WebflowModelUpdater {

	private static final List<IWebflowModelUpdate> UPDATES;

	static {
		UPDATES = new ArrayList<IWebflowModelUpdate>();
//		UPDATES.add(new UpdateFor252());
	}

	/**
	 * Updates the complete list of {@link IWebflowProject}.
	 */
	public static void updateModel(Collection<IWebflowProject> projects) {
		for (IWebflowProject project : projects) {
			updateProject(project);
		}
	}

	/**
	 * Updates a single {@link IBeansProject}.
	 */
	public static void updateProject(IWebflowProject project) {
		for (IWebflowModelUpdate update : UPDATES) {
			// Do dummy access to the model object to load the model
			project.getConfigs();
			if (update.requiresUpdate(project)) {
				UpdateJob job = new UpdateJob(project, update);
				job.setPriority(Job.BUILD);
				job.setRule(project.getProject());
				job.schedule();
			}
		}
	}

	private static class UpdateJob extends Job {

		private final IWebflowProject project;

		private final IWebflowModelUpdate update;

		public UpdateJob(IWebflowProject project, IWebflowModelUpdate update) {
			super("Updating Spring Project '" + project.getElementName() + "' with update '" + update.getName() + "'");
			this.project = project;
			this.update = update;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			if (monitor.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			update.updateProject(project);
			monitor.done();
			return Status.OK_STATUS;
		}
	}

}
