/*******************************************************************************
 * Copyright (c) 2004, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.beans.core.internal.model.resources;

import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.beans.core.model.IBeansConfig;
import org.springframework.ide.eclipse.beans.core.model.locate.IBeansConfigLocator;
import org.springframework.ide.eclipse.core.internal.model.resources.ISpringResourceChangeEvents;

/**
 * Defines callbacks for the {@link BeansResourceChangeListener}.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public interface IBeansResourceChangeEvents extends ISpringResourceChangeEvents {

	void projectDescriptionChanged(IFile file, int eventType);

	void configAdded(IFile file, int eventType);

	void configAdded(IFile file, int eventType, IBeansConfig.Type type);

	void configChanged(IFile file, int eventType);

	void configRemoved(IFile file, int eventType);

	/**
	 * A file listened by one of the {@link IBeansConfigLocator}s changed. 
	 * @since 2.0.5
	 */
	void listenedFileChanged(IFile file, int eventType);
}
