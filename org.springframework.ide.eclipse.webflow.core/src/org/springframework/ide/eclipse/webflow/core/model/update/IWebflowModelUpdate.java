/*******************************************************************************
 * Copyright (c) 2005, 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.model.update;

import org.springframework.ide.eclipse.webflow.core.model.IWebflowProject;

/**
 * A model update is applied if the {@link #requiresUpdate(IWebflowProject)} method returns true, indicating the this
 * update wants to update the given project by calling the {@link #updateProject(IWebflowProject)}.
 * @author Christian Dupuis
 * @since 2.5.2
 */
public interface IWebflowModelUpdate {

	/**
	 * Returns true if the given project needs to be updated.
	 */
	boolean requiresUpdate(IWebflowProject webflowProject);

	/**
	 * Update the given project.
	 */
	void updateProject(IWebflowProject webflowProject);

	/**
	 * Returns a useful description for that update to displayed in the UI.
	 */
	String getName();

}
