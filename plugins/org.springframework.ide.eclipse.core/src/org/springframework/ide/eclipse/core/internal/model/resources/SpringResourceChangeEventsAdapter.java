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
package org.springframework.ide.eclipse.core.internal.model.resources;

import org.eclipse.core.resources.IProject;

/**
 * Adapter implementation that enables for easy implementation of
 * {@link ISpringResourceChangeEvents}.
 * @author Christian Dupuis
 * @since 2.0
 */
public abstract class SpringResourceChangeEventsAdapter implements
		ISpringResourceChangeEvents {

	public void projectAdded(IProject project, int eventType) {
	}

	public void projectClosed(IProject project, int eventType) {
	}

	public void projectDeleted(IProject project, int eventType) {
	}

	public void projectOpened(IProject project, int eventType) {
	}

	public void springNatureAdded(IProject project, int eventType) {
	}

	public void springNatureRemoved(IProject project, int eventType) {
	}
}
