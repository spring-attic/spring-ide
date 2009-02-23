/*******************************************************************************
 * Copyright (c) 2005, 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.internal.model.resources;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.springframework.ide.eclipse.core.SpringCore;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

/**
 * Implementation of {@link IResourceChangeListener} which detects modifications
 * to Spring projects (add/remove Spring beans nature, open/close and delete).
 * <p>
 * An implementation of {@link ISpringResourceChangeEvents} has to be provided.
 * Here are callbacks defined for the different events.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public class SpringResourceChangeListener implements IResourceChangeListener {

	public static final int LISTENER_FLAGS = IResourceChangeEvent.PRE_CLOSE
			| IResourceChangeEvent.PRE_DELETE
			| IResourceChangeEvent.PRE_BUILD
			| IResourceChangeEvent.POST_BUILD 
			| IResourceChangeEvent.PRE_REFRESH;

	private static final int VISITOR_FLAGS = IResourceDelta.ADDED
			| IResourceDelta.CHANGED | IResourceDelta.REMOVED;

	private ISpringResourceChangeEvents events;

	public SpringResourceChangeListener(ISpringResourceChangeEvents events) {
		this.events = events;
	}

	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getSource() instanceof IWorkspace) {
			int eventType = event.getType();
			switch (eventType) {
			case IResourceChangeEvent.PRE_CLOSE:
				IProject closedProject = (IProject) event.getResource();
				if (SpringCoreUtils.isSpringProject(closedProject)) {
					events.projectClosed(closedProject, eventType);
				}
				break;

			case IResourceChangeEvent.PRE_DELETE:
				IProject openedProject = (IProject) event.getResource();
				if (SpringCoreUtils.isSpringProject(openedProject)) {
					events.projectDeleted(openedProject, eventType);
				}
				break;

			case IResourceChangeEvent.PRE_BUILD:
			case IResourceChangeEvent.POST_BUILD:
			case IResourceChangeEvent.PRE_REFRESH:
				IResourceDelta delta = event.getDelta();
				if (delta != null) {
					try {
						delta.accept(getVisitor(eventType), VISITOR_FLAGS);
					} catch (CoreException e) {
						SpringCore.log("Error while traversing "
								+ "resource change delta", e);
					}
				}
				break;
			}
		}
		else if (event.getSource() instanceof IProject) {
			int eventType = event.getType();
			switch (eventType) {
			case IResourceChangeEvent.PRE_CLOSE:
				IProject closedProject = (IProject) event.getSource();
				if (SpringCoreUtils.isSpringProject(closedProject)) {
					events.projectClosed(closedProject, eventType);
				}
				break;

			case IResourceChangeEvent.PRE_DELETE:
				IProject openedProject = (IProject) event.getSource();
				if (SpringCoreUtils.isSpringProject(openedProject)) {
					events.projectDeleted(openedProject, eventType);
				}
				break;

			case IResourceChangeEvent.PRE_BUILD:
			case IResourceChangeEvent.POST_BUILD:
				IResourceDelta delta = event.getDelta();
				if (delta != null) {
					try {
						delta.accept(getVisitor(eventType), VISITOR_FLAGS);
					} catch (CoreException e) {
						SpringCore.log("Error while traversing "
								+ "resource change delta", e);
					}
				}
				break;
			}
		}

	}

	protected IResourceDeltaVisitor getVisitor(int eventType) {
		return new SpringResourceVisitor(eventType);
	}

	/**
	 * Internal resource delta visitor.
	 */
	protected class SpringResourceVisitor implements IResourceDeltaVisitor {

		protected int eventType;

		public SpringResourceVisitor(int eventType) {
			this.eventType = eventType;
		}

		public final boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				return resourceAdded(resource);

			case IResourceDelta.OPEN:
				return resourceOpened(resource);

			case IResourceDelta.CHANGED:
				return resourceChanged(resource, delta.getFlags());

			case IResourceDelta.REMOVED:
				return resourceRemoved(resource);
			}
			return true;
		}

		protected boolean resourceAdded(IResource resource) {
			if (resource instanceof IProject) {
				if (SpringCoreUtils.isSpringProject(resource)) {
					events.projectAdded((IProject) resource, eventType);
				}
				return false;
			}
			return true;
		}

		protected boolean resourceOpened(IResource resource) {
			if (resource instanceof IProject) {
				if (SpringCoreUtils.isSpringProject(resource)) {
					events.projectOpened((IProject) resource, eventType);
				}
				return false;
			}
			return true;
		}

		protected boolean resourceChanged(IResource resource, int flags) {
			if (resource instanceof IProject) {
				if ((flags & IResourceDelta.OPEN) != 0) {
					if (SpringCoreUtils.isSpringProject(resource)) {
						events.projectOpened((IProject) resource,
								eventType);
					}
					return false;
				} else if ((flags & IResourceDelta.DESCRIPTION) != 0) {
					IProject project = (IProject) resource;
					if (SpringCoreUtils.isSpringProject(project)) {
						if (!events.isSpringProject((IProject) resource,
								eventType)) {
							events.springNatureAdded(project, eventType);
						}
					} else if (events.isSpringProject(project, eventType)) {
						events.springNatureRemoved(project, eventType);
					}
					return false;
				}
			}
			return true;
		}

		protected boolean resourceRemoved(IResource resource) {
			return true;
		}
	}
}
