/*******************************************************************************
 * Copyright (c) 2012 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.boot.core;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.springframework.ide.eclipse.boot.core.SpringBootStarter;

/**
 * SpringBoot-centric view on an IProject instance.
 * 
 * @author Kris De Volder
 */
public interface ISpringBootProject {

  
	/**
	 * @return corresponding Eclipse project.
	 */
	public IProject getProject();
  
	/**
	 * @return List of maven coordinates for known boot starters. These are discovered dynamically
	 * based on project contents. E.g. for maven projects we examine the 'dependencyManagement'
	 * section of the project's effective pom.
	 * 
	 * @throws CoreException 
	 */
	public List<SpringBootStarter> getKnownStarters() throws CoreException;

	/**
	 * Gets a list of bootstarters that are currently applied to the project.
	 * @throws CoreException 
	 */
	public List<SpringBootStarter> getBootStarters() throws CoreException;

	/**
	 * Modify project classpath, adding a SpringBootStarter. Note that this has to be done indirectly,
	 * by modifying the project's build scripts or pom rather than by directly modifying the classpath
	 * itself.
	 * @throws CoreException 
	 */
	public void addStarter(SpringBootStarter webStarter) throws CoreException;

	/**
	 * Modify project classpath, removing a SpringBootStarter. Note that this has to be done indirectly,
	 * by modifying the project's build scripts or pom rather than by directly modifying the classpath
	 * itself.
	 */
	public void removeStarter(SpringBootStarter webStarter) throws CoreException;

	/**
	 * Modify project classpath adding and/or removing starters to make them match the given
	 * set of starters. Note that versions of starters are generally ignored by this operation.
	 * @throws CoreException 
	 */
	public void setStarters(Collection<SpringBootStarter> values) throws CoreException;

  
}
