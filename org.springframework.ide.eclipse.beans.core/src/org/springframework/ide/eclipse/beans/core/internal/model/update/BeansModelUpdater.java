/*******************************************************************************
 * Copyright (c) 2005, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.update;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.beans.core.model.update.IBeansModelUpdate;

/**
 * Utility class that applies {@link IBeansModelUpdate} implementations to the
 * {@link IBeansModel}.
 * <p>
 * Note: Spring IDE does not expose an extension point for contributing model
 * updates.
 * @author Christian Dupuis
 * @since 2.0.3
 */
public abstract class BeansModelUpdater {

	private static final List<IBeansModelUpdate> UPDATES;

	static {
		UPDATES = new ArrayList<IBeansModelUpdate>();
		UPDATES.add(new UpdateFor203());
	}

	public static void updateModel(Collection<IBeansProject> projects) {
		for (IBeansModelUpdate update : UPDATES) {
			for (IBeansProject project : projects) {
				if (update.requiresUpdate(project)) {
					UpdateJob job = new UpdateJob(project, update);
					job.setPriority(Job.BUILD);
					job.setRule(ResourcesPlugin.getWorkspace().getRuleFactory()
							.buildRule());
					job.schedule();
				}
			}
		}
	}

	private static class UpdateJob extends Job {

		private final IBeansProject project;

		private final IBeansModelUpdate update;

		public UpdateJob(IBeansProject project, IBeansModelUpdate update) {
			super("Updating Spring Project '" + project.getElementName()
					+ "' with update '" + update.getName() + "'");
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
