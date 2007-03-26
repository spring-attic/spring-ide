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
package org.springframework.ide.eclipse.webflow.core.model;

import org.eclipse.core.resources.IProject;

/**
 * 
 */
public interface IWebflowModel {

	/**
	 * 
	 * 
	 * @param project
	 * 
	 * @return
	 */
	IWebflowProject getProject(IProject project);

	void registerModelChangeListener(IWebflowModelListener listener);

	void removeModelChangeListener(IWebflowModelListener listener);

	void fireModelChangedEvent(IWebflowProject project);
}
