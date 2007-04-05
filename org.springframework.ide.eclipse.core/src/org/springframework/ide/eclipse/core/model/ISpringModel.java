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
package org.springframework.ide.eclipse.core.model;

import java.util.Set;

import org.springframework.ide.eclipse.core.SpringCore;

/**
 * This model manages instances of {@link ISpringProject}s.
 * {@link ISpringModelChangedListener}s register with this model and receive
 * {@link SpringModelChangedEvent}s for all changes.
 * <p>
 * The single instance of {@link ISpringModel} is available from the static
 * method {@link SpringCore#getModel()}.
 * 
 * @author Torsten Juergeleit
 * @since 2.0
 */
public interface ISpringModel extends IModel {

	String ELEMENT_NAME = "SpringModel";

	/**
	 * Returns a list of all projects defined in this model.
	 */
	Set<ISpringProject> getProjects();
}
