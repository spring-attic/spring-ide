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
package org.springframework.ide.eclipse.beans.core.internal.model.resources;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.internal.model.resources.SpringResourceChangeListener;

/**
 * Implementation of {@link IResourceChangeListener} which detects modifications
 * to Spring projects (add/remove Spring beans nature, open/close and delete)
 * and Spring beans configurations (change and delete).
 * <p>
 * An implementation of {@link IBeansResourceChangeEvents} has to be provided.
 * Here are callbacks defined for the different events.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class BeansResourceChangeListener extends SpringResourceChangeListener {

	private IBeansResourceChangeEvents events;

	public BeansResourceChangeListener(IBeansResourceChangeEvents events) {
		super(events);
		this.events = events;
	}

	@Override
	protected IResourceDeltaVisitor getVisitor(int eventType) {
		return new BeansResourceVisitor(eventType);
	}

	/**
	 * Internal resource delta visitor.
	 */
	protected class BeansResourceVisitor extends SpringResourceVisitor {

		public BeansResourceVisitor(int eventType) {
			super(eventType);
		}

		@Override
		protected boolean resourceAdded(IResource resource) {
			if (resource instanceof IFile) {
				IFile file = (IFile) resource;
				if (isProjectDescriptionFile(file)) {
					if (SpringCoreUtils.isSpringProject(file)) {
						events.projectDescriptionChanged(file, eventType);
					}
				}
				else if (BeansCoreUtils.isBeansConfig(file)) {
					events.configAdded(file, eventType);
				}
				return false;
			}
			return super.resourceAdded(resource);
		}

		@Override
		protected boolean resourceChanged(IResource resource, int flags) {
			if (resource instanceof IFile) {
				if ((flags & IResourceDelta.CONTENT) != 0) {
					IFile file = (IFile) resource;
					if (isProjectDescriptionFile(file)) {
						if (SpringCoreUtils.isSpringProject(file)) {
							events.projectDescriptionChanged(file, eventType);
						}
					}
					else if (BeansCoreUtils.isBeansConfig(file, true)) {
						events.configChanged(file, eventType);
					}
				}
				return false;
			}
			return super.resourceChanged(resource, flags);
		}

		@Override
		protected boolean resourceRemoved(IResource resource) {
			if (resource instanceof IFile) {
				if (BeansCoreUtils.isBeansConfig(resource)) {
					events.configRemoved((IFile) resource, eventType);
				}
				return false;
			}
			return super.resourceRemoved(resource);
		}

		private boolean isProjectDescriptionFile(IResource resource) {
			return resource != null
					&& resource.isAccessible()
					&& resource.getType() == IResource.FILE
					&& ((resource.getFullPath().segmentCount() == 2 && resource.getName().equals(
							IBeansProject.DESCRIPTION_FILE)) || SpringCoreUtils.isManifest(resource));
		}
	}
}
