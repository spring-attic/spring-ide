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
package org.springframework.ide.eclipse.beans.ui.properties.model;

import org.springframework.ide.eclipse.beans.core.internal.model.BeansModel;
import org.springframework.ide.eclipse.core.model.IModelChangeListener;
import org.springframework.ide.eclipse.core.model.ModelChangeEvent;

/**
 * This model manages instances of {@link PropertiesProject}s. It's populated
 * by calling {@link #addProject(PropertiesProject)}.
 * <p>
 * {@link IModelChangeListener}s register with the {@link BeansModel}, and
 * receive {@link ModelChangeEvent}s for all changes.
 * 
 * @author Torsten Juergeleit
 */
public class PropertiesModel extends BeansModel {

	public void addProject(PropertiesProject project) {
		projects.put(project.getProject(), project);
	}
}
