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

package org.springframework.ide.eclipse.core;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;

public class SpringCoreUtils {

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
				if (project.hasNature(nature)) {

					// first remove problem markers from Spring beans project
					if (nature.equals(SpringCore.NATURE_ID)) {
// TODO find a way to remove all problem markers which are inherited from core's
// problem marker
//						SpringProject proj = (SpringProject)
//								 CorePlugin.getModel().getProject(project);
//						if (proj != null) {
//							proj.deleteProblemMarkers();
//						}
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
	 * Returns true if given project has an Spring project nature.
	 */
	public static boolean isSpringProject(IProject project) {
		if (project != null && project.isAccessible()) {
			try {
				return project.hasNature(SpringCore.NATURE_ID);
			} catch (CoreException e) {
				SpringCore.log(e);
			}
		}
		return false;
	}

//	public static void deleteProblemMarkers(IFile file) {
//		if (file != null && file.isAccessible()) {
//			try {
//				file.deleteMarkers(IBeansProjectMarker.PROBLEM_MARKER, false,
//								   IResource.DEPTH_ZERO);
//			} catch (CoreException e) {
//				CorePlugin.log(e);
//			}
//		}
//	}
}
