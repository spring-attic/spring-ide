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

/**
 * Defines callbacks for the {@link SpringResourceChangeListener}.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public interface ISpringResourceChangeEvents {

	boolean isSpringProject(IProject project, int eventType);

	void springNatureAdded(IProject project, int eventType);

	void springNatureRemoved(IProject project, int eventType);

	void projectAdded(IProject project, int eventType);

	void projectOpened(IProject project, int eventType);

	void projectClosed(IProject project, int eventType);

	void projectDeleted(IProject project, int eventType);
}
