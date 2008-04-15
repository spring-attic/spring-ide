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
package org.springframework.ide.eclipse.beans.core.model.locate;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

/**
 * Extension interface to be implemented by third parties who want to provide auto-discovery
 * mechanisms for {@link IBeansConfig}s.
 * <p>
 * This extension point can be used to provide for autodecting default locations. like e.g. for the
 * established Spring DM best practice of placing all config files in META-INF/spring/*.xml.
 * @author Christian Dupuis
 * @since 2.0.5
 */
public interface IBeansConfigLocator {

	/**
	 * Locates {@link IFile} instances that should be {@link IBeansConfig} for the supplied
	 * {@link IProject}.
	 * <p>
	 * Note: Implementations of this interface are not allowed to put {@link IBeansConfig} instances
	 * in the {@link IBeansProject} themselves. This method is not allowed to return
	 * <code>null</code>.
	 * @return a set of {@link IFile}
	 */
	Set<IFile> locateBeansConfigs(IProject project);

	/**
	 * Checks if the given <code>file</code> is a {@link IBeansConfig} in the algorithm of this
	 * {@link IBeansConfigLocator}.
	 * <p>
	 * This method is called of every resource change in the workspace, so it should <b>not</b>
	 * perform expensive operations.
	 * @param file the file that could eventually be an Spring configuration file.
	 */
	boolean isBeansConfig(IFile file);

}
