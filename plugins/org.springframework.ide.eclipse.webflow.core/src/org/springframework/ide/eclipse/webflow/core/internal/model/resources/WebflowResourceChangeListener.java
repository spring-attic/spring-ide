/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.internal.model.resources;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.springframework.ide.eclipse.core.SpringCoreUtils;
import org.springframework.ide.eclipse.core.internal.model.resources.SpringResourceChangeListener;
import org.springframework.ide.eclipse.webflow.core.internal.model.WebflowModelUtils;
import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;

/**
 * Implementation of {@link IResourceChangeListener} which detects modifications
 * to Spring projects (add/remove Spring beans nature, open/close and delete)
 * and Spring beans configurations (change and delete).
 * <p>
 * An implementation of {@link IWebflowResourceChangeEvents} has to be provided.
 * Here are callbacks defined for the different events.
 * @author Christian Dupuis
 * @since 2.0.1
 */
public class WebflowResourceChangeListener extends SpringResourceChangeListener {

	private IWebflowResourceChangeEvents events;

	public WebflowResourceChangeListener(IWebflowResourceChangeEvents events) {
		super(events);
		this.events = events;
	}

	@Override
	protected IResourceDeltaVisitor getVisitor(int eventType) {
		return new WebflowResourceVisitor(eventType);
	}

	/**
	 * Internal resource delta visitor.
	 */
	protected class WebflowResourceVisitor extends SpringResourceVisitor {

		public WebflowResourceVisitor(int eventType) {
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
				} else if (WebflowModelUtils.isWebflowConfig(file)) {
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
					} else if (WebflowModelUtils.isWebflowConfig(file)) {
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
				if (WebflowModelUtils.isWebflowConfig(resource)) {
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
					&& resource.getFullPath().segmentCount() == 2
					&& resource.getName()
							.equals(IWebflowProject.DESCRIPTION_FILE);
		}
	}
}
