/*
 * Copyright 2002-2004 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.beans.core.internal.model.resources;

import java.util.Collection;

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
import org.springframework.ide.eclipse.beans.core.model.IBeansModel;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

/**
 * Implementation of <code>IResourceChangeListener</code> which detects
 * modifications to Spring projects (add/remove Spring beans nature, open/close
 * and delete) and Spring beans configurations (change and delete).
 * <p>
 * An implementation of <code>IBeansResourceChangeEvents</code> has to be
 * provided. Here are callbacks defined for the different events. 
 * 
 * @see IBeansResourceChangeEvents
 */
public class BeansResourceChangeListener implements IResourceChangeListener {

	public static final int LISTENER_FLAGS = IResourceChangeEvent.PRE_CLOSE |
										   IResourceChangeEvent.PRE_DELETE |
										   IResourceChangeEvent.POST_AUTO_BUILD;
	private static final int VISITOR_FLAGS = IResourceDelta.ADDED |
											 IResourceDelta.CHANGED |
											 IResourceDelta.REMOVED;
	private IBeansResourceChangeEvents events;

	public BeansResourceChangeListener(IBeansResourceChangeEvents events) {
		this.events = events;
	}

	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getSource() instanceof IWorkspace) {
			IProject project = (IProject) event.getResource();
			IResourceDelta delta = event.getDelta();
			switch (event.getType()) {
				case IResourceChangeEvent.PRE_CLOSE :
					if (BeansCoreUtils.isBeansProject(project)) {
						events.projectClosed(project);
					}
					break;

				case IResourceChangeEvent.PRE_DELETE :
					if (BeansCoreUtils.isBeansProject(project)) {
						events.projectDeleted(project);
					}
					break;

				case IResourceChangeEvent.POST_AUTO_BUILD :
					if (delta != null) {
						try {
							delta.accept(new BeansProjectVisitor(),
										 VISITOR_FLAGS);
						} catch (CoreException e) {
							BeansCorePlugin.log("Error while traversing " +
												"resource change delta", e);
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

		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
				case IResourceDelta.ADDED :
					if (resource instanceof IProject) {
						if (BeansCoreUtils.isBeansProject((IProject)
														  resource)) {
							events.projectAdded((IProject) resource);
						}
						return false;
					} else if (resource instanceof IFile) {
						IFile file = (IFile) resource;
						if (isProjectDescriptionFile(file)) {
							events.projectDescriptionChanged(file);
						} else if (BeansCoreUtils.isBeansConfig(file)) {
							events.configAdded(file);
						}
						return false;
					}
					break;

				case IResourceDelta.OPEN :
					if (resource instanceof IProject) {
						IProject project = (IProject) resource; 
						if (BeansCoreUtils.isBeansProject(project)) {
							events.projectOpened(project);
						}
						return false;
					}
					break;

				case IResourceDelta.CHANGED :
					int flags = delta.getFlags();
					if (resource instanceof IFile) {
						if ((flags & IResourceDelta.CONTENT) != 0) {
							IFile file = (IFile) resource;
							if (isProjectDescriptionFile(resource)) {
								events.projectDescriptionChanged(file);
							} else if (BeansCoreUtils.isBeansConfig(file)) {
								events.configChanged(file);
							} else {
								visitChangedFile(file);
							}
						}
						return false;
					} else if (resource instanceof IProject) {
						if ((flags & IResourceDelta.OPEN) != 0) {
							IProject project = (IProject) resource; 
							if (BeansCoreUtils.isBeansProject(project)) {
								events.projectOpened(project);
							}
							return false;
						} else if ((flags & IResourceDelta.DESCRIPTION) != 0) {
							IProject project = (IProject) resource;
							if (BeansCoreUtils.isBeansProject(project)) {
								if (!events.isSpringProject(project)) {
									events.springNatureAdded(project);
								}
							} else if (events.isSpringProject(project)) {
								events.springNatureRemoved(project);
							}
							return false;
						}
					}
					break;

				case IResourceDelta.REMOVED :
					if (resource instanceof IFile) {
						if (BeansCoreUtils.isBeansConfig(resource)) {
							events.configRemoved((IFile) resource);
						}
						return false;
					}
					break;
			}
			return true;
		}

		private boolean isProjectDescriptionFile(IResource resource) {
			return resource != null && resource.isAccessible() &&
				   resource.getType() == IResource.FILE && 
				   resource.getFullPath().segmentCount() == 2 &&
				   resource.getName().equals(IBeansProject.DESCRIPTION_FILE);
		}

		private void visitChangedFile(IFile file) {
			String ext = file.getFileExtension();
			if (ext != null && "java".equals(ext)) {
				ICompilationUnit cu = JavaCore.createCompilationUnitFrom(file);
				if (cu != null && cu.exists()) {
					try {
						IBeansModel model = BeansCorePlugin.getModel();
						IType[] types = cu.getTypes();
						for (int i= 0; i < types.length; i++) {
							String className = types[i].getFullyQualifiedName();
							Collection configs = model.getConfigs(className);
							if (!configs.isEmpty()) {
								events.beanClassChanged(className, configs);
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
