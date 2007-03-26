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
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;

/**
 * Defines callbacks for the <code>BeansResourceChangeListener</code>.
 * @see BeansResourceChangeListener
 * @author Torsten Juergeleit
 */
public interface IBeansResourceChangeEvents {

	boolean isSpringProject(IProject project, int eventType);

	void springNatureAdded(IProject project, int eventType);

	void springNatureRemoved(IProject project, int eventType);

	void projectAdded(IProject project, int eventType);

	void projectOpened(IProject project, int eventType);

	void projectClosed(IProject project, int eventType);

	void projectDeleted(IProject project, int eventType);

	void projectDescriptionChanged(IFile file, int eventType);

	void configAdded(IFile file, int eventType);

	void configChanged(IFile file, int eventType);

	void configRemoved(IFile file, int eventType);

	void beanClassChanged(String className, Set<IBeansConfig> configs,
			int eventType);
}
