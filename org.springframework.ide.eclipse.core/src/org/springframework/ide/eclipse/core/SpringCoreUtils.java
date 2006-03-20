/*
 * Copyright 2002-2006 the original author or authors.
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

package org.springframework.ide.eclipse.core;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaCore;
import org.springframework.ide.eclipse.core.model.IModelElement;

public final class SpringCoreUtils {

	/**
	 * Creates given project at specified location.
	 */
	public static void createProject(IProject project, IPath location,
							   IProgressMonitor monitor) throws CoreException {
		if (!Platform.getLocation().equals(location)) {
			IProjectDescription desc = project.getWorkspace()
									 .newProjectDescription(project.getName());
			desc.setLocation(location);
			project.create(desc, monitor);
		} else {
			project.create(monitor);
		}
	}

	/**
	 * Creates given folder and (if necessary) all of it's parents.
	 */
	public static void createFolder(IFolder folder) throws CoreException {
		if (!folder.exists()) {
			IContainer parent = folder.getParent();
			if (parent instanceof IFolder) {
				createFolder((IFolder) parent);
			}
			folder.create(true, true, null);
		}
	}

	/**
	 * Adds given nature as first nature to specified project.
	 */	
	public static void addProjectNature(IProject project, String nature) {
		if (project != null && nature != null) {
			try {
				if (!project.hasNature(nature)) {
					IProjectDescription desc = project.getDescription();
					String[] oldNatures = desc.getNatureIds();
					String[] newNatures = new String[oldNatures.length + 1];
					newNatures[0] = nature;
					if (oldNatures.length > 0) {
						System.arraycopy(oldNatures, 0, newNatures, 1,
										 oldNatures.length);
					}
					desc.setNatureIds(newNatures);
					project.setDescription(desc, null);
				}
			} catch (CoreException e) {
				SpringCore.log(e);
			}
		}
	}

	/**
	 * Removes given nature from specified project.
	 */	
	public static void removeProjectNature(IProject project, String nature) {
		if (project != null && nature != null) {
			try {
				if (project.exists() && project.hasNature(nature)) {

					// first remove all problem markers (including the
					// inherited ones) from Spring beans project
					if (nature.equals(SpringCore.NATURE_ID)) {
						project.deleteMarkers(SpringCore.MARKER_ID, true,
											  IResource.DEPTH_INFINITE);
					}

					// now remove project nature
					IProjectDescription desc = project.getDescription();
					String[] oldNatures = desc.getNatureIds();
					String[] newNatures = new String[oldNatures.length - 1];
					int newIndex = oldNatures.length - 2;
					for (int i =  oldNatures.length - 1; i >= 0; i--) {
						if (!oldNatures[i].equals(nature)) {
							newNatures[newIndex--] = oldNatures[i];
						}
					}
					desc.setNatureIds(newNatures);
					project.setDescription(desc, null);
				}
			} catch (CoreException e) {
				SpringCore.log(e);
			}
		} 
	}

	/**
	 * Removes given builder from specified project.
	 */
	public static void removeProjectBuilder(IProject project, String builder) {
		if (project != null && builder != null) {
			try {
				IProjectDescription desc = project.getDescription();
				ICommand[] commands = desc.getBuildSpec();
				for (int i = commands.length - 1; i >= 0; i--) {
					if (commands[i].getBuilderName().equals(builder)) {
						ICommand[] newCommands = new ICommand[commands.length -
															  1];
						System.arraycopy(commands, 0, newCommands, 0, i);
						System.arraycopy(commands, i + 1, newCommands, i,
										 commands.length - i - 1);
						// Commit the spec change into the project
						desc.setBuildSpec(newCommands);
						project.setDescription(desc, null);
						break;
					}
				}
			} catch (CoreException e) {
				SpringCore.log(e);
			}
		}
	}

	/**
	 * Returns true if given resource is a Java project.
	 */
	public static boolean isJavaProject(IResource resource) {
		if (resource instanceof IProject && resource.isAccessible()) {
			try {
				return ((IProject) resource).hasNature(JavaCore.NATURE_ID);
			} catch (CoreException e) {
				SpringCore.log(e);
			}
		}
		return false;
	}

	/**
	 * Returns true if given resource is a Spring project.
	 */
	public static boolean isSpringProject(IResource resource) {
		if (resource instanceof IProject && resource.isAccessible()) {
			try {
				return ((IProject) resource).hasNature(SpringCore.NATURE_ID);
			} catch (CoreException e) {
				SpringCore.log(e);
			}
		}
		return false;
	}

	/**
	 * Removes all Spring project problem markers from given resource.
	 */
	public static void deleteProblemMarkers(IResource resource) {
		if (resource != null && resource.isAccessible()) {
			try {
				resource.deleteMarkers(ISpringProjectMarker.PROBLEM_MARKER,
									   false, IResource.DEPTH_ZERO);
			} catch (CoreException e) {
				SpringCore.log(e);
			}
		}
	}

	/**
	 * Trys to adapt given element to <code>IModelElement</code>. 
	 */
	public static Object adaptToModelElement(Object element) {
		if (!(element instanceof IModelElement) &&
											 (element instanceof IAdaptable)) {
			return ((IAdaptable) element).getAdapter(IModelElement.class);
		}
		return element;
	}
}
