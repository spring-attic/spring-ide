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

package org.springframework.ide.eclipse.beans.core.internal.project;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.springframework.ide.eclipse.beans.core.BeansCorePlugin;
import org.springframework.ide.eclipse.beans.core.BeansCoreUtils;

public abstract class BeansProjectNature implements IProjectNature {

    public static final String NATURE_ID = BeansCorePlugin.PLUGIN_ID +
    															 ".beansnature";
	public static final String DEBUG_OPTION = BeansCorePlugin.PLUGIN_ID +
														"/project/nature/debug";
	public static boolean DEBUG = BeansCorePlugin.isDebug(DEBUG_OPTION);

	private IProject project;

	/**
	 * Constructor needed for <code>IProject.getNature()</code> and
	 * <code>IProject.addNature()</code>.
	 *
	 * @see #setProject(IProject)
	 */
	public BeansProjectNature() {
	}

    public void setProject(IProject project) {
		this.project = project;
    }

    public IProject getProject() {
        return project;
    }

	/**
	 * Adds Spring beans valdiator to project's list of external builders.
	 */
    public void configure() throws CoreException {
    	if (DEBUG) {
    		System.out.println("configuring Spring beans nature");
    	}
    	IProject project = getProject();
    	IProjectDescription desc = project.getDescription();
    	ICommand builderCommand = getBuilderCommand(desc,
    												BeansProjectValidator.BUILDER_ID);
    	if (builderCommand == null) {
    		// Add a new build spec
    		ICommand command = desc.newCommand();
    		command.setBuilderName(BeansProjectValidator.BUILDER_ID);

			// Commit the spec change into the project
    		setBuilderCommand(desc, command);
			project.setDescription(desc, null);
    	}
    }

	/**
	 * Removes Spring beans valdiator from project's list of external builders.
	 */
    public void deconfigure() throws CoreException {
    	if (DEBUG) {
    		System.out.println("deconfiguring Spring nature");
    	}
    	IProject project = getProject();
		BeansCoreUtils.removeProjectBuilder(project, BeansProjectValidator.BUILDER_ID);
    }

	public abstract void deleteProblemMarkers();

    private ICommand getBuilderCommand(IProjectDescription description,
									   String builderID) throws CoreException {
		ICommand command = null;
    	ICommand[] commands = description.getBuildSpec();
    	for (int i = commands.length - 1; i >= 0 ; i--) {
    		if (commands[i].getBuilderName().equals(builderID)) {
    			command = commands[i];
    			break;
    		}
    	}
    	return command;
    }

    private void setBuilderCommand(IProjectDescription description,
								   ICommand command) throws CoreException {
    	ICommand[] oldCommands = description.getBuildSpec();
    	ICommand oldBuilderCommand = getBuilderCommand(description,
    											   command.getBuilderName());
    	ICommand[] newCommands;
    
    	if (oldBuilderCommand == null) {
    		// Add given builder to the end of the builder list
    		newCommands = new ICommand[oldCommands.length + 1];
    		System.arraycopy(oldCommands, 0, newCommands, 0,
    						 oldCommands.length);
    		newCommands[oldCommands.length] = command;
    	} else {
    		// Replace old builder with given new one
    		for (int i = 0, max = oldCommands.length; i < max; i++) {
    			if (oldCommands[i] == oldBuilderCommand) {
    				oldCommands[i] = command;
    				break;
    			}
    		}
    		newCommands = oldCommands;
    	}
    
    	// Commit the spec change into the project
    	description.setBuildSpec(newCommands);
    	getProject().setDescription(description, null);
    }
}
