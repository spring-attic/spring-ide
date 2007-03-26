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
package org.springframework.ide.eclipse.beans.core.internal.model.resources;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;
import org.springframework.ide.eclipse.core.SpringCoreUtils;

/**
 * Implementation of {@link IResourceChangeListener} which detects modifications
 * to Spring projects (add/remove Spring beans nature, open/close and delete)
 * and Spring beans configurations (change and delete).
 * <p>
 * An implementation of {@link IBeansResourceChangeEvents} has to be provided.
 * Here are callbacks defined for the different events.
 * 
 * @author Torsten Juergeleit
 */
public class BeansResourceChangeListener implements IResourceChangeListener {

	public static final int LISTENER_FLAGS = IResourceChangeEvent.PRE_CLOSE
			| IResourceChangeEvent.PRE_DELETE
			| IResourceChangeEvent.PRE_BUILD
			| IResourceChangeEvent.POST_BUILD;

	private static final int VISITOR_FLAGS = IResourceDelta.ADDED
			| IResourceDelta.CHANGED | IResourceDelta.REMOVED;

	private IBeansResourceChangeEvents events;

	public BeansResourceChangeListener(IBeansResourceChangeEvents events) {
		this.events = events;
	}

	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getSource() instanceof IWorkspace) {
			IProject project = (IProject) event.getResource();
			IResourceDelta delta = event.getDelta();
			int eventType = event.getType();
			switch (eventType) {
			case IResourceChangeEvent.PRE_CLOSE:
				if (SpringCoreUtils.isSpringProject(project)) {
					events.projectClosed(project, eventType);
				}
				break;

			case IResourceChangeEvent.PRE_DELETE:
				if (SpringCoreUtils.isSpringProject(project)) {
					events.projectDeleted(project, eventType);
				}
				break;

			case IResourceChangeEvent.PRE_BUILD:
			case IResourceChangeEvent.POST_BUILD:
				if (delta != null) {
					try {
						delta.accept(new BeansProjectVisitor(eventType),
								VISITOR_FLAGS);
					} catch (CoreException e) {
						BeansCorePlugin.log("Error while traversing "
								+ "resource change delta", e);
					}
				}
				break;
			}
		}
	}
	
	/**
	 * Internal resource delta visitor.
	 */
	private class BeansProjectVisitor implements IResourceDeltaVisitor {

		private int eventType;

		public BeansProjectVisitor(int eventType) {
			this.eventType = eventType;
		}

		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				if (resource instanceof IProject) {
					if (SpringCoreUtils.isSpringProject(resource)) {
						events.projectAdded((IProject) resource, eventType);
					}
					return false;
				} else if (resource instanceof IFile) {
					IFile file = (IFile) resource;
					if (isProjectDescriptionFile(file)) {
						events.projectDescriptionChanged(file, eventType);
					} else if (BeansCoreUtils.isBeansConfig(file)) {
						events.configAdded(file, eventType);
					}
					return false;
				}
				break;

			case IResourceDelta.OPEN:
				if (resource instanceof IProject) {
					if (SpringCoreUtils.isSpringProject(resource)) {
						events.projectOpened((IProject) resource, eventType);
					}
					return false;
				}
				break;

			case IResourceDelta.CHANGED:
				int flags = delta.getFlags();
				if (resource instanceof IFile) {
					if ((flags & IResourceDelta.CONTENT) != 0) {
						IFile file = (IFile) resource;
						if (isProjectDescriptionFile(file)) {
							events.projectDescriptionChanged(file, eventType);
						} else if (BeansCoreUtils.isBeansConfig(file)) {
							events.configChanged(file, eventType);
						} else {
							visitChangedFile(file);
						}
					}
					return false;
				} else if (resource instanceof IProject) {
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
				break;

			case IResourceDelta.REMOVED:
				if (resource instanceof IFile) {
					if (BeansCoreUtils.isBeansConfig(resource)) {
						events.configRemoved((IFile) resource, eventType);
					}
					return false;
				}
				break;
			}
			return true;
		}

		private boolean isProjectDescriptionFile(IResource resource) {
			return resource != null
					&& resource.isAccessible()
					&& resource.getType() == IResource.FILE
					&& resource.getFullPath().segmentCount() == 2
					&& resource.getName()
							.equals(IBeansProject.DESCRIPTION_FILE);
		}

		private void visitChangedFile(IFile file) {
			String ext = file.getFileExtension();
			if (ext != null && "java".equals(ext)) {
				ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
				if (cu != null && cu.exists()) {
					try {
						IBeansModel model = BeansCorePlugin.getModel();
						for (IType type : cu.getTypes()) {
							String className = type.getFullyQualifiedName();
							Set<IBeansConfig> configs = model
									.getConfigs(className);
							if (!configs.isEmpty()) {
								events.beanClassChanged(className, configs,
										eventType);
							}
						}
					} catch (JavaModelException e) {
						BeansCorePlugin.log(e);
					}
				}
			}
		}
	}
}
