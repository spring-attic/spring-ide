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
package org.springframework.ide.eclipse.beans.core;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

/**
 * Some helper methods.
 * @author Torsten Juergeleit
 */
public class BeansCoreUtils {

	/**
	 * Returns <code>true</code> if given resource is a Spring bean factory
	 * config file.
	 */
	public static boolean isBeansConfig(IResource resource) {
		if (resource instanceof IFile) {
			IBeansProject project = BeansCorePlugin.getModel().getProject(
					resource.getProject());
			if (project != null) {
				return project.hasConfig((IFile) resource);
			}
		}
		return false;
	}
}
