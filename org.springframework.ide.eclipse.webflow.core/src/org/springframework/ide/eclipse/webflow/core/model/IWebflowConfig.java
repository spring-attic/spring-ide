/*******************************************************************************
 * Copyright (c) 2007, 2008 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.webflow.core.model;

import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.springframework.ide.eclipse.core.model.IModelElement;

/**
 * @author Christian Dupuis
 */
public interface IWebflowConfig extends IWebflowModelElement {

	void setResource(IFile file);

	IFile getResource();

	Set<IModelElement> getBeansConfigs();

	void setBeansConfigs(Set<IModelElement> beansConfigs);

	void setBeansConfigsElementIds(Set<String> beansConfigs);

	void addBeansConfigElementId(String id);

	IWebflowProject getProject();

	void setName(String name);

	String getName();
	
}
