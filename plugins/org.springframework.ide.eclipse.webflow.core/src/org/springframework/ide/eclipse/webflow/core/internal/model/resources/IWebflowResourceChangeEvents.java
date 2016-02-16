/*******************************************************************************
 * Copyright (c) 2007 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.internal.model.resources;

import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.core.internal.model.resources.ISpringResourceChangeEvents;

/**
 * Defines callbacks for the {@link WebflowResourceChangeListener}.
 * @author Christian Dupuis
 * @since 2.0.1
 */
public interface IWebflowResourceChangeEvents
		extends ISpringResourceChangeEvents {

	void projectDescriptionChanged(IFile file, int eventType);

	void configAdded(IFile file, int eventType);

	void configChanged(IFile file, int eventType);

	void configRemoved(IFile file, int eventType);
}
