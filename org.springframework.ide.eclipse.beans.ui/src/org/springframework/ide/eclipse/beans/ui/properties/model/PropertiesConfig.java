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
package org.springframework.ide.eclipse.beans.ui.properties.model;

import org.springframework.ide.eclipse.beans.core.internal.model.BeansConfig;
import org.springframework.ide.eclipse.beans.core.model.IBeansProject;

/**
 * This class defines a Spring beans configuration.
 * @author Torsten Juergeleit
 * @author Christian Dupuis
 */
public class PropertiesConfig extends BeansConfig {

	public PropertiesConfig(PropertiesProject project, String name, Type type) {
		super(project, name, type);
	}

	public PropertiesConfig(PropertiesConfigSet configSet, String configName, Type type) {
		super((IBeansProject) configSet.getElementParent(), configName, type);

		// After initializing the config with the corresponding project
		// (required for retrieving the file) change the parent to the given
		// config set
		setElementParent(configSet);
	}
}
