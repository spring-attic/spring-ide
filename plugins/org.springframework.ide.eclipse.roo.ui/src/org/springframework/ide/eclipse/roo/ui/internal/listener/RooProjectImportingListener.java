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
package org.springframework.ide.eclipse.roo.ui.internal.listener;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.springframework.ide.eclipse.roo.core.RooCoreActivator;
import org.springframework.ide.eclipse.roo.core.internal.RooNature;


/**
 * An {@link IResourceChangeListener} that listens for new roo projects being
 * imported into the workspace. This will ensure that the -outxml preference is
 * added to the project.
 * @author Andrew Eisenberg
 * @author Christian Dupuis
 * @since 2.5.1
 */
public class RooProjectImportingListener implements IResourceChangeListener {

	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
			// use instanceof test instead of getType() to protect against null
			if (event.getDelta().getResource() instanceof IWorkspaceRoot) {
				IResourceDelta[] projectDeltas = event.getDelta().getAffectedChildren(IResourceDelta.ADDED);
				for (IResourceDelta childDelta : projectDeltas) {
					if (childDelta.getResource() instanceof IProject) {
						final IProject project = (IProject) childDelta.getResource();
						try {
							if (project.isAccessible() && project.hasNature(RooNature.NATURE_ID)) {
								// can't call this directly since at this point,
								// the
								// resource tree is locked for changes,
								// so run as a job
								Job job = new Job("Configure new Roo project") {

									@Override
									protected IStatus run(IProgressMonitor monitor) {
										try {
											project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
										}
										catch (CoreException e) {
											RooCoreActivator.log(e);
										}
										RooNature.addOutxmlOption(project);
										return Status.OK_STATUS;
									}
								};
								job.setUser(false);
								// set priority to build so that we are sure all
								// other initialization work is completed before
								// we do this.
								job.setPriority(Job.BUILD);
								job.schedule();
							}
						}
						catch (CoreException e) {
							RooCoreActivator.log(e);
						}
					}
				}
			}
		}
	}
	
}
